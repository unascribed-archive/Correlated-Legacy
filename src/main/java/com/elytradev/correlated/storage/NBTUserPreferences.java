package com.elytradev.correlated.storage;

import com.elytradev.correlated.inventory.ContainerTerminal.CraftingTarget;

import java.util.List;

import com.elytradev.correlated.inventory.SortMode;
import com.google.common.base.Enums;
import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;

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
	
	
	@Override
	public List<List<ItemStack>> getCraftingGhost() {
		List<List<ItemStack>> rtrn = NonNullList.withSize(9, NonNullList.from(ItemStack.EMPTY));
		for (int i = 0; i < 9; i++) {
			if (!tag.hasKey("Ghost"+i, NBT.TAG_LIST)) continue;
			NBTTagList li = tag.getTagList("Ghost"+i, NBT.TAG_COMPOUND);
			List<ItemStack> out = Lists.newArrayList();
			for (int j = 0; j < li.tagCount(); j++) {
				out.add(new ItemStack(li.getCompoundTagAt(j)));
			}
		}
		return rtrn;
	}
	
	@Override
	public void setCraftingGhost(List<? extends List<ItemStack>> craftingGhost) {
		for (int i = 0; i < 9; i++) {
			if (craftingGhost.get(i).isEmpty()) {
				tag.removeTag("Ghost"+i);
			} else {
				NBTTagList out = new NBTTagList();
				List<ItemStack> li = craftingGhost.get(i);
				for (ItemStack is : li) {
					out.appendTag(is.serializeNBT());
				}
				tag.setTag("Ghost"+i, out);
			}
		}
	}

}
