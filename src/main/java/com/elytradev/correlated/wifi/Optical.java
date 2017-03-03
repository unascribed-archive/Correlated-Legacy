package com.elytradev.correlated.wifi;

import java.util.Collections;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A short-range transmitter and provides coverage for one APN within its range,
 * if there is line-of-sight.
 */
public class Optical extends RadiusBased implements INBTSerializable<NBTTagCompound>, Station {
	private String apn;
	
	public Optical() {
	}
	
	public Optical(BlockPos position, double radius, String apn) {
		this.position = position;
		this.radius = radius;
		this.apn = apn;
	}
	
	public String getAPN() {
		return apn;
	}
	
	@Override
	public Set<String> getAPNs() {
		return Collections.singleton(apn);
	}
	
	@Override
	public boolean isInRange(Entity e) {
		RayTraceResult rtr = e.world.rayTraceBlocks(new Vec3d(position), e.getPositionVector().addVector(0, e.getEyeHeight(), 0));
		return (rtr == null || rtr.entityHit == e || rtr.typeOfHit == Type.MISS) &&
				e.getDistanceSq(position.getX()+0.5, position.getY()+0.5, position.getZ()+0.5) <= (radius*radius);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setLong("Position", position.toLong());
		nbt.setInteger("Radius", (int)(radius*2));
		nbt.setString("APN", apn);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		position = BlockPos.fromLong(nbt.getLong("Position"));
		radius = nbt.getInteger("Radius")/2D;
		apn = nbt.getString("APN");
	}
}
