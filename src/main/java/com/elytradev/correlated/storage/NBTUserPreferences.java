package com.elytradev.correlated.storage;

import com.elytradev.correlated.inventory.ContainerTerminal.CraftingTarget;
import com.elytradev.correlated.inventory.SortMode;
import com.google.common.base.Enums;

import net.minecraft.nbt.NBTTagCompound;

public class NBTUserPreferences implements UserPreferences {
	private final NBTTagCompound tag;
	public NBTUserPreferences(NBTTagCompound tag) {
		this.tag = tag;
	}
	
	@Override
	public SortMode getSortMode() {
		return Enums.getIfPresent(SortMode.class, tag.getString("SortMode")).or(SortMode.QUANTITY);
	}
	@Override
	public void setSortMode(SortMode sortMode) {
		tag.setString("SortMode", sortMode.name());
	}

	@Override
	public boolean isSortAscending() {
		return tag.getBoolean("SortAscending");
	}
	@Override
	public void setSortAscending(boolean sortAscending) {
		tag.setBoolean("SortAscending", sortAscending);
	}

	@Override
	public String getLastSearchQuery() {
		return tag.getString("LastSearchQuery");
	}
	@Override
	public void setLastSearchQuery(String lastSearchQuery) {
		tag.setString("LastSearchQuery", lastSearchQuery);
	}

	@Override
	public CraftingTarget getCraftingTarget() {
		return Enums.getIfPresent(CraftingTarget.class, tag.getString("CraftingTarget")).or(CraftingTarget.INVENTORY);
	}
	@Override
	public void setCraftingTarget(CraftingTarget craftingTarget) {
		tag.setString("CraftingTarget", craftingTarget.name());
	}
	
	@Override
	public boolean isJeiSyncEnabled() {
		return tag.getBoolean("JeiSyncEnabled");
	}
	@Override
	public void setJeiSyncEnabled(boolean jeiSyncEnabled) {
		tag.setBoolean("JeiSyncEnabled", jeiSyncEnabled);
	}
	
	@Override
	public boolean isSearchFocusedByDefault() {
		return tag.getBoolean("SearchFocusedByDefault");
	}
	@Override
	public void setSearchFocusedByDefault(boolean searchFocusedByDefault) {
		tag.setBoolean("SearchFocusedByDefault", searchFocusedByDefault);
	}

}
