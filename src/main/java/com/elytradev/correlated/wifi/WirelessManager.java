package com.elytradev.correlated.wifi;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.math.Vec2i;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;

public class WirelessManager implements INBTSerializable<NBTTagCompound> {
	
	private final CorrelatedWorldData data;
	
	private final Multimap<Vec2i, Optical> opticalsByChunk = HashMultimap.create();
	private final Multimap<Vec2i, Tower> towersByChunk = HashMultimap.create();
	private final Multimap<Vec2i, Beam> beamsByChunk = HashMultimap.create();
	
	private final Map<BlockPos, Beam> beamsByEnd = Maps.newHashMap();
	
	private final Vec2i goat = new Vec2i(0, 0);
	
	private boolean loading = false;
	
	
	public WirelessManager(CorrelatedWorldData data) {
		this.data = data;
	}
	
	public void add(Tower t) {
		if (t == null) return;
		for (Vec2i chunk : t.chunks()) {
			towersByChunk.put(chunk, t);
		}
		if (!loading) data.markDirty();
	}
	
	public void remove(Tower t) {
		if (t == null) return;
		for (Vec2i chunk : t.chunks()) {
			towersByChunk.remove(chunk, t);
		}
		if (!loading) data.markDirty();
	}
	
	
	public void add(Optical o) {
		if (o == null) return;
		for (Vec2i chunk : o.chunks()) {
			opticalsByChunk.put(chunk, o);
		}
		if (!loading) data.markDirty();
	}
	
	public void remove(Optical o) {
		if (o == null) return;
		for (Vec2i chunk : o.chunks()) {
			opticalsByChunk.remove(chunk, o);
		}
		if (!loading) data.markDirty();
	}
	
	
	public void add(Beam b) {
		if (b == null) return;
		System.out.println("Add beam "+b.getStart().getX()+", "+b.getStart().getY()+", "+b.getStart().getZ()+" - "+b.getEnd().getX()+", "+b.getEnd().getY()+", "+b.getEnd().getZ());
		if (data.getWorld().getBlockState(b.getStart()).getBlock() != Correlated.microwave_beam) return;
		if (data.getWorld().getBlockState(b.getEnd()).getBlock() != Correlated.microwave_beam) return;
		System.out.println("Start and end look good, continue");
		for (Vec2i chunk : b.chunks()) {
			beamsByChunk.put(chunk, b);
		}
		beamsByEnd.put(b.getStart(), b);
		beamsByEnd.put(b.getEnd(), b);
		if (!loading) data.markDirty();
	}
	
	public void remove(Beam b) {
		if (b == null) return;
		for (Vec2i chunk : b.chunks()) {
			beamsByChunk.remove(chunk, b);
		}
		beamsByEnd.remove(b.getStart());
		beamsByEnd.remove(b.getEnd());
		if (!loading) data.markDirty();
	}
	
	
	public Iterable<Optical> allOpticalsInChunk(Chunk c) {
		if (c == null || c.getWorld() != data.getWorld()) return Collections.emptySet();
		goat.x = c.xPosition;
		goat.y = c.zPosition;
		return opticalsByChunk.get(goat);
	}
	
	public Iterable<Tower> allTowersInChunk(Chunk c) {
		if (c == null || c.getWorld() != data.getWorld()) return Collections.emptySet();
		goat.x = c.xPosition;
		goat.y = c.zPosition;
		return towersByChunk.get(goat);
	}
	
	public Iterable<Beam> allBeamsInChunk(Chunk c) {
		if (c == null || c.getWorld() != data.getWorld()) return Collections.emptySet();
		goat.x = c.xPosition;
		goat.y = c.zPosition;
		return beamsByChunk.get(goat);
	}
	
	public Iterable<Station> allStationsInChunk(Chunk c) {
		return Iterables.concat(allTowersInChunk(c), allOpticalsInChunk(c));
	}
	
	public Beam getBeam(BlockPos end) {
		return beamsByEnd.get(end);
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		Set<Object> serialized = Sets.newHashSet();
		NBTTagList optical = new NBTTagList();
		for (Optical o : opticalsByChunk.values()) {
			if (!serialized.add(o)) continue;
			optical.appendTag(o.serializeNBT());
		}
		nbt.setTag("Optical", optical);
		NBTTagList tower = new NBTTagList();
		for (Tower t : towersByChunk.values()) {
			if (!serialized.add(t)) continue;
			tower.appendTag(t.serializeNBT());
		}
		nbt.setTag("Tower", tower);
		NBTTagList beam = new NBTTagList();
		for (Beam b : beamsByChunk.values()) {
			if (!serialized.add(b)) continue;
			beam.appendTag(b.serializeNBT());
		}
		nbt.setTag("Beam", beam);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		try {
			loading = true;
			opticalsByChunk.clear();
			towersByChunk.clear();
			beamsByChunk.clear();
			NBTTagList optical = nbt.getTagList("Optical", NBT.TAG_COMPOUND);
			for (int i = 0; i < optical.tagCount(); i++) {
				Optical o = new Optical();
				o.deserializeNBT(optical.getCompoundTagAt(i));
				add(o);
			}
			NBTTagList tower = nbt.getTagList("Tower", NBT.TAG_COMPOUND);
			for (int i = 0; i < tower.tagCount(); i++) {
				Tower t = new Tower();
				t.deserializeNBT(tower.getCompoundTagAt(i));
				add(t);
			}
			NBTTagList beam = nbt.getTagList("Beam", NBT.TAG_COMPOUND);
			for (int i = 0; i < beam.tagCount(); i++) {
				Beam b = new Beam(data);
				b.deserializeNBT(beam.getCompoundTagAt(i));
				add(b);
			}
		} finally {
			loading = false;
		}
	}
	
	
	
}
