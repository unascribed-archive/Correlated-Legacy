package com.elytradev.correlated.tile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.elytradev.correlated.Correlated;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import com.elytradev.probe.api.impl.ProbeData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

// I hate this entire class.
public class TileEntityInterface extends TileEntityNetworkMember implements IInventory, ISidedInventory, ITickable {
	public enum FaceMode implements IStringSerializable {
		PASSIVE,
		ACTIVE_PULL,
		ACTIVE_PUSH,
		DISABLED;
		public boolean canInsert() {
			return this == FaceMode.PASSIVE || this == ACTIVE_PULL;
		}

		public boolean canExtract() {
			return this == FaceMode.PASSIVE || this == ACTIVE_PUSH;
		}

		@Override
		public String getName() {
			switch (this) {
				case ACTIVE_PULL: return "pull";
				case ACTIVE_PUSH: return "push";
				case PASSIVE: return "none";
				case DISABLED: return "disabled";
			}
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}
	}
	private InventoryBasic inv = new InventoryBasic("container.interface", false, 18);
	private ItemStack[] prototypes = new ItemStack[9];
	private InvWrapper selfInvWrapper = new InvWrapper(this);

	private FaceMode[] modes = new FaceMode[6];

	public TileEntityInterface() {
		Arrays.fill(prototypes, ItemStack.EMPTY);
	}
	
	public FaceMode getModeForFace(EnumFacing face) {
		if (face == null) return null;
		FaceMode mode = modes[face.ordinal()];
		if (mode == null) {
			modes[face.ordinal()] = FaceMode.PASSIVE;
			mode = FaceMode.PASSIVE;
			markDirty();
		}
		return mode;
	}

	public void setModeForFace(EnumFacing face, FaceMode mode) {
		if (face == null) return;
		modes[face.ordinal()] = mode;
		markDirty();
	}

	public ItemStack getOutputPrototype(int i) {
		return prototypes[i];
	}

	public void setOutputPrototype(int i, ItemStack prototype) {
		prototypes[i] = prototype;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		writeFacesToNBT(compound);
		NBTTagList inv = new NBTTagList();
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is = getStackInSlot(i);
			if (!is.isEmpty()) {
				NBTTagCompound nbt = is.writeToNBT(new NBTTagCompound());
				nbt.setInteger("Slot", i);
				inv.appendTag(nbt);
			}
		}
		compound.setTag("Buffer", inv);
		NBTTagList proto = new NBTTagList();
		for (int i = 0; i < prototypes.length; i++) {
			ItemStack is = prototypes[i];
			if (!is.isEmpty()) {
				NBTTagCompound nbt = is.writeToNBT(new NBTTagCompound());
				nbt.setInteger("Slot", i);
				nbt.removeTag("Count");
				proto.appendTag(nbt);
			}
		}
		compound.setTag("Prototypes", proto);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		readFacesFromNBT(compound);
		NBTTagList inv = compound.getTagList("Buffer", NBT.TAG_COMPOUND);
		for (int i = 0; i < inv.tagCount(); i++) {
			NBTTagCompound nbt = inv.getCompoundTagAt(i);
			setInventorySlotContents(nbt.getInteger("Slot"), new ItemStack(nbt));
		}
		NBTTagList proto = compound.getTagList("Prototypes", NBT.TAG_COMPOUND);
		for (int i = 0; i < proto.tagCount(); i++) {
			NBTTagCompound nbt = proto.getCompoundTagAt(i);
			nbt.setInteger("Count", 1);
			prototypes[nbt.getInteger("Slot")] = new ItemStack(nbt);
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("x", getPos().getX());
		nbt.setInteger("y", getPos().getY());
		nbt.setInteger("z", getPos().getZ());
		writeFacesToNBT(nbt);
		return nbt;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		readFacesFromNBT(pkt.getNbtCompound());
		world.markBlockRangeForRenderUpdate(getPos(), getPos());
	}

	private void writeFacesToNBT(NBTTagCompound nbt) {
		for (EnumFacing face : EnumFacing.VALUES) {
			nbt.setString("Mode-"+face.getName(), getModeForFace(face).name());
		}
	}

	private void readFacesFromNBT(NBTTagCompound nbt) {
		for (EnumFacing face : EnumFacing.VALUES) {
			setModeForFace(face, Enums.getIfPresent(FaceMode.class, nbt.getString("Mode-"+face.getName())).or(FaceMode.PASSIVE));
		}
	}

