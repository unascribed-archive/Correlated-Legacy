package com.elytradev.correlated.wifi;

import java.util.Collections;
import java.util.Set;

import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.math.Vec2i;
import com.google.common.collect.Sets;

import net.minecraft.util.math.BlockPos;

public abstract class RadiusBased {
	protected CorrelatedWorldData data;
	protected BlockPos position;
	
	private transient Set<Vec2i> chunks;
	
	public RadiusBased(CorrelatedWorldData data) {
		this.data = data;
	}
	
	public BlockPos getPosition() {
		return position;
	}
	
	public double getX() {
		return getPosition().getX()+0.5;
	}
	
	public double getY() {
		return getPosition().getY()+0.5;
	}
	
	public double getZ() {
		return getPosition().getZ()+0.5;
	}
	
	public abstract double getRadius();
	
	public double distanceTo(double x, double y, double z) {
		return Math.sqrt(position.distanceSqToCenter(x, y, z));
	}
	
	protected void invalidateCache() {
		chunks = null;
		data.getWirelessManager().update(this);
	}
	
	public Iterable<Vec2i> chunks() {
		if (chunks == null) {
			chunks = Sets.newHashSet();
			int chunkRadius = (int)Math.ceil(getRadius()/16d);
			int chunkOriginX = position.getX()/16;
			int chunkOriginZ = position.getZ()/16;
			int radiusSq = chunkRadius*chunkRadius;
			for (int x = 0; x < chunkRadius*2; x++) {
				for (int z = 0; z < chunkRadius*2; z++) {
					int chunkX = chunkOriginX+(x-chunkRadius);
					int chunkZ = chunkOriginZ+(z-chunkRadius);
					
					int xDist = chunkX-chunkOriginX;
					int zDist = chunkZ-chunkOriginZ;
					
					if ((xDist*xDist)+(zDist*zDist) <= radiusSq) {
						chunks.add(new Vec2i(chunkX, chunkZ));
					}
				}
			}
		}
		return Collections.unmodifiableCollection(chunks);
	}
}
