package com.unascribed.correlatedpotentialistics.tile;

import java.util.UUID;

import com.unascribed.correlatedpotentialistics.block.BlockWirelessEndpoint.State;

import net.minecraft.nbt.NBTTagCompound;

public class TileEntityWirelessTransmitter extends TileEntityWirelessEndpoint {
	private UUID id = UUID.randomUUID();
	@Override
	public void update() {
		super.update();
	}
	
	public UUID getId() {
		return id;
	}
	
	public void setId(UUID id) {
		this.id = id;
		markDirty();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		id = new UUID(nbt.getLong("UUIDMost"), nbt.getLong("UUIDLeast"));
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("UUIDMost", id.getMostSignificantBits());
		nbt.setLong("UUIDLeast", id.getLeastSignificantBits());
	}
	
	@Override
	protected State getCurrentState() {
		return State.LINKED;
	}
	
}
