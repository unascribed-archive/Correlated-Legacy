package com.unascribed.correlatedpotentialistics.tile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.block.BlockVT;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT.CraftingTarget;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT.SortMode;
import com.unascribed.correlatedpotentialistics.item.ItemDrive;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityVT extends TileEntityNetworkMember implements ITickable, IInventory {
	public class UserPreferences {
		public SortMode sortMode = SortMode.QUANTITY;
		public boolean sortAscending = false;
		public String lastSearchQuery = "";
		public CraftingTarget craftingTarget = CraftingTarget.INVENTORY;
	}
	private Map<UUID, UserPreferences> preferences = Maps.newHashMap();

	@Override
	public void update() {
		if (hasWorldObj() && !worldObj.isRemote) {
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == CoPo.vt) {
				boolean lit;
				if (hasController() && getController().isPowered()) {
					lit = true;
				} else {
					lit = false;
				}
				if (lit != state.getValue(BlockVT.lit)) {
					getWorld().setBlockState(getPos(), state.withProperty(BlockVT.lit, lit));
				}
			}
			
			if (hasController()) {
				TileEntityController controller = getController();
				if (controller.isPowered() && !controller.error && !controller.booting && dumpDrive != null) {
					if (dumpDrive.getItem() instanceof ItemDrive) {
						ItemDrive id = (ItemDrive)dumpDrive.getItem();
						List<ItemStack> prototypes = id.getPrototypes(dumpDrive);
						int moved = 0;
						while (moved < 100) {
							if (prototypes.isEmpty()) break;
							ItemStack prototype = prototypes.get(0);
							ItemStack split = id.removeItems(dumpDrive, prototype, prototype.getMaxStackSize());
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
								id.addItem(dumpDrive, split);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public int getEnergyConsumedPerTick() {
		return 4;
	}

	public UserPreferences getPreferences(UUID uuid) {
		if (!preferences.containsKey(uuid)) {
			preferences.put(uuid, new UserPreferences());
		}
		return preferences.get(uuid);
	}

	public UserPreferences getPreferences(EntityPlayer player) {
		return getPreferences(player.getGameProfile().getId());
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagList prefs = new NBTTagList();
		for (Map.Entry<UUID, UserPreferences> en : preferences.entrySet()) {
			UserPreferences pref = en.getValue();
			NBTTagCompound data = new NBTTagCompound();
			data.setLong("UUIDMost", en.getKey().getMostSignificantBits());
			data.setLong("UUIDLeast", en.getKey().getLeastSignificantBits());
			data.setString("SortMode", pref.sortMode.name());
			data.setBoolean("SortAscending", pref.sortAscending);
			data.setString("LastSearchQuery", Strings.nullToEmpty(pref.lastSearchQuery));
			data.setString("CraftingTarget", pref.craftingTarget.name());
			prefs.appendTag(data);
		}
		compound.setTag("Preferences", prefs);
		if (dumpDrive != null) {
			compound.setTag("DumpDrive", dumpDrive.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("DumpDrive", NBT.TAG_COMPOUND)) {
			dumpDrive = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("DumpDrive"));
		} else {
			dumpDrive = null;
		}
		NBTTagList prefs = compound.getTagList("Preferences", NBT.TAG_COMPOUND);
		for (int i = 0; i < prefs.tagCount(); i++) {
			UserPreferences pref = new UserPreferences();
			NBTTagCompound data = prefs.getCompoundTagAt(i);
			pref.sortMode = Enums.getIfPresent(SortMode.class, data.getString("SortMode")).or(SortMode.QUANTITY);
			pref.sortAscending = data.getBoolean("SortAscending");
			pref.lastSearchQuery = data.getString("LastSearchQuery");
			pref.craftingTarget = Enums.getIfPresent(CraftingTarget.class, data.getString("CraftingTarget")).or(CraftingTarget.INVENTORY);
			preferences.put(new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast")), pref);
		}
	}

	private ItemStack dumpDrive;
	
	@Override
	public String getName() {
		return "container.vt";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public IChatComponent getDisplayName() {
		return null;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index != 0) throw new IndexOutOfBoundsException();
		return dumpDrive;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (index != 0) throw new IndexOutOfBoundsException();
		return dumpDrive.splitStack(count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (index != 0) throw new IndexOutOfBoundsException();
		ItemStack swp = dumpDrive;
		dumpDrive = null;
		return swp;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index != 0) throw new IndexOutOfBoundsException();
		dumpDrive = stack;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index != 0) throw new IndexOutOfBoundsException();
		return stack != null && stack.getItem() instanceof ItemDrive;
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
		dumpDrive = null;
	}

}
