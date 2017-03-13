package com.elytradev.correlated.wifi;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.math.Vec2i;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
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
	private final Multimap<Vec2i, Beacon> beaconsByChunk = HashMultimap.create();
	private final Multimap<Vec2i, Beam> beamsByChunk = HashMultimap.create();
	
	private final Map<BlockPos, Beam> beamsByEnd = Maps.newHashMap();
	private final Map<BlockPos, Optical> opticals = Maps.newHashMap();
	private final Map<BlockPos, Beacon> beacons = Maps.newHashMap();
	
	private final Vec2i goat = new Vec2i(0, 0);
	
	private boolean loading = false;
	
	
	public WirelessManager(CorrelatedWorldData data) {
		this.data = data;
	}
	
	public void add(Beacon t) {
		if (t == null) return;
		for (Vec2i chunk : t.chunks()) {
			beaconsByChunk.put(chunk, t);
		}
		beacons.put(t.getPosition(), t);
		if (!loading) data.markDirty();
	}
	
	public void remove(Beacon t) {
		if (t == null) return;
		for (Vec2i chunk : t.chunks()) {
			beaconsByChunk.remove(chunk, t);
		}
		beacons.remove(t.getPosition());
		if (!loading) data.markDirty();
	}
	
	
	public void add(Optical o) {
		if (o == null) return;
		for (Vec2i chunk : o.chunks()) {
			opticalsByChunk.put(chunk, o);
		}
		opticals.put(o.getPosition(), o);
		if (!loading) data.markDirty();
	}
	
	public void remove(Optical o) {
		if (o == null) return;
		for (Vec2i chunk : o.chunks()) {
			opticalsByChunk.remove(chunk, o);
		}
		opticals.remove(o.getPosition());
		if (!loading) data.markDirty();
	}
	
	
	public void add(Beam b) {
		if (b == null) return;
		if (data.getWorld().getBlockState(b.getStart()).getBlock() != Correlated.wireless) return;
		if (data.getWorld().getBlockState(b.getEnd()).getBlock() != Correlated.wireless) return;
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
		return ImmutableList.copyOf(opticalsByChunk.get(goat));
	}
	
	public Iterable<Beacon> allBeaconsInChunk(Chunk c) {
		if (c == null || c.getWorld() != data.getWorld()) return Collections.emptySet();
		goat.x = c.xPosition;
		goat.y = c.zPosition;
		return ImmutableList.copyOf(beaconsByChunk.get(goat));
	}
	
	public Iterable<Beam> allBeamsInChunk(Chunk c) {
		if (c == null || c.getWorld() != data.getWorld()) return Collections.emptySet();
		goat.x = c.xPosition;
		goat.y = c.zPosition;
		return ImmutableList.copyOf(beamsByChunk.get(goat));
	}
	
	public Iterable<Station> allStationsInChunk(Chunk c) {
		return Iterables.concat(allBeaconsInChunk(c), allOpticalsInChunk(c));
	}
	
	public Beam getBeam(BlockPos end) {
		return beamsByEnd.get(end);
	}
	
	public Optical getOptical(BlockPos pos) {
		return opticals.get(pos);
	}
	
	public Beacon getBeacon(BlockPos pos) {
		return beacons.get(pos);
	}
	
	public Station getStation(BlockPos pos) {
		return getOptical(pos) == null ? getBeacon(pos) : getOptical(pos);
	}
	
	protected void update(RadiusBased rb) {
		if (rb instanceof Optical) {
			remove((Optical)rb);
			add((Optical)rb);
		} else if (rb instanceof Beacon) {
			remove((Beacon)rb);
			add((Beacon)rb);
		}
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
		NBTTagList beacon = new NBTTagList();
		for (Beacon b : beaconsByChunk.values()) {
			if (!serialized.add(b)) continue;
			beacon.appendTag(b.serializeNBT());
		}
		nbt.setTag("Beacon", beacon);
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
			beaconsByChunk.clear();
			beamsByChunk.clear();
			NBTTagList optical = nbt.getTagList("Optical", NBT.TAG_COMPOUND);
			for (int i = 0; i < optical.tagCount(); i++) {
				Optical o = new Optical(data);
				o.deserializeNBT(optical.getCompoundTagAt(i));
				add(o);
			}
			NBTTagList beacon = nbt.getTagList("Beacon", NBT.TAG_COMPOUND);
			for (int i = 0; i < beacon.tagCount(); i++) {
				Beacon b = new Beacon(data);
				b.deserializeNBT(beacon.getCompoundTagAt(i));
				add(b);
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

	public int getSignalStrength(double x, double y, double z, String apn) {
		Chunk c = data.getWorld().getChunkFromBlockCoords(new BlockPos((int)x, (int)y, (int)z));
		double minDist = Double.POSITIVE_INFINITY;
		Station closest = null;
		for (Station s : allStationsInChunk(c)) {
			if (s.getAPNs().contains(apn) && !s.getStorages(apn).isEmpty() && s.isInRange(x, y, z)) {
				double dist = s.distanceTo(x, y, z);
				if (dist < minDist) {
					minDist = dist;
					closest = s;
				}
			}
		}
		if (closest != null) {
			double div = 1-(minDist/closest.getRadius());
			return Math.max(0, Math.min(5, (int)(Math.log10(div*100)*3)));
		}
		return 0;
	}

	
}
