package io.github.elytra.copo;

import com.google.common.base.Enums;
import com.google.common.base.Strings;

import io.github.elytra.copo.inventory.ContainerVT.CraftingTarget;
import io.github.elytra.copo.inventory.ContainerVT.SortMode;
import io.github.elytra.copo.tile.TileEntityController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;

public interface IVT {

	public static class UserPreferences {
		public SortMode sortMode = SortMode.QUANTITY;
		public boolean sortAscending = false;
		public String lastSearchQuery = "";
		public CraftingTarget craftingTarget = CraftingTarget.INVENTORY;
		public void writeToNBT(NBTTagCompound data) {
			data.setString("SortMode", sortMode.name());
			data.setBoolean("SortAscending", sortAscending);
			data.setString("LastSearchQuery", Strings.nullToEmpty(lastSearchQuery));
			data.setString("CraftingTarget", craftingTarget.name());
		}
		public void readFromNBT(NBTTagCompound data) {
			sortMode = Enums.getIfPresent(SortMode.class, data.getString("SortMode")).or(SortMode.QUANTITY);
			sortAscending = data.getBoolean("SortAscending");
			lastSearchQuery = data.getString("LastSearchQuery");
			craftingTarget = Enums.getIfPresent(CraftingTarget.class, data.getString("CraftingTarget")).or(CraftingTarget.INVENTORY);
		}
	}
	
	UserPreferences getPreferences(EntityPlayer player);
	TileEntityController getController();
	boolean hasController();
	boolean supportsDumpSlot();
	IInventory getDumpSlotInventory();
	boolean canContinueInteracting(EntityPlayer player);
	void markUnderlyingStorageDirty();

}

