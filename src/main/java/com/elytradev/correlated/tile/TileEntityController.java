package com.elytradev.correlated.tile;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.block.BlockController;
import com.elytradev.correlated.block.BlockController.State;
import com.elytradev.correlated.compat.probe.UnitPotential;
import com.elytradev.correlated.helper.DriveComparator;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.item.ItemDrive;
import com.elytradev.correlated.item.ItemMemory;
import com.elytradev.correlated.storage.IDigitalStorage;
import com.elytradev.correlated.storage.InsertResult;
import com.elytradev.correlated.storage.InsertResult.Result;
import com.elytradev.correlated.wifi.IWirelessClient;
import com.elytradev.correlated.wifi.Station;
import com.elytradev.correlated.wifi.WirelessManager;
import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import com.elytradev.probe.api.UnitDictionary;
import com.elytradev.probe.api.impl.ProbeData;
import com.google.common.base.Objects;
import com.google.common.collect.EnumMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface="elucent.albedo.lighting.ILightProvider", modid="albedo")
public class TileEntityController extends TileEntityAbstractEnergyAcceptor implements ITickable, IDigitalStorage, IWirelessClient, ILightProvider {
	
	public boolean error = false;
	public boolean booting = true;
	public String errorReason;
	private int consumedPerTick = CConfig.controllerPUsage;
	private int energyCapacity = CConfig.controllerCapacity;
	public int bootTicks = 0;
	private int totalScanned = 0;
	private transient Set<BlockPos> networkMemberLocations = Sets.newHashSet();
	private transient List<TileEntityInterface> interfaces = Lists.newArrayList();
	private transient List<TileEntityDriveBay> driveBays = Lists.newArrayList();
	private transient List<TileEntityMemoryBay> memoryBays = Lists.newArrayList();
	private transient List<TileEntityMicrowaveBeam> beams = Lists.newArrayList();
	private transient List<ItemStack> drives = Lists.newArrayList();
	private transient Set<ItemStack> prototypes;
	private transient Multiset<Class<? extends TileEntityNetworkMember>> memberTypes = HashMultiset.create(7);
	public int changeId = 0;
	
	protected long clientMemoryMax;
	protected long clientMemoryUsed;
	protected int clientEnergy;
	protected int clientEnergyMax;
	
	private long maxMemory = 0;
	
	private String apn;
	
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
	public int getMaxPotential() {
		return energyCapacity;
	}
	
	@Override
	public int getReceiveCap() {
		return CConfig.controllerCap;
	}
	
	@Override
	public boolean canReceivePotential() {
		return true;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("APN", NBT.TAG_STRING)) {
			apn = compound.getString("APN");
		} else {
			apn = null;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (apn != null) {
			compound.setString("APN", apn);
		}
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
			modifyEnergyStored(-getPotentialConsumedPerTick());
			bootTicks++;
		} else {
			potential = 0;
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
		if (getClientEnergy() != getPotentialStored() ||
				getClientEnergyMax() != getMaxPotential() ||
				getClientMemoryMax() != getMaxMemory() ||
				getClientMemoryUsed() != getTotalUsedMemory()) {
			CNetwork.sendUpdatePacket(this);
		}
		updateState();
	}
	
	@Override
	@Optional.Method(modid="albedo")
	public Light provideLight() {
		float x = (float)getX();
		float y = (float)getY();
		float z = (float)getZ();
		
		int color = 0;
		
		IBlockState state = getWorld().getBlockState(getPos());
		if (state.getBlock() == CBlocks.CONTROLLER) {
			switch (state.getValue(BlockController.STATE)) {
				case OFF:
				case BOOTING:
					return null;
				case ERROR:
					color = Correlated.proxy.getColor("other", 65);
					break;
				case POWERED:
					color = Correlated.proxy.getColor("other", 64);
					break;
			}
		}
		
		return Light.builder()
				.pos(x, y, z)
				.color(color, true)
				.radius(1.5f)
				.build();
	}

	@Override
	public int getPotentialConsumedPerTick() {
		return consumedPerTick;
	}

	@Override
	public boolean hasController() {
		return true;
	}

