package com.elytradev.correlated.wifi;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.storage.IDigitalStorage;
import com.elytradev.correlated.tile.TileEntityOpticalTransceiver;
import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A short-range transmitter that provides coverage for one APN within its range,
 * if there is line-of-sight.
 */
public class Optical extends RadiusBased implements INBTSerializable<NBTTagCompound>, Station {
	private CorrelatedWorldData data;
	private double radius;
	private String apn;
	
	public Optical(CorrelatedWorldData data) {
		super(data);
		this.data = data;
	}
	
	public Optical(CorrelatedWorldData data,  BlockPos position, double radius, String apn) {
		super(data);
		this.data = data;
		this.position = position;
		this.radius = radius;
		this.apn = apn;
	}
	
	public String getAPN() {
		return apn;
	}
	
	@Override
	public Set<String> getAPNs() {
		return apn == null ? Collections.emptySet() : Collections.singleton(apn);
	}
	
	public void setAPN(String apn) {
		this.apn = apn;
		data.markDirty();
	}
	
	@Override
	public double getRadius() {
		return radius;
	}
	
	@Override
	public boolean isInRange(double x, double y, double z) {
		if (!isOperational()) return false;
		Vec3d start = new Vec3d(x, y, z);
		Vec3d end = new Vec3d(position.getX()+0.5, position.getY()+0.5, position.getZ()+0.5);
		Vec3d diff = end.subtract(start).normalize().scale(1.5);
		RayTraceResult rtr = data.getWorld().rayTraceBlocks(start.add(diff), end);
		return (rtr != null && rtr.typeOfHit == Type.BLOCK && position.equals(rtr.getBlockPos())) &&
				position.distanceSqToCenter(x, y, z) <= (radius*radius);
	}
	
	@Override
	public boolean isOperational() {
		TileEntity te = data.getWorld().getTileEntity(position);
		if (te instanceof TileEntityOpticalTransceiver) {
			return ((TileEntityOpticalTransceiver)te).isOperational();
		}
		return false;
	}
	
	@Override
	public List<IDigitalStorage> getStorages(String apn, Set<Station> alreadyChecked) {
		alreadyChecked.add(this);
		if (this.apn == null || !this.apn.equals(apn)) return Collections.emptyList();
		TileEntity te = data.getWorld().getTileEntity(position);
		if (te instanceof TileEntityOpticalTransceiver) {
			TileEntityOpticalTransceiver teor = ((TileEntityOpticalTransceiver)te);
			if (teor.hasController()) {
				return Collections.singletonList(teor.getController());
			} else {
				List<IDigitalStorage> li = Lists.newArrayList();
				for (Station s : data.getWirelessManager().allStationsInChunk(data.getWorld().getChunkFromBlockCoords(getPosition()))) {
					if (alreadyChecked.contains(s)) continue;
					if (s.isInRange(getX(), getY(), getZ()) && s.getAPNs().contains(apn)) {
						li.addAll(s.getStorages(apn, alreadyChecked));
					}
					alreadyChecked.add(s);
				}
				return li;
			}
		}
		return Collections.emptyList();
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setLong("Position", position.toLong());
		nbt.setInteger("Radius", (int)(radius*2));
		if (apn != null) {
			nbt.setString("APN", apn);
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		position = BlockPos.fromLong(nbt.getLong("Position"));
		radius = nbt.getInteger("Radius")/2D;
		apn = nbt.hasKey("APN") ? nbt.getString("APN") : null;
	}
}
