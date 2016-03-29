package com.unascribed.correlatedpotentialistics.tile;

import java.util.Map;
import java.util.UUID;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT.CraftingTarget;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT.SortMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityVT extends TileEntityNetworkMember {
	public class UserPreferences {
		public SortMode sortMode = SortMode.QUANTITY;
		public boolean sortAscending = false;
		public String lastSearchQuery = "";
		public CraftingTarget craftingTarget = CraftingTarget.INVENTORY;
	}
	private Map<UUID, UserPreferences> preferences = Maps.newHashMap();
	
	@Override
	public int getEnergyConsumedPerTick() {
		return 4;
	}
	
	public UserPreferences getPreferences(UUID uuid) {
		if (!preferences.containsKey(uuid)) {
			preferences.put(uuid, new UserPreferences());
		}
		return preferences.get(uuid);
	}
	
	public UserPreferences getPreferences(EntityPlayer player) {
		return getPreferences(player.getGameProfile().getId());
	}
	
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagList prefs = new NBTTagList();
		for (Map.Entry<UUID, UserPreferences> en : preferences.entrySet()) {
			UserPreferences pref = en.getValue();
			NBTTagCompound data = new NBTTagCompound();
			data.setLong("UUIDMost", en.getKey().getMostSignificantBits());
			data.setLong("UUIDLeast", en.getKey().getLeastSignificantBits());
			data.setString("SortMode", pref.sortMode.name());
			data.setBoolean("SortAscending", pref.sortAscending);
			data.setString("LastSearchQuery", Strings.nullToEmpty(pref.lastSearchQuery));
			data.setString("CraftingTarget", pref.craftingTarget.name());
			prefs.appendTag(data);
		}
		compound.setTag("Preferences", prefs);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagList prefs = compound.getTagList("Preferences", NBT.TAG_COMPOUND);
		for (int i = 0; i < prefs.tagCount(); i++) {
			UserPreferences pref = new UserPreferences();
			NBTTagCompound data = prefs.getCompoundTagAt(i);
			pref.sortMode = Enums.getIfPresent(SortMode.class, data.getString("SortMode")).or(SortMode.QUANTITY);
			pref.sortAscending = data.getBoolean("SortAscending");
			pref.lastSearchQuery = data.getString("LastSearchQuery");
			pref.craftingTarget = Enums.getIfPresent(CraftingTarget.class, data.getString("CraftingTarget")).or(CraftingTarget.INVENTORY);
			preferences.put(new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast")), pref);
		}
	}

}
