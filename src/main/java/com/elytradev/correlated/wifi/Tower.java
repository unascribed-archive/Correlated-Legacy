package com.elytradev.correlated.wifi;

import java.util.List;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * A long-range transmitter that ignores line-of-sight and provides coverage for
 * one or more APNs within its range.
 */
public class Tower extends RadiusBased implements INBTSerializable<NBTTagCompound>, Station {
	private ImmutableSet<String> apns;
	
	public Tower() {
	}
	
	public Tower(BlockPos position, double radius, ImmutableSet<String> apns) {
		this.position = position;
		this.radius = radius;
		this.apns = apns;
	}
	
	@Override
	public ImmutableSet<String> getAPNs() {
		return apns;
	}
	
	@Override
	public boolean isInRange(Entity e) {
		return e.getDistanceSq(position) <= (radius*radius);
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setLong("Position", position.toLong());
		nbt.setInteger("Radius", (int)(radius*2));
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
		radius = nbt.getInteger("Radius")/2D;
		NBTTagList nbtli = nbt.getTagList("APNs", NBT.TAG_STRING);
		List<String> li = Lists.newArrayListWithCapacity(nbtli.tagCount());
		for (int i = 0; i < nbtli.tagCount(); i++) {
			li.add(nbtli.getStringTagAt(i));
		}
		apns = ImmutableSet.copyOf(li);
	}
}
