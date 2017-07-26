package com.elytradev.correlated.wifi;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.math.Vec2i;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import gnu.trove.iterator.TShortIterator;
import gnu.trove.set.TShortSet;
import gnu.trove.set.hash.TShortHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * An infinite range connection between exactly two networks requiring
 * line-of-sight.
 */
public class Beam implements INBTSerializable<NBTTagCompound> {
	private CorrelatedWorldData data;
	
	private BlockPos start;
	private BlockPos end;
	
	private Map<Vec2i, TShortSet> beam;
	private Set<BlockPos> obstructions;
	
	private final Vec2i goat = new Vec2i(0, 0);
	
	public Beam(CorrelatedWorldData data) {
		this.data = data;
	}
	
	public Beam(CorrelatedWorldData data, BlockPos start, BlockPos end) {
		this.data = data;
		this.start = start;
		this.end = end;
	}

	private void calculateBeamIfNeeded() {
		if (beam == null) {
			Stopwatch sw = Stopwatch.createStarted();
			double dist = Math.sqrt(start.distanceSq(end));
			// check every quarter block, so we get a more solid line
			double planck = 0.25;
			obstructions = Sets.newHashSet();
			beam = Maps.newHashMap();
			
			cast(dist,  0.00,  0.00,  0.00, planck);
			
			// thanks Falkreon!
			cast(dist,  0.095,  0.02,  0.02, planck);
			cast(dist, -0.095, -0.02, -0.02, planck);
			cast(dist,  0.02,  0.005, -0.0975, planck);
			cast(dist, -0.02, -0.005,  0.0975, planck);
			cast(dist,  0.02, -0.0975,  0.00, planck);
			cast(dist, -0.02,  0.075,  0.00, planck);
			
			sw.stop();
			if (sw.elapsed(TimeUnit.MILLISECONDS) > 50) {
				CLog.warn("Took a long time ({} > 50 ms) to calculate all used positions for a microwave beam between {}, {}, {} and {}, {}, {} - a distance of {} blocks ({} plancks x {} raycasts = {} iterations)!",
						sw,
						start.getX(), start.getY(), start.getZ(),
						end.getX(), end.getY(), end.getZ(),
						(int)dist,
						(int)(dist/planck), 7, (int)((dist/planck)*7));
			}
		}
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setLong("Start", start.toLong());
		nbt.setLong("End", end.toLong());
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		start = BlockPos.fromLong(nbt.getLong("Start"));
		end = BlockPos.fromLong(nbt.getLong("End"));
		beam = null;
	}
	
	public World getWorld() {
		return data.getWorld();
	}
	
	public Set<BlockPos> getObstructions() {
		calculateBeamIfNeeded();
		return Collections.unmodifiableSet(obstructions);
	}
	
	public boolean isObstructed() {
		calculateBeamIfNeeded();
		return !obstructions.isEmpty();
	}
	
	public void addObstruction(BlockPos pos) {
		if (pos.equals(start) || pos.equals(end)) return;
		calculateBeamIfNeeded();
		obstructions.add(pos.toImmutable());
	}
	
	public void removeObstruction(BlockPos pos) {
		calculateBeamIfNeeded();
		obstructions.remove(pos);
	}
	
	public BlockPos getStart() {
		return start;
	}
	
	public BlockPos getEnd() {
		return end;
	}
	
	public Map<Vec2i, TShortSet> getWatchedPositions() {
		calculateBeamIfNeeded();
		return beam;
	}
	
	
	public Iterable<Vec2i> chunks() {
		calculateBeamIfNeeded();
		return Collections.unmodifiableCollection(beam.keySet());
	}
	
	public Iterable<MutableBlockPos> beamMutable() {
		calculateBeamIfNeeded();
		MutableBlockPos bpgoat = new MutableBlockPos();
		return Iterables.concat(Iterables.<Entry<Vec2i, TShortSet>, Iterable<MutableBlockPos>>transform(beam.entrySet(), (en) -> {
			TShortIterator p = en.getValue().iterator();
			return new FluentIterable<MutableBlockPos>() {
				@Override
				public Iterator<MutableBlockPos> iterator() {
					return new Iterator<MutableBlockPos>() {
						
						@Override
						public void remove() {
							p.remove();
						}
						
						@Override
						public boolean hasNext() {
							return p.hasNext();
						}
						
						@Override
						public MutableBlockPos next() {
							return fromShort(bpgoat, en.getKey().x, en.getKey().y, p.next());
						}
					};
				}
			};
		}));
	}
	
	public boolean intersects(BlockPos pos) {
		calculateBeamIfNeeded();
		return getSetForBlock(pos.getX(), pos.getZ()).contains(toShort(pos));
	}
	
	private void cast(double dist, double xOfs, double yOfs, double zOfs, double planck) {
		Vec3d dir = new Vec3d(end.getX()-start.getX(), end.getY()-start.getY(), end.getZ()-start.getZ()).normalize();
		Vec3d step = dir.scale(planck);
		double posX = start.getX()+0.5+xOfs;
		double posY = start.getY()+0.5+yOfs;
		double posZ = start.getZ()+0.5+zOfs;
		MutableBlockPos bpgoat = new MutableBlockPos();
		for (int i = 0; i < dist/planck; i++) {
			int x = (int)posX;
			int y = (int)posY;
			int z = (int)posZ;
			IBlockState ibs = getWorld().getBlockState(bpgoat.setPos(x, y, z));
			if (!ibs.getBlock().isAir(ibs, getWorld(), bpgoat)) {
				addObstruction(bpgoat);
			}
			short s = toShort(x, y, z);
			TShortSet set = getSetForBlock(x, z);
			set.add(s);
			posX += step.xCoord;
			posY += step.yCoord;
			posZ += step.zCoord;
		}
	}

	private MutableBlockPos fromShort(MutableBlockPos in, int chunkX, int chunkZ, short s) {
		int x = (s & 0xFFFF) >> X_SHIFT & X_MASK;
		int y = (s & 0xFFFF) >> Y_SHIFT & Y_MASK;
		int z = (s & 0xFFFF) & Z_MASK;
		return in.setPos(x+(chunkX<<4), y, z+(chunkZ<<4));
	}
	
	private short toShort(BlockPos bp) {
		return toShort(bp.getX()&15, bp.getY(), bp.getZ()&15);
	}
	
	private short toShort(int x, int y, int z) {
		return (short) ((x & X_MASK) << X_SHIFT | (y & Y_MASK) << Y_SHIFT | (z & Z_MASK) << 0);
	}

	private TShortSet getSetForBlock(int x, int z) {
		return getSetForChunk(x >> 4, z >> 4);
	}
	
	private TShortSet getSetForChunk(int x, int z) {
		goat.x = x;
		goat.y = z;
		if (!beam.containsKey(goat)) {
			beam.put(new Vec2i(x, z), new TShortHashSet());
		}
		return beam.get(goat);
	}

	// Somewhat stolen from BlockPos
	private static final int NUM_X_BITS = 4;
	private static final int NUM_Z_BITS = 4;
	private static final int NUM_Y_BITS = 16 - NUM_X_BITS - NUM_Z_BITS;
	private static final int Y_SHIFT = NUM_Z_BITS;
	private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
	private static final short X_MASK = (short)((1 << NUM_X_BITS) - 1);
	private static final short Y_MASK = (short)((1 << NUM_Y_BITS) - 1);
	private static final short Z_MASK = (short)((1 << NUM_Z_BITS) - 1);

}
