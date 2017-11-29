package com.elytradev.correlated.storage;

import java.util.List;

import com.elytradev.correlated.inventory.ContainerTerminal.CraftingTarget;
import com.elytradev.correlated.inventory.SortMode;

import net.minecraft.item.ItemStack;

public interface UserPreferences {
	SortMode getSortMode();
	void setSortMode(SortMode sortMode);
	
	boolean isSortAscending();
	void setSortAscending(boolean sortAscending);
	
	String getLastSearchQuery();
	void setLastSearchQuery(String lastSearchQuery);
	
	CraftingTarget getCraftingTarget();
	void setCraftingTarget(CraftingTarget craftingTarget);
	
	boolean isJeiSyncEnabled();
	void setJeiSyncEnabled(boolean jeiSyncEnabled);
	
	boolean isSearchFocusedByDefault();
	void setSearchFocusedByDefault(boolean searchFocusedByDefault);
	
	List<? extends List<ItemStack>> getCraftingGhost();
	void setCraftingGhost(List<? extends List<ItemStack>> craftingGhost);
}