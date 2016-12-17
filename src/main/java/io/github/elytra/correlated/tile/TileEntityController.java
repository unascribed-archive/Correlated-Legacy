package io.github.elytra.correlated.tile;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;
import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.block.BlockController;
import io.github.elytra.correlated.block.BlockController.State;
import io.github.elytra.correlated.helper.DriveComparator;
import io.github.elytra.correlated.item.ItemDrive;
import io.github.elytra.correlated.item.ItemMemory;
import io.github.elytra.correlated.storage.IDigitalStorage;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface="cofh.api.energy.IEnergyReceiver",modid="CoFHAPI|energy")
public class TileEntityController extends TileEntityNetworkMember implements ITickable, IDigitalStorage, IEnergyStorage {
	
	public boolean error = false;
	public boolean booting = true;
	public String errorReason;
	private long consumedPerTick = Correlated.inst.controllerRfUsage;
	private long energyCapacity = Correlated.inst.controllerCapacity;
	public int bootTicks = 0;
	private int totalScanned = 0;
	private transient Set<BlockPos> networkMemberLocations = Sets.newHashSet();
	private transient List<TileEntityInterface> interfaces = Lists.newArrayList();
	private transient List<TileEntityWirelessReceiver> receivers = Lists.newArrayList();
	private transient List<TileEntityDriveBay> driveBays = Lists.newArrayList();
	private transient List<TileEntityMemoryBay> memoryBays = Lists.newArrayList();
	private transient List<ItemStack> drives = Lists.newArrayList();
	private transient Set<ItemStack> prototypes;
	private transient Multiset<Class<? extends TileEntityNetworkMember>> memberTypes = HashMultiset.create(7);
	public int changeId = 0;
	private boolean checkingInfiniteLoop = false;
	
	private long maxMemory = 0;
	
	// Measured in Teslas, also accepts RF and CapabilityEnergy
	private long energy;
	
	public TileEntityController() {
		prototypes = new TCustomHashSet<>(new HashingStrategy<ItemStack>() {
			private static final long serialVersionUID = 7782704091709458883L;

			@Override
			public int computeHashCode(ItemStack is) {
				// intentionally excludes quantity
				
				// excludes capabilities, due to there being no good way to get
				// a capability hashcode - it'll have to collide and get
				// resolved in equals. oh well.
				if (is == null) return 0;
				int res = 1;
				if (is.hasTagCompound()) {
					res = (31 * res) + is.getTagCompound().hashCode();
				} else {
					res *= 31;
				}
				res = (31 * res) + is.getItem().hashCode();
				res = (31 * res) + is.getMetadata();
				return res;
			}

			@Override
			public boolean equals(ItemStack o1, ItemStack o2) {
				// also intentionally excludes quantity
				if (o1 == o2) return true;
				if (o1 == null || o2 == null) return false;
				if (o1.hasTagCompound() != o2.hasTagCompound()) return false;
				if (o1.getItem() != o2.getItem()) return false;
				if (o1.getMetadata() != o2.getMetadata()) return false;
				if (!Objects.equal(o1.getTagCompound(), o2.getTagCompound())) return false;
				if (!o1.areCapsCompatible(o2)) return false;
				return true;
			}
			
		});
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		energy = compound.getLong("Energy");
		if (energy > energyCapacity) energy = energyCapacity;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setLong("Energy", energy);
		return compound;
	}

	@Override
	public void update() {
		if (!hasWorld() || getWorld().isRemote) return;
		if (bootTicks > 100 && booting) {
			/*
			 * The booting delay is meant to deal with people avoiding the
			 * system's passive power drain by just shutting it off when it's
			 * not in use. Without this, I'd expect a common setup to be hooking
			 * up some sort of RF toggle to a pressure plate, so the system is
			 * only online when someone is actively using it. This makes such a
			 * minmax setup inconvenient.
			 */
			booting = false;
			scanNetwork();
		}
		if (isPowered()) {
			modifyEnergyStored(-getEnergyConsumedPerTick());
			bootTicks++;
		} else {
			energy = 0;
		}
		if (getTotalUsedMemory() > maxMemory) {
			error = true;
			errorReason = "out_of_memory";
		} else if ("out_of_memory".equals(errorReason)) {
			error = false;
			errorReason = null;
			bootTicks = 0;
			booting = true;
		}
		updateState();
	}