	@Override
	public void update() {
		if (hasStorage() && hasWorld() && !world.isRemote && world.getTotalWorldTime() % 16 == 0) {
			TileEntityController controller = getStorage();
			if (!controller.isPowered() || controller.booting || controller.error) return;
			for (int i = 0; i <= 8; i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (!stack.isEmpty()) {
					inv.setInventorySlotContents(i, controller.addItemToNetwork(stack));
				}
			}
			for (int i = 9; i <= 17; i++) {
				ItemStack prototype = prototypes[i-9];
				if (!prototype.isEmpty()) {
					ItemStack cur = inv.getStackInSlot(i);
					int needed;
					if (cur.isEmpty()) {
						needed = prototype.getMaxStackSize();
						cur = prototype.copy();
						cur.setCount(0);
					} else {
						if (!ItemStack.areItemsEqual(cur, prototype) || !ItemStack.areItemStackTagsEqual(cur, prototype)) {
							continue;
						}
						needed = prototype.getMaxStackSize()-cur.getCount();
					}
					if (needed > 0) {
						ItemStack stack = controller.removeItemsFromNetwork(prototype, needed, false);
						cur.setCount(cur.getCount() + stack.getCount());
						inv.setInventorySlotContents(i, cur);
					}
				}
			}
			for (EnumFacing face : EnumFacing.VALUES) {
				FaceMode mode = getModeForFace(face);
				if (mode == FaceMode.DISABLED || mode == FaceMode.PASSIVE) continue;
				TileEntity other = world.getTileEntity(getPos().offset(face));
				if (other == null) continue;
				IItemHandler inv;
				if (other.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite())) {
					inv = other.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
				} else {
					if (other instanceof ISidedInventory) {
						inv = new SidedInvWrapper((ISidedInventory)other, face.getOpposite());
					} else if (other instanceof IInventory) {
						inv = new InvWrapper((IInventory)other);
					} else {
						continue;
					}
				}
				if (mode == FaceMode.ACTIVE_PUSH) {
					for (int i = 9; i < 18; i++) {
						ItemStack content = getStackInSlot(i);
						if (!content.isEmpty()) {
							int slot = findSlot(inv, content, 0, inv.getSlots());
							if (slot != -1) {
								transfer(selfInvWrapper, i, inv, slot);
							}
						}
					}
				} else if (mode == FaceMode.ACTIVE_PULL) {
					for (int s = 0; s < inv.getSlots(); s++) {
						ItemStack content = inv.getStackInSlot(s);
						if (!content.isEmpty()) {
							int slot = findSlot(selfInvWrapper, content, 0, 9);
							if (slot != -1) {
								transfer(inv, s, selfInvWrapper, slot);
							}
						}
					}
				}
			}
		}
	}

	private static void transfer(IItemHandler fromInv, int fromSlot, IItemHandler toInv, int toSlot) {
		ItemStack available = fromInv.extractItem(fromSlot, fromInv.getSlotLimit(fromSlot), true);
		ItemStack existing = toInv.insertItem(toSlot, available, true);
		int toTake = available.getCount()-existing.getCount();
		ItemStack remaining = toInv.insertItem(toSlot, fromInv.extractItem(fromSlot, toTake, false), false);
		if (!remaining.isEmpty()) {
			Correlated.log.warn("Accidentally disappeared {} items into the ether", remaining.getCount());
		}
	}

	private static int findSlot(IItemHandler inv, ItemStack a, int start, int end) {
		for (int i = start; i < end; i++) {
			ItemStack b = inv.getStackInSlot(i);
			if (b.isEmpty()) {
				return i;
			} else if (b.getCount() < b.getMaxStackSize() && b.getCount() < inv.getSlotLimit(i)
					&& ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int getEnergyConsumedPerTick() {
		return Correlated.inst.interfaceRfUsage;
	}

	@Override
	public String getName() {
		return "container.correlated.interface";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentTranslation(getName());
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		if (getModeForFace(side) == FaceMode.DISABLED) return new int[0];
		if (getModeForFace(side) == FaceMode.ACTIVE_PULL) return new int[] {
				0, 1, 2, 3, 4, 5, 6, 7, 8
		};
		if (getModeForFace(side) == FaceMode.ACTIVE_PUSH) return new int[] {
				9, 10, 11, 12, 13, 14, 15, 16, 17
		};
		return new int[] {
				0,  1,  2,  3,  4,  5,  6,  7,  8, // in
				9, 10, 11, 12, 13, 14, 15, 16, 17 // out
		};
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return getModeForFace(direction).canInsert() && index >= 0 && index <= 8;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return getModeForFace(direction).canExtract() && index >= 9 && index <= 17;
	}

	@Override
	public int getSizeInventory() {
		return 18;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return inv.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return inv.decrStackSize(index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return inv.removeStackFromSlot(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		inv.setInventorySlotContents(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index >= 9) return false;
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		inv.clear();
	}

	@Override
	public boolean isEmpty() {
		return inv.isEmpty();
	}
	
	private Map<EnumFacing, Object> probeCapability = Maps.newEnumMap(EnumFacing.class);
	private Object facelessProbeCapability;
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == null) return null;
		if (capability == Correlated.PROBE) {
			if (facing == null) {
				if (facelessProbeCapability == null) facelessProbeCapability = new ProbeCapability(null);
				return (T)facelessProbeCapability;
			} else {
				if (!probeCapability.containsKey(facing)) probeCapability.put(facing, new ProbeCapability(facing));
				return (T)probeCapability.get(facing);
			}
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
		private final EnumFacing facing;
		public ProbeCapability(EnumFacing facing) {
			this.facing = facing;
		}

		@Override
		public void provideProbeData(List<IProbeData> data) {
			if (facing != null) {
				data.add(new ProbeData()
						.withLabel(new TextComponentTranslation("tooltip.correlated.mode", new TextComponentTranslation("tooltip.correlated.iface.mode_"+getModeForFace(facing).getName()))));
				data.add(new ProbeData()
						.withLabel(new TextComponentTranslation("tooltip.correlated.side", new TextComponentTranslation("direction.correlated."+facing.getName()))));
			}
			ItemStack[] input = new ItemStack[9];
			ItemStack[] output = new ItemStack[9];
			for (int i = 0; i < 9; i++) {
				input[i] = inv.getStackInSlot(i);
			}
			for (int i = 0; i < 9; i++) {
				output[i] = inv.getStackInSlot(i+9);
			}
			data.add(new ProbeData()
					.withLabel(new TextComponentTranslation("tooltip.correlated.input"))
					.withInventory(ImmutableList.copyOf(input)));
			data.add(new ProbeData()
					.withLabel(new TextComponentTranslation("tooltip.correlated.output"))
					.withInventory(ImmutableList.copyOf(output)));
		}
	}

}
