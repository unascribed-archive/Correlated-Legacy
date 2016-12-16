package io.github.elytra.correlated.storage;

import io.github.elytra.correlated.inventory.ContainerTerminal.CraftingTarget;
import io.github.elytra.correlated.inventory.ContainerTerminal.SortMode;

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
}