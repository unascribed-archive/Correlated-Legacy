package io.github.elytra.copo.storage;

import io.github.elytra.copo.inventory.ContainerTerminal.CraftingTarget;
import io.github.elytra.copo.inventory.ContainerTerminal.SortMode;

public interface UserPreferences {
	SortMode getSortMode();
	void setSortMode(SortMode sortMode);
	boolean isSortAscending();
	void setSortAscending(boolean sortAscending);
	String getLastSearchQuery();
	void setLastSearchQuery(String lastSearchQuery);
	CraftingTarget getCraftingTarget();
	void setCraftingTarget(CraftingTarget craftingTarget);
}