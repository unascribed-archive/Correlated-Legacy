package io.github.elytra.correlated.storage;

import com.google.common.base.Enums;
import com.google.common.base.Strings;

import io.github.elytra.correlated.inventory.ContainerTerminal.CraftingTarget;
import io.github.elytra.correlated.inventory.ContainerTerminal.SortMode;
import net.minecraft.nbt.NBTTagCompound;

public class SimpleUserPreferences implements UserPreferences {
	private SortMode sortMode = SortMode.QUANTITY;
	private boolean sortAscending = false;
	private String lastSearchQuery = "";
	private CraftingTarget craftingTarget = CraftingTarget.INVENTORY;
	private boolean jeiSyncEnabled = false;
	private boolean searchFocusedByDefault = false;
	
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
	
	@Override
	public boolean isJeiSyncEnabled() {
		return jeiSyncEnabled;
	}
	@Override
	public void setJeiSyncEnabled(boolean jeiSyncEnabled) {
		this.jeiSyncEnabled = jeiSyncEnabled;
	}
	
	@Override
	public boolean isSearchFocusedByDefault() {
		return searchFocusedByDefault;
	}
	@Override
	public void setSearchFocusedByDefault(boolean searchFocusedByDefault) {
		this.searchFocusedByDefault = searchFocusedByDefault;
	}
	
	public void writeToNBT(NBTTagCompound data) {
		data.setString("SortMode", getSortMode().name());
		data.setBoolean("SortAscending", isSortAscending());
		data.setString("LastSearchQuery", Strings.nullToEmpty(getLastSearchQuery()));
		data.setString("CraftingTarget", getCraftingTarget().name());
		data.setBoolean("JeiSyncEnabled", jeiSyncEnabled);
		data.setBoolean("SearchFocusedByDefault", searchFocusedByDefault);
	}
	public void readFromNBT(NBTTagCompound data) {
		setSortMode(Enums.getIfPresent(SortMode.class, data.getString("SortMode")).or(SortMode.QUANTITY));
		setSortAscending(data.getBoolean("SortAscending"));
		setLastSearchQuery(data.getString("LastSearchQuery"));
		setCraftingTarget(Enums.getIfPresent(CraftingTarget.class, data.getString("CraftingTarget")).or(CraftingTarget.INVENTORY));
		setJeiSyncEnabled(data.getBoolean("JeiSyncEnabled"));
		setSearchFocusedByDefault(data.getBoolean("SearchFocusedByDefault"));
	}
}