	@Override
	public long getEnergyConsumedPerTick() {
		return consumedPerTick;
	}

	@Override
	public boolean hasStorage() {
		return true;
	}

	@Override
	public TileEntityController getStorage() {
		return this;
	}

	@Override
	public void setController(TileEntityController controller) {}

	public void scanNetwork() {
		if (!hasWorld()) return;
		if (world.isRemote) return;
		if (booting) return;
		Set<BlockPos> seen = Sets.newHashSet();
		List<TileEntityNetworkMember> members = Lists.newArrayList();
		List<BlockPos> queue = Lists.newArrayList(getPos());
		boolean foundOtherController = false;

		for (BlockPos pos : networkMemberLocations) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityNetworkMember) {
				((TileEntityNetworkMember)te).setController(null);
			}
		}

		totalScanned = 0;
		networkMemberLocations.clear();
		driveBays.clear();
		memoryBays.clear();
		receivers.clear();
		interfaces.clear();
		prototypes.clear();

		int itr = 0;
		while (!queue.isEmpty()) {
			if (itr > 100) {
				error = true;
				errorReason = "network_too_big";
				consumedPerTick = Correlated.inst.controllerErrorUsage_NetworkTooBig;
				return;
			}
			BlockPos pos = queue.remove(0);
			seen.add(pos);
			TileEntity te = getWorld().getTileEntity(pos);
			if (te instanceof TileEntityNetworkMember) {
				for (EnumFacing ef : EnumFacing.VALUES) {
					BlockPos p = pos.offset(ef);
					if (seen.contains(p)) continue;
					seen.add(p);
					if (world.getTileEntity(p) == null) {
						continue;
					}
					queue.add(p);
				}
				if (te != this) {
					if (te instanceof TileEntityController) {
						error = true;
						((TileEntityController) te).error = true;
						Correlated.log.debug("Found other controller");
						foundOtherController = true;
					}
					if (!members.contains(te)) {
						TileEntityNetworkMember tenm = (TileEntityNetworkMember) te;
						members.add(tenm);
						if (te instanceof TileEntityDriveBay) {
							driveBays.add((TileEntityDriveBay)te);
						} else if (te instanceof TileEntityInterface) {
							interfaces.add((TileEntityInterface)te);
						} else if (te instanceof TileEntityWirelessReceiver) {
							receivers.add((TileEntityWirelessReceiver)te);
						} else if (te instanceof TileEntityMemoryBay) {
							memoryBays.add((TileEntityMemoryBay)te);
						}
						networkMemberLocations.add(pos);
						memberTypes.add(tenm.getClass());
						consumedPerTick += tenm.getEnergyConsumedPerTick();
					}
				}
			}
			itr++;
		}
		if (foundOtherController) {
			error = true;
			errorReason = "multiple_controllers";
			consumedPerTick = Correlated.inst.controllerErrorUsage_MultipleControllers;
		} else {
			error = false;
			errorReason = null;
		}
		checkInfiniteLoop();
		for (TileEntityNetworkMember te : members) {
			te.setController(this);
		}
		totalScanned = itr;
		long energyUsage = Correlated.inst.controllerRfUsage;
		for (TileEntityNetworkMember tenm : members) {
			energyUsage += tenm.getEnergyConsumedPerTick();
		}
		consumedPerTick = energyUsage;
		if (consumedPerTick > Correlated.inst.controllerCap) {
			error = true;
			errorReason = "too_much_power";
		}
		updateDrivesCache();
		updateMemoryCache();
		booting = false;
		Correlated.log.debug("Found "+members.size()+" network members");
	}
	
	public void checkInfiniteLoop() {
		checkingInfiniteLoop = true;
		for (TileEntityWirelessReceiver r : receivers) {
			TileEntityController cont = r.getTransmitterController();
			if (cont != null && cont.isLinkedTo(this, 0)) {
				error = true;
				errorReason = "infinite_loop";
				receivers.clear();
				checkingInfiniteLoop = false;
				return;
			}
		}
		if (error && "infinite_loop".equals(errorReason)) {
			error = false;
			errorReason = null;
		}
		checkingInfiniteLoop = false;
	}
	
	public boolean isCheckingInfiniteLoop() {
		return checkingInfiniteLoop;
	}
	
	public boolean isLinkedTo(TileEntityController tec, int depth) {
		// bail out now in case our infinite loop checking is causing infinite recursion
		if (depth > 50) return true;
		if (tec.equals(this)) return true;
		for (TileEntityWirelessReceiver r : receivers) {
			TileEntityController cont = r.getTransmitterController();
			if (cont != null && cont.isLinkedTo(tec, depth + 1)) {
				return true;
			}
		}
		return false;
	}

	private void updateState() {
		if (!hasWorld()) return;
		if (world.isRemote) return;
		boolean cheaty = world.getBlockState(getPos()).getValue(BlockController.cheaty);
		State old = world.getBlockState(getPos()).getValue(BlockController.state);
		State nw;
		if (cheaty) {
			energy = energyCapacity;
		}
		if (isPowered()) {
			if (old == State.OFF) {
				booting = true;
				bootTicks = -200;
			}
			if (booting) {
				nw = State.BOOTING;
			} else if (error) {
				nw = State.ERROR;
			} else {
				nw = State.POWERED;
			}
		} else {
			nw = State.OFF;
		}
		if (old != nw) {
			world.setBlockState(getPos(), world.getBlockState(getPos())
					.withProperty(BlockController.state, nw));
		}
	}
	
	@Override
	public boolean isPowered() {
		return energy >= getEnergyConsumedPerTick();
	}

	/** assumes the network cache is also up to date, if it's not, call scanNetwork */
	public void updateDrivesCache() {
		if (hasWorld() && world.isRemote) return;
		drives.clear();
		prototypes.clear();
		for (TileEntityDriveBay tedb : driveBays) {
			if (tedb.isInvalid()) continue;
			for (ItemStack is : tedb) {
				drives.add(is);
				ItemDrive id = (ItemDrive)is.getItem();
				prototypes.addAll(id.getPrototypes(is));
			}
		}
		Collections.sort(drives, new DriveComparator());
	}
	
	public void updateMemoryCache() {
		if (!hasWorld() || world.isRemote) return;
		maxMemory = 0;
		for (TileEntityMemoryBay temb : memoryBays) {
			if (temb.isInvalid()) continue;
			for (int i = 0; i < 12; i++) {
				if (temb.hasMemoryInSlot(i)) {
					ItemStack stack = temb.getMemoryInSlot(i);
					if (stack.getItem() instanceof ItemMemory) {
						maxMemory += ((ItemMemory)stack.getItem()).getMaxBits(stack);
					}
				}
			}
		}
		bootTicks = 0;
		booting = true;
	}

	public void updateConsumptionRate(long change) {
		consumedPerTick += change;
		if (consumedPerTick > Correlated.inst.controllerCap) {
			error = true;
			errorReason = "too_much_power";
		} else {
			if (error && "too_much_power".equals(errorReason)) {
				error = false;
				errorReason = null;
			}
		}
	}

	@Override
	public ItemStack addItemToNetwork(ItemStack stack) {
		if (error) return stack;
		if (!prototypes.contains(stack) && getTotalUsedMemory()+getMemoryUsage(stack) > getMaxMemory()) {
			return stack;
		}
		for (ItemStack drive : drives) {
			// both these conditions should always be true, but might as well be safe
			if (drive != null && drive.getItem() instanceof ItemDrive) {
				int oldSize = stack.getCount();
				ItemDrive itemDrive = ((ItemDrive)drive.getItem());
				itemDrive.addItem(drive, stack);
				if (stack.getCount() < oldSize && !prototypes.contains(stack)) {
					prototypes.add(stack.copy());
				}
				if (stack.isEmpty()) break;
			}
		}
		for (TileEntityWirelessReceiver r : receivers) {
			TileEntityController cont = r.getTransmitterController();
			if (cont != null) {
				cont.addItemToNetwork(stack);
			}
			if (stack.isEmpty()) break;
		}
		changeId++;
		return stack;
	}

	@Override
	public ItemStack removeItemsFromNetwork(ItemStack prototype, int amount, boolean checkInterfaces) {
		if (error) return ItemStack.EMPTY;
		ItemStack stack = prototype.copy();
		stack.setCount(0);
		if (checkInterfaces) {
			for (TileEntityInterface in : interfaces) {
				for (int i = 9; i <= 17; i++) {
					ItemStack is = in.getStackInSlot(i);
					if (is != null && ItemStack.areItemsEqual(is, prototype) && ItemStack.areItemStackTagsEqual(is, prototype)) {
						int amountWanted = amount-stack.getCount();
						int amountTaken = Math.min(is.getCount(), amountWanted);
						is.setCount(is.getCount()-amountTaken);
						stack.setCount(stack.getCount()+amountTaken);
						if (stack.getCount() >= amount) break;
					}
				}
			}
		}
		boolean anyDriveStillHasItem = false;
		for (ItemStack drive : drives) {
			// both these conditions should always be true, but might as well be safe
			if (drive != null && drive.getItem() instanceof ItemDrive) {
				ItemDrive itemDrive = ((ItemDrive)drive.getItem());
				int amountWanted = amount-stack.getCount();
				ItemStack res = itemDrive.removeItems(drive, prototype, amountWanted);
				stack.grow(res.getCount());
				if (!anyDriveStillHasItem && itemDrive.getAmountStored(drive, prototype) > 0) {
					anyDriveStillHasItem = true;
				}
				if (stack.getCount() >= amount) break;
			}
		}
		for (TileEntityWirelessReceiver r : receivers) {
			TileEntityController cont = r.getTransmitterController();
			if (cont != null) {
				ItemStack remote = cont.removeItemsFromNetwork(prototype, amount-stack.getCount(), checkInterfaces);
				if (remote != null) {
					stack.setCount(stack.getCount()+remote.getCount());
				}
			}
			if (stack.getCount() >= amount) break;
		}
		if (!anyDriveStillHasItem) {
			prototypes.remove(prototype);
		}
		changeId++;
		return stack;
	}
	
	@Override
	public int getKilobitsStorageFree() {
		int accum = 0;
		for (ItemStack drive : drives) {
			if (drive != null && drive.getItem() instanceof ItemDrive) {
				accum += ((ItemDrive)drive.getItem()).getKilobitsFree(drive);
			}
		}
		return accum;
	}
	
	private long getMemoryUsage(ItemStack is) {
		return (8L + ItemDrive.getNBTComplexity(is.getTagCompound()));
	}
	
	public long getUsedTypeMemory() {
		long count = 0;
		for (ItemStack is : prototypes) {
			count += getMemoryUsage(is);
		}
		return count;
	}
	
	public long getUsedNetworkMemory() {
		return totalScanned * 6L;
	}
	
	public long getUsedWirelessMemory() {
		return (memberTypes.count(TileEntityWirelessReceiver.class) * 16L) + (memberTypes.count(TileEntityWirelessTransmitter.class) * 32L);
	}
	
	public long getTotalUsedMemory() {
		return getUsedTypeMemory()+getUsedNetworkMemory()+getUsedWirelessMemory();
	}
	
	public long getBitsMemoryFree() {
		return getMaxMemory()-getTotalUsedMemory();
	}
	
	public long getMaxMemory() {
		return maxMemory;
	}

	@Override
	public List<ItemStack> getTypes() {
		List<ItemStack> li = Lists.newArrayList();
		for (ItemStack drive : drives) {
			if (drive != null && drive.getItem() instanceof ItemDrive) {
				li.addAll(((ItemDrive)drive.getItem()).getTypes(drive));
			}
		}
		for (TileEntityInterface in : interfaces) {
			for (int i = 9; i <= 17; i++) {
				ItemStack ifaceStack = in.getStackInSlot(i);
				boolean added = false;
				for (ItemStack cur : li) {
					if (ItemStack.areItemsEqual(ifaceStack, cur) && ItemStack.areItemStackTagsEqual(ifaceStack, cur)) {
						cur.setCount(cur.getCount()+ifaceStack.getCount());
						added = true;
						break;
					}
				}
				if (!added) {
					li.add(ifaceStack.copy());
				}
			}
		}
		for (TileEntityWirelessReceiver r : receivers) {
			TileEntityController cont = r.getTransmitterController();
			if (cont != null) {
				li.addAll(cont.getTypes());
			}
		}
		return li;
	}

	public void onNetworkPatched(TileEntityNetworkMember tenm) {
		if (totalScanned == 0) return;
		if (tenm instanceof TileEntityDriveBay) {
			if (!driveBays.contains(tenm)) {
				driveBays.add((TileEntityDriveBay)tenm);
				updateDrivesCache();
				changeId++;
			}
		} else if (tenm instanceof TileEntityInterface) {
			if (!interfaces.contains(tenm)) {
				interfaces.add((TileEntityInterface)tenm);
				changeId++;
			}
		} else if (tenm instanceof TileEntityWirelessReceiver) {
			if (!receivers.contains(tenm)) {
				receivers.add((TileEntityWirelessReceiver)tenm);
				checkInfiniteLoop();
				changeId++;
			}
		} else if (tenm instanceof TileEntityMemoryBay) {
			if (!memoryBays.contains(tenm)) {
				memoryBays.add((TileEntityMemoryBay)tenm);
				updateMemoryCache();
				changeId++;
			}
		}
		if (networkMemberLocations.add(tenm.getPos())) {
			totalScanned++;
			if (totalScanned > 100) {
				error = true;
				errorReason = "network_too_big";
				consumedPerTick = Correlated.inst.controllerErrorUsage_NetworkTooBig;
			}
		}
	}

	public boolean knowsOfMemberAt(BlockPos pos) {
		return networkMemberLocations.contains(pos);
	}

	@Override
	public int getChangeId() {
		return changeId;
	}
	
	
	
	public void modifyEnergyStored(long energy) {
		this.energy += energy;
		if (this.energy > energyCapacity) {
			this.energy = energyCapacity;
		} else if (this.energy < 0) {
			this.energy = 0;
		}
	}
	
	public long receiveEnergy(long maxReceive, boolean simulate) {
		long energyReceived = Math.min(energyCapacity - energy,
				Math.min(Correlated.inst.controllerCap+1, maxReceive));

		if (!simulate) {
			energy += energyReceived;
		}
		return energyReceived;
	}
	
	
	
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return Ints.saturatedCast(receiveEnergy((long)maxReceive, simulate));
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored() {
		return Ints.saturatedCast(energy);
	}

	@Override
	public int getMaxEnergyStored() {
		return Ints.saturatedCast(energyCapacity);
	}

	@Override
	public boolean canExtract() {
		return false;
	}

	@Override
	public boolean canReceive() {
		return true;
	}
	
	
	private Object teslaConsumer;
	
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == null) return null;
		if (capability == CapabilityEnergy.ENERGY) {
			return (T)this;
		} else if (capability == Correlated.TESLA_CONSUMER) {
			if (teslaConsumer == null) {
				teslaConsumer = new TeslaConsumer();
			}
			return (T)teslaConsumer;
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == null) return false;
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		} else if (capability == Correlated.TESLA_CONSUMER) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	public class TeslaConsumer implements ITeslaConsumer {
		@Override
		public long givePower(long power, boolean simulated) {
			return receiveEnergy(power, simulated);
		}
	}

}
