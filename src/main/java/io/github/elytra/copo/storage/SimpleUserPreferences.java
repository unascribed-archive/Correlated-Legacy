package io.github.elytra.copo.storage;

import com.google.common.base.Enums;
import com.google.common.base.Strings;

import io.github.elytra.copo.inventory.ContainerTerminal.CraftingTarget;
import io.github.elytra.copo.inventory.ContainerTerminal.SortMode;
import net.minecraft.nbt.NBTTagCompound;

public class SimpleUserPreferences implements UserPreferences {
	private SortMode sortMode = SortMode.QUANTITY;
	private boolean sortAscending = false;
	private String lastSearchQuery = "";
	private CraftingTarget craftingTarget = CraftingTarget.INVENTORY;
	@Override
	public SortMode getSortMode() {
		return sortMode;
	}
	@Override
	public void setSortMode(SortMode sortMode) {
		this.sortMode = sortMode;
	}
	@Override
	public boolean isSortAscending() {
		return sortAscending;
	}
	@Override
	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}
	@Override
	public String getLastSearchQuery() {
		return lastSearchQuery;
	}
	@Override
	public void setLastSearchQuery(String lastSearchQuery) {
		this.lastSearchQuery = lastSearchQuery;
	}
	@Override
	public CraftingTarget getCraftingTarget() {
		return craftingTarget;
	}
	@Override
	public void setCraftingTarget(CraftingTarget craftingTarget) {
		this.craftingTarget = craftingTarget;
	}
	
	public void writeToNBT(NBTTagCompound data) {
		data.setString("SortMode", getSortMode().name());
		data.setBoolean("SortAscending", isSortAscending());
		data.setString("LastSearchQuery", Strings.nullToEmpty(getLastSearchQuery()));
		data.setString("CraftingTarget", getCraftingTarget().name());
	}
	public void readFromNBT(NBTTagCompound data) {
		setSortMode(Enums.getIfPresent(SortMode.class, data.getString("SortMode")).or(SortMode.QUANTITY));
		setSortAscending(data.getBoolean("SortAscending"));
		setLastSearchQuery(data.getString("LastSearchQuery"));
		setCraftingTarget(Enums.getIfPresent(CraftingTarget.class, data.getString("CraftingTarget")).or(CraftingTarget.INVENTORY));
	}
}