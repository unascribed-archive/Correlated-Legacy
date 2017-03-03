package com.elytradev.correlated.wifi;

import java.util.Collections;
import java.util.Set;

import com.elytradev.correlated.math.Vec2i;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public abstract class RadiusBased {
	protected BlockPos position;
	protected double radius;
	
	private transient Set<Vec2i> chunks;
	
	public RadiusBased() {
	}
	
	public BlockPos getPosition() {
		return position;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public double distanceTo(Entity e) {
		return e.getDistance(position.getX()+0.5, position.getY()+0.5, position.getZ()+0.5);
	}
	
	public Iterable<Vec2i> chunks() {
		if (chunks == null) {
			chunks = Sets.newHashSet();
			int chunkRadius = (int)Math.ceil(radius/16d);
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
						System.out.println(chunkX+", "+chunkZ+" is within range");
						chunks.add(new Vec2i(chunkX, chunkZ));
					}
				}
			}
		}
		return Collections.unmodifiableCollection(chunks);
	}
}
