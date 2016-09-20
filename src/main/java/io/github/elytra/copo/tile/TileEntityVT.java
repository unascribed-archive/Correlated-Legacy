package io.github.elytra.copo.tile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.IVT;
import io.github.elytra.copo.block.BlockVT;
import io.github.elytra.copo.item.ItemDrive;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityVT extends TileEntityNetworkMember implements ITickable, IInventory, IVT, ISidedInventory {
	private Map<UUID, UserPreferences> preferences = Maps.newHashMap();

	@Override
	public void update() {
		if (hasWorldObj() && !worldObj.isRemote) {
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == CoPo.vt) {
				boolean lit;
				if (hasStorage() && getStorage().isPowered()) {
					lit = true;
				} else {
					lit = false;
				}
				if (lit != state.getValue(BlockVT.LIT)) {
					getWorld().setBlockState(getPos(), state = state.withProperty(BlockVT.LIT, lit));
				}
			}
			boolean floppy = getStackInSlot(1) != null;
			if (floppy != state.getValue(BlockVT.FLOPPY)) {
				getWorld().setBlockState(getPos(), state.withProperty(BlockVT.FLOPPY, floppy));
			}
			
			if (hasStorage()) {
				TileEntityController controller = getStorage();
				if (controller.isPowered() && !controller.error && !controller.booting && getDumpDrive() != null) {
					if (getDumpDrive().getItem() instanceof ItemDrive) {
						ItemDrive id = (ItemDrive)getDumpDrive().getItem();
						List<ItemStack> prototypes = id.getPrototypes(getDumpDrive());
						int moved = 0;
						while (moved < 100) {
							if (prototypes.isEmpty()) break;
							ItemStack prototype = prototypes.get(0);
							ItemStack split = id.removeItems(getDumpDrive(), prototype, prototype.getMaxStackSize());
							if (split.stackSize == 0) {
								prototypes.remove(0);
								continue;
							}
							controller.addItemToNetwork(split);
							moved += split.stackSize;
							if (split.stackSize > 0) {
								// no more room for this item in the network, skip it this tick
								prototypes.remove(0);
								moved -= split.stackSize;
								id.addItem(getDumpDrive(), split);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public int getEnergyConsumedPerTick() {
		return CoPo.inst.terminalRfUsage;
	}

	public UserPreferences getPreferences(UUID uuid) {
		if (!preferences.containsKey(uuid)) {
			preferences.put(uuid, new UserPreferences());
		}
		return preferences.get(uuid);
	}

	@Override
	public UserPreferences getPreferences(EntityPlayer player) {
		return getPreferences(player.getGameProfile().getId());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagList prefs = new NBTTagList();
		for (Map.Entry<UUID, UserPreferences> en : preferences.entrySet()) {
			UserPreferences pref = en.getValue();
			NBTTagCompound data = new NBTTagCompound();
			data.setLong("UUIDMost", en.getKey().getMostSignificantBits());
			data.setLong("UUIDLeast", en.getKey().getLeastSignificantBits());
			pref.writeToNBT(data);
			prefs.appendTag(data);
		}
		compound.setTag("Preferences", prefs);
		NBTTagList invList = new NBTTagList();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack is = getStackInSlot(i);
			if (is == null) continue;
			NBTTagCompound tag = is.writeToNBT(new NBTTagCompound());
			tag.setInteger("Slot", i);
			invList.appendTag(tag);
		}
		compound.setTag("Inventory", invList);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagList prefs = compound.getTagList("Preferences", NBT.TAG_COMPOUND);
		for (int i = 0; i < prefs.tagCount(); i++) {
			UserPreferences pref = new UserPreferences();
			NBTTagCompound data = prefs.getCompoundTagAt(i);
			pref.readFromNBT(data);
			preferences.put(new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast")), pref);
		}
		inv.clear();
		NBTTagList invList = compound.getTagList("Inventory", NBT.TAG_COMPOUND);
		for (int i = 0; i < invList.tagCount(); i++) {
			NBTTagCompound tag = invList.getCompoundTagAt(i);
			ItemStack is = ItemStack.loadItemStackFromNBT(tag);
			int slot = tag.getInteger("Slot");
			setInventorySlotContents(slot, is);
		}
	}

	public ItemStack getDumpDrive() {
		return getStackInSlot(0);
	}
	
	private InventoryBasic inv = new InventoryBasic("container.vt", false, 2);

	public void addInventoryChangeListener(IInventoryChangedListener listener) {
		inv.addInventoryChangeListener(listener);
	}

	public void removeInventoryChangeListener(IInventoryChangedListener listener) {
		inv.removeInventoryChangeListener(listener);
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return inv.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return inv.decrStackSize(index, count);
	}

	public ItemStack addItem(ItemStack stack) {
		return inv.addItem(stack);
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
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@Override
	public String getName() {
		return inv.getName();
	}

	@Override
	public boolean hasCustomName() {
		return inv.hasCustomName();
	}

	public void setCustomName(String inventoryTitleIn) {
		inv.setCustomName(inventoryTitleIn);
	}

	@Override
	public ITextComponent getDisplayName() {
		return inv.getDisplayName();
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public void markDirty() {
		inv.markDirty();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return inv.isUseableByPlayer(player);
	}

	@Override
	public void openInventory(EntityPlayer player) {
		inv.openInventory(player);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		inv.closeInventory(player);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return inv.isItemValidForSlot(index, stack);
	}

	@Override
	public int getField(int id) {
		return inv.getField(id);
	}

	@Override
	public void setField(int id, int value) {
		inv.setField(id, value);
	}

	@Override
	public int getFieldCount() {
		return inv.getFieldCount();
	}

	@Override
	public void clear() {
		inv.clear();
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean supportsDumpSlot() {
		return true;
	}

	@Override
	public IInventory getDumpSlotInventory() {
		return this;
	}

	@Override
	public boolean canContinueInteracting(EntityPlayer player) {
		return true;
	}

	@Override
	public void markUnderlyingStorageDirty() {
		markDirty();
	}
	
	

}
