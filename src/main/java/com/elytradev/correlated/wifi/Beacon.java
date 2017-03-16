package com.elytradev.correlated.wifi;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.storage.IDigitalStorage;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * A long-range transmitter that ignores line-of-sight and provides coverage for
 * one or more APNs within its range.
 */
public class Beacon extends RadiusBased implements INBTSerializable<NBTTagCompound>, Station, IWirelessClient {
	private static final Accessor<Boolean> isComplete = Accessors.findField(TileEntityBeacon.class, "field_146015_k", "isComplete", "j");
	
	private CorrelatedWorldData data;
	private ImmutableSet<String> apns;
	
	public Beacon(CorrelatedWorldData data) {
		super(data);
		this.data = data;
	}
	
	public Beacon(CorrelatedWorldData data, BlockPos position, ImmutableSet<String> apns) {
		super(data);
		this.data = data;
		this.position = position;
		this.apns = apns;
	}
	
	@Override
	public ImmutableSet<String> getAPNs() {
		return apns;
	}
	
	@Override
	public void setAPNs(Set<String> apn) {
		this.apns = ImmutableSet.copyOf(apn);
		data.markDirty();
	}
	
	@Override
	public double getRadius() {
		TileEntity te = data.getWorld().getTileEntity(position.down());
		if (te instanceof TileEntityBeacon) {
			int levels = ((TileEntityBeacon)te).getField(0);
			int range = (levels*10)+10;
			return range;
		}
		return 0;
	}
	
	@Override
	public boolean isOperational() {
		TileEntity te = data.getWorld().getTileEntity(position.down());
		if (te instanceof TileEntityBeacon) {
			return isComplete.get(te);
		}
		return false;
	}
	
	@Override
	public boolean isInRange(double x, double y, double z) {
		return isOperational() && position.distanceSqToCenter(x, y, z) <= (getRadius()*getRadius());
	}
	
	@Override
	public List<IDigitalStorage> getStorages(String apn, Set<Station> alreadyChecked) {
		if (!apns.contains(apn)) return Collections.emptyList();
		alreadyChecked.add(this);
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
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setLong("Position", position.toLong());
		NBTTagList li = new NBTTagList();
		for (String s : apns) {
			li.appendTag(new NBTTagString(s));
		}
		nbt.setTag("APNs", li);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		position = BlockPos.fromLong(nbt.getLong("Position"));
		NBTTagList nbtli = nbt.getTagList("APNs", NBT.TAG_STRING);
		List<String> li = Lists.newArrayListWithCapacity(nbtli.tagCount());
		for (int i = 0; i < nbtli.tagCount(); i++) {
			li.add(nbtli.getStringTagAt(i));
		}
		apns = ImmutableSet.copyOf(li);
	}
}
