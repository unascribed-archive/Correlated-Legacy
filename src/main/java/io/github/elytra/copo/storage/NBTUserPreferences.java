package io.github.elytra.copo.storage;

import com.google.common.base.Enums;

import io.github.elytra.copo.inventory.ContainerTerminal.CraftingTarget;
import io.github.elytra.copo.inventory.ContainerTerminal.SortMode;
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

}
