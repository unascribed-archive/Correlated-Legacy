package com.elytradev.correlated;

import java.util.Map;
import java.util.UUID;

import com.elytradev.correlated.wifi.WirelessManager;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

public class CorrelatedWorldData extends WorldSavedData {
	
	private World world;
	private Map<UUID, NBTTagCompound> playerRespawnData = Maps.newHashMap();
	private NBTTagCompound wireless;
	private WirelessManager wm;
	
	public CorrelatedWorldData(String name) {
		super(name);
	}
	
	public void setWorld(World world) {
		boolean changed = this.world != world;
		this.world = world;
		if (changed) {
			wm = new WirelessManager(this);
			if (wireless != null) {
				wm.deserializeNBT(wireless);
			}
		}
	}
	
	public World getWorld() {
		return world;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList re = nbt.getTagList("PlayerRespawnData", NBT.TAG_COMPOUND);
		playerRespawnData.clear();
		for (int i = 0; i < re.tagCount(); i++) {
			NBTTagCompound tag = re.getCompoundTagAt(i);
			playerRespawnData.put(tag.getUniqueId("Id"), tag.getCompoundTag("Data"));
		}
		if (wm != null) {
			wm.deserializeNBT(nbt.getCompoundTag("Wireless"));
		} else {
			wireless = nbt.getCompoundTag("Wireless");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList re = new NBTTagList();
		for (Map.Entry<UUID, NBTTagCompound> en : playerRespawnData.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setUniqueId("Id", en.getKey());
			tag.setTag("Data", en.getValue());
			re.appendTag(tag);
		}
		nbt.setTag("PlayerRespawnData", re);
		nbt.setTag("Wireless", wm != null ? wm.serializeNBT() : wireless);
		return nbt;
	}
	
	public Map<UUID, NBTTagCompound> getPlayerRespawnData() {
		return playerRespawnData;
	}
	
	public WirelessManager getWirelessManager() {
		return wm;
	}

	
	
	public static CorrelatedWorldData getFor(World w) {
		CorrelatedWorldData data = (CorrelatedWorldData)w.getPerWorldStorage().getOrLoadData(CorrelatedWorldData.class, "correlated");
		if (data == null) {
			data = new CorrelatedWorldData("correlated");
			w.getPerWorldStorage().setData("correlated", data);
		}
		data.setWorld(w);
		return data;
	}

}
