package io.github.elytra.copo.world;

import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.util.INBTSerializable;

public class DungeonPlayer implements INBTSerializable<NBTTagCompound> {
	private GameProfile profile;
	private int unstablePearlSlot;
	private NBTTagCompound oldPlayer;
	private long seed;
	
	public DungeonPlayer() {}
	public DungeonPlayer(GameProfile profile, int unstablePearlSlot, NBTTagCompound oldPlayer) {
		this.profile = profile;
		this.unstablePearlSlot = unstablePearlSlot;
		this.oldPlayer = oldPlayer;
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		NBTUtil.writeGameProfile(tag, profile);
		tag.setInteger("UnstablePearlSlot", unstablePearlSlot);
		tag.setTag("OldPlayer", oldPlayer);
		tag.setLong("Seed", seed);
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		profile = NBTUtil.readGameProfileFromNBT(nbt);
		unstablePearlSlot = nbt.getInteger("UnstablePearlSlot");
		oldPlayer = nbt.getCompoundTag("OldPlayer");
		seed = nbt.getLong("Seed");
	}
	
	
	public GameProfile getProfile() {
		return profile;
	}
	
	public void setProfile(GameProfile profile) {
		this.profile = profile;
	}
	
	public int getUnstablePearlSlot() {
		return unstablePearlSlot;
	}
	
	public void setUnstablePearlSlot(int unstablePearlSlot) {
		this.unstablePearlSlot = unstablePearlSlot;
	}
	
	public NBTTagCompound getOldPlayer() {
		return oldPlayer;
	}
	
	public void setOldPlayer(NBTTagCompound oldPlayer) {
		this.oldPlayer = oldPlayer;
	}
	
	public long getSeed() {
		return seed;
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
	}
	
	
}
