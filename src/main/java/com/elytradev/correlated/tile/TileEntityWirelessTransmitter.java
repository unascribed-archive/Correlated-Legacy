package com.elytradev.correlated.tile;

import java.util.List;
import java.util.UUID;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.CorrelatedWorldData.Transmitter;
import com.elytradev.correlated.block.BlockWirelessEndpoint.State;

import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import com.elytradev.probe.api.impl.ProbeData;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;

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
	public int getEnergyConsumedPerTick() {
		return Correlated.inst.transmitterRfUsage;
	}
	
	private Object probeCapability;
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == null) return null;
		if (capability == Correlated.PROBE) {
			if (probeCapability == null) probeCapability = new ProbeCapability();
			return (T)probeCapability;
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == null) return false;
		if (capability == Correlated.PROBE) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	private final class ProbeCapability implements IProbeDataProvider {
		@Override
		public void provideProbeData(List<IProbeData> data) {
			int range = 0;
			Transmitter t = Correlated.getDataFor(getWorld()).getTransmitterById(getId());
			if (t != null) {
				range = (int)t.range;
			}
			data.add(new ProbeData()
						.withLabel(new TextComponentTranslation("tooltip.correlated.transmitter_range", range)));
			// TODO should we display how many receivers are linked? there's not currently a way to do that
		}
	}
	
}
