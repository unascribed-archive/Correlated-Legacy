package io.github.elytra.copo.tile;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.block.BlockController;
import io.github.elytra.copo.block.BlockController.State;
import io.github.elytra.copo.helper.DriveComparator;
import io.github.elytra.copo.item.ItemDrive;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityController extends TileEntityNetworkMember implements IEnergyReceiver, ITickable {
	public static final int POWER_CAP = 640;
	
	private EnergyStorage energy = new EnergyStorage(POWER_CAP*100, POWER_CAP+1);
	public boolean error = false;
	public boolean booting = true;
	public String errorReason;
	private int consumedPerTick = 32;
	public int bootTicks = 0;
	private int networkMembers = 0;
	private transient Set<BlockPos> networkMemberLocations = Sets.newHashSet();
	private transient List<TileEntityInterface> interfaces = Lists.newArrayList();
	private transient List<TileEntityWirelessReceiver> receivers = Lists.newArrayList();
	private transient List<TileEntityDriveBay> driveBays = Lists.newArrayList();
	private transient List<ItemStack> drives = Lists.newArrayList();
	public int changeId = 0;
	private boolean checkingInfiniteLoop = false;

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		energy.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		energy.writeToNBT(compound);
		return compound;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return energy.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return energy.getMaxEnergyStored();
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public void update() {
		if (!hasWorldObj() || getWorld().isRemote) return;
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
			energy.modifyEnergyStored(-getEnergyConsumedPerTick());
			bootTicks++;
		} else {
			energy.setEnergyStored(0);
		}
		updateState();
	}

	@Override
	public int getEnergyConsumedPerTick() {
		return consumedPerTick;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		int rtrn = energy.receiveEnergy(maxReceive, simulate);
		updateState();
		return rtrn;
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
		if (!hasWorldObj()) return;
		if (worldObj.isRemote) return;
		if (booting) return;
		Set<BlockPos> seen = Sets.newHashSet();
		List<TileEntityNetworkMember> members = Lists.newArrayList();
		List<BlockPos> queue = Lists.newArrayList(getPos());
		boolean foundOtherController = false;
		int consumedPerTick = 32;

		for (BlockPos pos : networkMemberLocations) {
			TileEntity te = worldObj.getTileEntity(pos);
			if (te instanceof TileEntityNetworkMember) {
				((TileEntityNetworkMember)te).setController(null);
			}
		}

		networkMembers = 0;
		networkMemberLocations.clear();
		driveBays.clear();
		receivers.clear();
		interfaces.clear();

		int itr = 0;
		while (!queue.isEmpty()) {
			if (itr > 100) {
				error = true;
				errorReason = "network_too_big";
				consumedPerTick = POWER_CAP;
				return;
			}
			BlockPos pos = queue.remove(0);
			seen.add(pos);
			TileEntity te = getWorld().getTileEntity(pos);
			if (te instanceof TileEntityNetworkMember) {
				for (EnumFacing ef : EnumFacing.VALUES) {
					BlockPos p = pos.offset(ef);
					if (seen.contains(p)) continue;
					if (worldObj.getTileEntity(p) == null) {
						seen.add(p);
						continue;
					}
					queue.add(p);
				}
				if (te != this) {
					if (te instanceof TileEntityController) {
						error = true;
						((TileEntityController) te).error = true;
						CoPo.log.debug("Found other controller");
						foundOtherController = true;
					}
					if (!members.contains(te)) {
						members.add((TileEntityNetworkMember) te);
						if (te instanceof TileEntityDriveBay) {
							driveBays.add((TileEntityDriveBay)te);
						} else if (te instanceof TileEntityInterface) {
							interfaces.add((TileEntityInterface)te);
						} else if (te instanceof TileEntityWirelessReceiver) {
							receivers.add((TileEntityWirelessReceiver)te);
						}
						networkMemberLocations.add(pos);
						consumedPerTick += ((TileEntityNetworkMember) te).getEnergyConsumedPerTick();
					}
				}
			}
			itr++;
		}
		if (foundOtherController) {
			error = true;
			errorReason = "multiple_controllers";
			consumedPerTick = 4;
		} else {
			error = false;
			errorReason = null;
		}
		checkInfiniteLoop();
		for (TileEntityNetworkMember te : members) {
			te.setController(this);
		}
		networkMembers = itr;
		if (consumedPerTick > POWER_CAP) {
			error = true;
			errorReason = "too_much_power";
		}
		this.consumedPerTick = consumedPerTick;
		updateDrivesCache();
		CoPo.log.info("Found "+members.size()+" network members");
	}
	
	public void checkInfiniteLoop() {
		checkingInfiniteLoop = true;
		for (TileEntityWirelessReceiver r : receivers) {
			TileEntityController cont = r.getTransmitterController();
			if (cont != null && cont.isLinkedTo(this)) {
				error = true;
				errorReason = "infinite_loop";
				receivers.clear();
				checkingInfiniteLoop = false;
				return;
			}
		}
		if (error && errorReason.equals("infinite_loop")) {
			error = false;
			errorReason = null;
		}
		checkingInfiniteLoop = false;
	}
	
	public boolean isCheckingInfiniteLoop() {
		return checkingInfiniteLoop;
	}
	
	public boolean isLinkedTo(TileEntityController tec) {
		if (tec.equals(this)) return true;
		for (TileEntityWirelessReceiver r : receivers) {
			TileEntityController cont = r.getTransmitterController();
			if (cont != null && cont.isLinkedTo(tec)) {
				return true;
			}
		}
		return false;
	}

	private void updateState() {
		if (!hasWorldObj()) return;
		if (worldObj.isRemote) return;
		State old = worldObj.getBlockState(getPos()).getValue(BlockController.state);
		State nw;
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
			worldObj.setBlockState(getPos(), worldObj.getBlockState(getPos())
					.withProperty(BlockController.state, nw));
		}
	}
	
	public boolean isPowered() {
		return energy.getEnergyStored() >= getEnergyConsumedPerTick();
	}

	/** assumes the network cache is also up to date, if it's not, call scanNetwork */
	public void updateDrivesCache() {
		if (hasWorldObj() && worldObj.isRemote) return;
		drives.clear();
		for (TileEntityDriveBay tedb : driveBays) {
			if (tedb.isInvalid()) continue;
			for (int i = 0; i < 8; i++) {
				if (tedb.hasDriveInSlot(i)) {
					drives.add(tedb.getDriveInSlot(i));
				}
			}
		}
		Collections.sort(drives, new DriveComparator());
	}

	public void updateConsumptionRate(int change) {
		consumedPerTick += change;
		if (consumedPerTick > POWER_CAP) {
			error = true;
			errorReason = "too_much_power";
		} else {
			if (error && "too_much_power".equals(errorReason)) {
				error = false;
				errorReason = null;
			}
		}
	}

	public ItemStack addItemToNetwork(ItemStack stack) {
		if (stack == null) return null;
		for (ItemStack drive : drives) {
			// both these conditions should always be true, but might as well be safe
			if (drive != null && drive.getItem() instanceof ItemDrive) {
				ItemDrive itemDrive = ((ItemDrive)drive.getItem());
				itemDrive.addItem(drive, stack);
				if (stack.stackSize <= 0) break;
			}
		}
		for (TileEntityWirelessReceiver r : receivers) {
			TileEntityController cont = r.getTransmitterController();
			if (cont != null) {
				cont.addItemToNetwork(stack);
			}
			if (stack.stackSize <= 0) break;
		}
		changeId++;
		return stack.stackSize <= 0 ? null : stack;
	}

	public ItemStack removeItemsFromNetwork(ItemStack prototype, int amount, boolean checkInterfaces) {
		if (prototype == null) return null;
		ItemStack stack = prototype.copy();
		stack.stackSize = 0;
		if (checkInterfaces) {
			for (TileEntityInterface in : interfaces) {
				for (int i = 9; i <= 17; i++) {
					ItemStack is = in.getStackInSlot(i);
					if (is != null && ItemStack.areItemsEqual(is, prototype) && ItemStack.areItemStackTagsEqual(is, prototype)) {
						int amountWanted = amount-stack.stackSize;
						int amountTaken = Math.min(is.stackSize, amountWanted);
						is.stackSize -= amountTaken;
						stack.stackSize += amountTaken;
						if (is.stackSize <= 0) {
							in.setInventorySlotContents(i, null);
						}
						if (stack.stackSize >= amount) break;
					}
				}
			}
		}
		for (ItemStack drive : drives) {
			// both these conditions should always be true, but might as well be safe
			if (drive != null && drive.getItem() instanceof ItemDrive) {
				ItemDrive itemDrive = ((ItemDrive)drive.getItem());
				int amountWanted = amount-stack.stackSize;
				itemDrive.removeItems(drive, stack, amountWanted);
				if (stack.stackSize >= amount) break;
			}
		}
		for (TileEntityWirelessReceiver r : receivers) {
			TileEntityController cont = r.getTransmitterController();
			if (cont != null) {
				ItemStack remote = cont.removeItemsFromNetwork(prototype, amount-stack.stackSize, checkInterfaces);
				if (remote != null) {
					stack.stackSize += remote.stackSize;
				}
			}
			if (stack.stackSize >= amount) break;
		}
		changeId++;
		return stack.stackSize <= 0 ? null : stack;
	}

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
				if (ifaceStack != null) {
					boolean added = false;
					for (ItemStack cur : li) {
						if (ItemStack.areItemsEqual(ifaceStack, cur) && ItemStack.areItemStackTagsEqual(ifaceStack, cur)) {
							cur.stackSize += ifaceStack.stackSize;
							added = true;
							break;
						}
					}
					if (!added) {
						li.add(ifaceStack.copy());
					}
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
		if (networkMembers == 0) return;
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
		}
		if (networkMemberLocations.add(tenm.getPos())) {
			networkMembers++;
			if (networkMembers > 100) {
				error = true;
				errorReason = "network_too_big";
				consumedPerTick = POWER_CAP;
			}
		}
	}

	public boolean knowsOfMemberAt(BlockPos pos) {
		return networkMemberLocations.contains(pos);
	}

}