	@Override
	public TileEntityController getController() {
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
		interfaces.clear();
		prototypes.clear();
		beams.clear();
		memberTypes.clear();

		int itr = 0;
		while (!queue.isEmpty()) {
			if (itr > 100) {
				error = true;
				errorReason = "network_too_big";
				consumedPerTick = CConfig.controllerErrorUsage_NetworkTooBig;
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
						CLog.debug("Found other controller");
						foundOtherController = true;
					}
					if (!members.contains(te)) {
						TileEntityNetworkMember tenm = (TileEntityNetworkMember) te;
						members.add(tenm);
						if (te instanceof TileEntityDriveBay) {
							driveBays.add((TileEntityDriveBay)te);
						} else if (te instanceof TileEntityInterface) {
							interfaces.add((TileEntityInterface)te);
						} else if (te instanceof TileEntityMemoryBay) {
							memoryBays.add((TileEntityMemoryBay)te);
						} else if (te instanceof TileEntityMicrowaveBeam) {
							beams.add((TileEntityMicrowaveBeam)te);
						}
						networkMemberLocations.add(pos);
						memberTypes.add(tenm.getClass());
						consumedPerTick += tenm.getPotentialConsumedPerTick();
					}
				}
			}
			itr++;
		}
		if (foundOtherController) {
			error = true;
			errorReason = "multiple_controllers";
			consumedPerTick = CConfig.controllerErrorUsage_MultipleControllers;
		} else {
			error = false;
			errorReason = null;
		}
		for (TileEntityNetworkMember te : members) {
			te.setController(this);
		}
		totalScanned = itr;
		int energyUsage = CConfig.controllerPUsage;
		for (TileEntityNetworkMember tenm : members) {
			energyUsage += tenm.getPotentialConsumedPerTick();
		}
		consumedPerTick = energyUsage;
		if (consumedPerTick > CConfig.controllerCap) {
			error = true;
			errorReason = "too_much_power";
		}
		updateDrivesCache();
		updateMemoryCache();
		booting = false;
		CLog.debug("Found "+members.size()+" network members");
	}

	private void updateState() {
		if (!hasWorld()) return;
		if (world.isRemote) return;
		boolean cheaty = world.getBlockState(getPos()).getValue(BlockController.CHEATY);
		State old = world.getBlockState(getPos()).getValue(BlockController.STATE);
		State nw;
		if (cheaty) {
			potential = energyCapacity;
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
					.withProperty(BlockController.STATE, nw));
		}
	}
	
	@Override
	public boolean isPowered() {
		return getPotentialStored() >= getPotentialConsumedPerTick();
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
		maxMemory = 128;
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

	public void updateConsumptionRate(int change) {
		consumedPerTick += change;
		if (consumedPerTick > CConfig.controllerCap) {
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
	public InsertResult addItemToNetwork(ItemStack stack, Set<IDigitalStorage> alreadyChecked) {
		if (error) return InsertResult.refused(stack);
		if (stack == null || stack.isEmpty()) return InsertResult.success(stack);
		boolean hasCheckedMemory = false;
		boolean insufficientMemory = false;
		Multiset<Result> results = EnumMultiset.create(Result.class);
		for (ItemStack drive : drives) {
			if (stack.isEmpty()) break;
			// both these conditions should always be true, but might as well be safe
			if (drive != null && drive.getItem() instanceof ItemDrive) {
				ItemDrive itemDrive = ((ItemDrive)drive.getItem());
				ItemStack copy = stack.copy();
				copy.setCount(1);
				if (!hasCheckedMemory) {
					InsertResult sim = itemDrive.addItem(drive, stack, true);
					if (sim.wasSuccessful()) {
						// specifically ignores SUCCESS_VOIDED
						if (sim.result == Result.SUCCESS && !prototypes.contains(copy)
								&& getTotalUsedMemory()+getMemoryUsage(stack) > getMaxMemory()) {
							insufficientMemory = true;
							break;
						}
						hasCheckedMemory = true;
					}
				}
				InsertResult ir = itemDrive.addItem(drive, stack, false);
				results.add(ir.result);
				stack = ir.stack;
				if (ir.result == Result.SUCCESS && !prototypes.contains(copy)) {
					prototypes.add(copy);
				}
			}
		}
		alreadyChecked.add(this);
		for (IDigitalStorage other : getRemotes(alreadyChecked)) {
			if (stack.isEmpty()) break;
			if (other != null) {
				InsertResult ir = other.addItemToNetwork(stack, alreadyChecked);
				results.add(ir.result);
				stack = ir.stack;
				insufficientMemory = false;
			}
		}
		if (insufficientMemory) {
			return InsertResult.insufficientMemory(stack);
		}
		changeId++;
		if (!results.contains(Result.SUCCESS) && !results.contains(Result.SUCCESS_VOIDED) && results.size() > 0) {
			Result result = null;
			int num = 0;
			for (Multiset.Entry<Result> en : results.entrySet()) {
				if (en.getCount() > num) {
					result = en.getElement();
					num = en.getCount();
				}
			}
			return new InsertResult(result, stack);
		}
		return stack.isEmpty() ? results.count(Result.SUCCESS_VOIDED) > results.count(Result.SUCCESS) ?
				InsertResult.successVoided(stack) : InsertResult.success(stack) : InsertResult.insufficientStorage(stack);
	}

	@Override
	public ItemStack removeItemsFromNetwork(ItemStack prototype, int amount, boolean checkInterfaces, Set<IDigitalStorage> alreadyChecked) {
		if (error) return ItemStack.EMPTY;
		ItemStack stack = prototype.copy();
		stack.setCount(0);
		boolean anyDriveStillHasItem = false;
		for (ItemStack drive : drives) {
			if (stack.getCount() >= amount) break;
			// both these conditions should always be true, but might as well be safe
			if (drive != null && drive.getItem() instanceof ItemDrive) {
				ItemDrive itemDrive = ((ItemDrive)drive.getItem());
				int amountWanted = amount-stack.getCount();
				ItemStack res = itemDrive.removeItems(drive, prototype, amountWanted);
				stack.grow(res.getCount());
				if (!anyDriveStillHasItem && itemDrive.getAmountStored(drive, prototype) > 0) {
					anyDriveStillHasItem = true;
				}
			}
		}
		alreadyChecked.add(this);
		for (IDigitalStorage other : getRemotes(alreadyChecked)) {
			if (stack.getCount() >= amount) break;
			ItemStack remote = other.removeItemsFromNetwork(prototype, amount-stack.getCount(), checkInterfaces, alreadyChecked);
			if (remote != null) {
				stack.grow(remote.getCount());
			}
		}
		if (checkInterfaces) {
			for (TileEntityInterface in : interfaces) {
				if (stack.getCount() >= amount) break;
				for (int i = 9; i <= 17; i++) {
					ItemStack is = in.getStackInSlot(i);
					if (is != null && ItemStack.areItemsEqual(is, prototype) && ItemStack.areItemStackTagsEqual(is, prototype)) {
						int amountWanted = amount-stack.getCount();
						int amountTaken = Math.min(is.getCount(), amountWanted);
						is.setCount(is.getCount()-amountTaken);
						stack.setCount(stack.getCount()+amountTaken);
					}
				}
			}
		}
		if (!anyDriveStillHasItem) {
			ItemStack copy = prototype.copy();
			copy.setCount(1);
			prototypes.remove(copy);
		}
		changeId++;
		return stack;
	}
	
	public List<IDigitalStorage> getRemotes() {
		return getRemotes(Sets.newHashSet(this));
	}
	
	public List<IDigitalStorage> getRemotes(Set<IDigitalStorage> alreadyChecked) {
		List<IDigitalStorage> li = Lists.newArrayList();
		getRemotes(alreadyChecked, li);
		return li;
	}
	
	public void getRemotes(Set<IDigitalStorage> alreadyChecked, List<IDigitalStorage> li) {
		for (TileEntityMicrowaveBeam beam : beams) {
			TileEntityController other = beam.getOtherSide();
			if (alreadyChecked.contains(other)) continue;
			if (other != null) {
				li.add(other);
				other.getRemotes(alreadyChecked, li);
			}
		}
		if (apn != null) {
			WirelessManager wm = CorrelatedWorldData.getFor(getWorld()).getWirelessManager();
			for (Station s : wm.allStationsInChunk(getWorld().getChunkFromBlockCoords(getPos()))) {
				if (s.getAPNs().contains(apn) && s.isInRange(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5)) {
					for (IDigitalStorage other : s.getStorages(apn)) {
						if (alreadyChecked.contains(other)) continue;
						if (other != null) {
							li.add(other);
							if (other instanceof TileEntityController) {
								((TileEntityController)other).getRemotes(alreadyChecked, li);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public int getKilobitsStorageFree(Set<IDigitalStorage> alreadyChecked) {
		int accum = 0;
		for (ItemStack drive : drives) {
			if (drive != null && drive.getItem() instanceof ItemDrive) {
				accum += ((ItemDrive)drive.getItem()).getKilobitsFree(drive);
			}
		}
		alreadyChecked.add(this);
		for (IDigitalStorage other : getRemotes(alreadyChecked)) {
			accum += other.getKilobitsStorageFree(alreadyChecked);
		}
		return accum;
	}
	
	private long getMemoryUsage(ItemStack is) {
		return (128L + ItemDrive.getNBTComplexity(is.getTagCompound()));
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
		return memberTypes.count(TileEntityMicrowaveBeam.class) * 80L;
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
	public void getTypes(Set<IDigitalStorage> alreadyChecked, List<ItemStack> li) {
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
		alreadyChecked.add(this);
		for (IDigitalStorage other : getRemotes(alreadyChecked)) {
			other.getTypes(alreadyChecked, li);
		}
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
				consumedPerTick = CConfig.controllerErrorUsage_NetworkTooBig;
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
	
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		nbt.setLong("MemoryUsed", clientMemoryUsed = getTotalUsedMemory());
		nbt.setLong("MemoryMax", clientMemoryMax = getMaxMemory());
		nbt.setInteger("Energy", clientEnergy = getPotentialStored());
		nbt.setInteger("EnergyMax", clientEnergyMax = getMaxPotential());
		return nbt;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound nbt) {
		clientMemoryUsed = nbt.getLong("MemoryUsed");
		clientMemoryMax = nbt.getLong("MemoryMax");
		clientEnergy = nbt.getInteger("Energy");
		clientEnergyMax = nbt.getInteger("EnergyMax");
	}
	
	public int getClientEnergy() {
		return clientEnergy;
	}
	
	public int getClientEnergyMax() {
		return clientEnergyMax;
	}
	
	public long getClientMemoryMax() {
		return clientMemoryMax;
	}
	
	public long getClientMemoryUsed() {
		return clientMemoryUsed;
	}
	
	
	
	@Override
	public void setAPNs(Set<String> apn) {
		if (apn.size() > 1) throw new IllegalArgumentException("Only supports 1 APN");
		this.apn = apn.isEmpty() ? null : apn.iterator().next();
	}

	@Override
	public Set<String> getAPNs() {
		return this.apn == null ? Collections.emptySet() : Collections.singleton(this.apn);
	}
	
	@Override
	public BlockPos getPosition() {
		return getPos();
	}
	
	@Override
	public double getX() {
		return getPos().getX()+0.5;
	}
	
	@Override
	public double getY() {
		return getPos().getY()+0.5;
	}
	
	@Override
	public double getZ() {
		return getPos().getZ()+0.5;
	}
	
	private Object probeCapability;
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == null) return null;
		if (capability == Correlated.PROBE) {
			if (probeCapability == null) probeCapability = new ProbeCapability();
			return (T)probeCapability;
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == null) return false;
		if (capability == Correlated.PROBE) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	private final class ProbeCapability implements IProbeDataProvider {
		@Override
		public void provideProbeData(List<IProbeData> data) {
			boolean cheaty = world.getBlockState(getPos()).getValue(BlockController.CHEATY);
			data.add(new ProbeData(new TextComponentTranslation("tooltip.correlated.energy_stored"))
					.withBar(0, cheaty ? Double.POSITIVE_INFINITY : clientEnergy, clientEnergyMax, UnitPotential.INSTANCE));
			data.add(new ProbeData(new TextComponentTranslation("tooltip.correlated.energy_usage"))
					.withBar(0, getPotentialConsumedPerTick(), CConfig.controllerCap, UnitPotential.INSTANCE));
			data.add(new ProbeData(new TextComponentTranslation("tooltip.correlated.memory"))
					.withBar(0, getTotalUsedMemory()/8D, getMaxMemory()/8D, UnitDictionary.BYTES));
			double storage = 0;
			double maxStorage = 0;
			for (ItemStack drive : drives) {
				if (drive != null && drive.getItem() instanceof ItemDrive) {
					ItemDrive id = (ItemDrive)drive.getItem();
					storage += (id.getKilobitsUsed(drive)/8D)*1024;
					maxStorage += (id.getMaxKilobits(drive)/8D)*1024;
				}
			}
			data.add(new ProbeData(new TextComponentTranslation("tooltip.correlated.storage"))
					.withBar(0, storage, maxStorage, UnitDictionary.BYTES));
			if (booting) {
				int timeLeft = 100-bootTicks;
				data.add(new ProbeData(new TextComponentTranslation("tooltip.correlated.boot_time"))
						.withBar(0, timeLeft, 300, UnitDictionary.TICKS));
			}
		}
	}


}
