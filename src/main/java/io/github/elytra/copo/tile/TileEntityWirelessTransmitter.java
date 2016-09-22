package io.github.elytra.copo.tile;

import java.util.UUID;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.block.BlockWirelessEndpoint.State;
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("UUIDMost", id.getMostSignificantBits());
		nbt.setLong("UUIDLeast", id.getLeastSignificantBits());
		return nbt;
	}
	
	@Override
	protected State getCurrentState() {
		return State.LINKED;
	}

	@Override
	public long getEnergyConsumedPerTick() {
		return CoPo.inst.transmitterRfUsage;
	}
	
}
