package com.elytradev.correlated.storage;

import java.util.List;

import com.elytradev.correlated.inventory.ContainerTerminal.CraftingTarget;
import com.elytradev.correlated.inventory.SortMode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

public class SimpleUserPreferences implements UserPreferences {
	private SortMode sortMode = SortMode.QUANTITY;
	private boolean sortAscending = false;
	private String lastSearchQuery = "";
	private CraftingTarget craftingTarget = CraftingTarget.INVENTORY;
	private boolean jeiSyncEnabled = false;
	private boolean searchFocusedByDefault = false;
	private List<? extends List<ItemStack>> craftingGhost = NonNullList.withSize(9, NonNullList.from(ItemStack.EMPTY));
	
	
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
	
	
	@Override
	public List<? extends List<ItemStack>> getCraftingGhost() {
		return craftingGhost;
	}
	
	@Override
	public void setCraftingGhost(List<? extends List<ItemStack>> craftingGhost) {
		this.craftingGhost = craftingGhost;
	}
	
	
	public void writeToNBT(NBTTagCompound data) {
		NBTUserPreferences nup = new NBTUserPreferences(data);
		nup.setSortMode(sortMode);
		nup.setSortAscending(sortAscending);
		nup.setLastSearchQuery(lastSearchQuery);
		nup.setCraftingTarget(craftingTarget);
		nup.setJeiSyncEnabled(jeiSyncEnabled);
		nup.setSearchFocusedByDefault(searchFocusedByDefault);
		nup.setCraftingGhost(craftingGhost);
	}
	public void readFromNBT(NBTTagCompound data) {
		NBTUserPreferences nup = new NBTUserPreferences(data);
		sortMode = nup.getSortMode();
		sortAscending = nup.isSortAscending();
		lastSearchQuery = nup.getLastSearchQuery();
		craftingTarget = nup.getCraftingTarget();
		jeiSyncEnabled = nup.isJeiSyncEnabled();
		searchFocusedByDefault = nup.isSearchFocusedByDefault();
		craftingGhost = nup.getCraftingGhost();
	}
}