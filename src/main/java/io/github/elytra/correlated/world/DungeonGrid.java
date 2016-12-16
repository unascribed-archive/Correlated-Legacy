package io.github.elytra.correlated.world;

import java.util.Map;

import com.google.common.collect.Maps;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.math.Vec2i;
import io.github.elytra.hallways.Cardinal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;

public class DungeonGrid implements INBTSerializable<NBTTagCompound> {
	private Map<Vec2i, Dungeon> dungeons = Maps.newHashMap();
	private TLongObjectMap<Dungeon> dungeonsBySeed = new TLongObjectHashMap<>();
	
	public DungeonGrid() {}

	public Dungeon get(Vec2i vec) {
		Dungeon d = dungeons.get(vec);
		if (d != null) {
			d.x = vec.x;
			d.z = vec.y;
		}
		return d;
	}
	
	public Dungeon get(int x, int z) {
		return get(new Vec2i(x, z));
	}
	
	public Dungeon getFromBlock(BlockPos pos) {
		return getFromBlock(pos.getX(), pos.getZ());
	}
	
	public Dungeon getFromBlock(int x, int z) {
		return get((x/Dungeon.NODE_SIZE)/Dungeon.DUNGEON_SIZE, (z/Dungeon.NODE_SIZE)/Dungeon.DUNGEON_SIZE);
	}
	
	public Dungeon getFromChunk(ChunkPos pos) {
		return getFromChunk(pos.chunkXPos, pos.chunkZPos);
	}
	
	public Dungeon getFromChunk(int x, int z) {
		return getFromBlock(x*16, z*16);
	}
	
	public void set(int x, int z, Dungeon d) {
		dungeons.put(new Vec2i(x, z), d);
		if (dungeonsBySeed.containsKey(d.getSeed()) && d != dungeonsBySeed.get(d.getSeed())) {
			Correlated.log.warn("Adding second dungeon with the same seed (orig: {}, new: {})", dungeonsBySeed.get(d.getSeed()), d);
		}
		dungeonsBySeed.put(d.getSeed(), d);
		d.x = x;
		d.z = z;
	}
	
	public void set(Vec2i vec, Dungeon d) {
		set(vec.x, vec.y, d);
		d.x = vec.x;
		d.z = vec.y;
	}
	
	public Vec2i findFreeSpot() {
		int x = 0;
		int z = 0;
		Cardinal dir = Cardinal.WEST;
		int legLength = 0;
		int i = 0;
		int j = 0;
		// scan in a counterclockwise outward spiral from 0, 0
		// i.e. find the closest point to 0, 0 that is free or can be freed
		while (true) {
			Dungeon cur = get(x, z);
			if (cur == null || cur.canBeFreed()) {
				return new Vec2i(x, z);
			}
			if (i >= legLength) {
				dir = dir.ccw();
				i = 0;
				j++;
				if (j % 2 == 0) {
					legLength++;
				}
			}
			x += dir.xOfs();
			z += dir.yOfs();
			i++;
		}
	}
	
	public Dungeon getBySeed(long seed) {
		return dungeonsBySeed.get(seed);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagList li = new NBTTagList();
		for (Map.Entry<Vec2i, Dungeon> en : dungeons.entrySet()) {
			NBTTagCompound entry = en.getValue().serializeNBT();
			entry.setInteger("X", en.getKey().x);
			entry.setInteger("Z", en.getKey().y);
			li.appendTag(entry);
		}
		tag.setTag("Dungeons", li);
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		dungeons.clear();
		NBTTagList li = nbt.getTagList("Dungeons", NBT.TAG_COMPOUND);
		for (int i = 0; i < li.tagCount(); i++) {
			NBTTagCompound entry = li.getCompoundTagAt(i);
			Dungeon d = new Dungeon();
			d.deserializeNBT(entry);
			dungeons.put(new Vec2i(entry.getInteger("X"), entry.getInteger("Z")), d);
			dungeonsBySeed.put(d.getSeed(), d);
		}
	}

}
