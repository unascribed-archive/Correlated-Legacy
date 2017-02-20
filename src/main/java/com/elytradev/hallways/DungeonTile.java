package com.elytradev.hallways;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class DungeonTile implements INBTSerializable<NBTTagCompound>, Cloneable {
	public enum TileType {
		HALLWAY(0x00FF00),
		ROOM(0x0000FF),
		ENTRANCE(0xFFFF88),
		EXIT(0xFF00FF);
		public final int color;
		private TileType(int color) {
			this.color = color;
		}
	}

	public TileType type;
	public EnumSet<Cardinal> exits;
	
	public DungeonTile(TileType type) {
		this.type = type;
		this.exits = EnumSet.noneOf(Cardinal.class);
	}
	
	@Override
	public DungeonTile clone() {
		DungeonTile result = new DungeonTile(type);
		result.exits = EnumSet.copyOf(exits);
		return result;
	}
	
	public EnumSet<Cardinal> exits() { return exits; }
	
	public void clearExits() {
		exits.clear();
	}
	
	public void setExits(Cardinal... cardinals) {
		exits.clear();
		for(Cardinal cardinal : cardinals) {
			exits.add(cardinal);
		}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		int i = 0;
		for (Cardinal c : exits) {
			i |= (1 << c.ordinal());
		}
		tag.setByte("Exits", (byte)i);
		tag.setByte("Type", (byte)type.ordinal());
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		exits.clear();
		int exitsSet = nbt.getByte("Exits")&0xFF;
		for (Cardinal c : Cardinal.values()) {
			if ((exitsSet & (1 << c.ordinal())) != 0) {
				exits.add(c);
			}
		}
		type = TileType.values()[nbt.getInteger("Type")];
	}
}
