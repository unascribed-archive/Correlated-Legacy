package com.elytradev.correlated.tile;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockWireless;
import com.elytradev.correlated.block.BlockWireless.State;
import com.elytradev.correlated.wifi.IWirelessClient;
import com.elytradev.correlated.wifi.Optical;
import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import com.elytradev.probe.api.impl.ProbeData;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityOpticalReceiver extends TileEntityNetworkMember implements ITickable, IWirelessClient {

	@Override
	public int getEnergyConsumedPerTick() {
		return Correlated.inst.opticalRfUsage;
	}
	
	@Override
	public void update() {
		if (!hasWorld() || getWorld().isRemote) return;
		Optical o = Correlated.getDataFor(world).getWirelessManager().getOptical(getPos());
		State newState = State.DEAD;
		if (!hasController() || !getController().isPowered()) {
			newState = State.DEAD;
		} else {
			if (o != null) {
				newState = State.OK;
			} else {
				newState = State.ERROR;
			}
		}
		IBlockState ibs = world.getBlockState(getPos());
		if (ibs.getBlock() == Correlated.wireless) {
			if (ibs.getValue(BlockWireless.state) != newState) {
				world.setBlockState(getPos(), ibs.withProperty(BlockWireless.state, newState));
			}
		}
	}
	
	public boolean isOperational() {
		return hasController() && getController().isPowered();
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		return nbt;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound nbt) {
	}
	
	@Override
	public void setAPNs(Set<String> apn) {
		if (apn.size() > 1) throw new IllegalArgumentException("Only supports 1 APN");
		Optical o = Correlated.getDataFor(world).getWirelessManager().getOptical(getPos());
		o.setAPN(apn.isEmpty() ? null : apn.iterator().next());
	}

	@Override
	public Set<String> getAPNs() {
		Optical o = Correlated.getDataFor(world).getWirelessManager().getOptical(getPos());
		return o.getAPN() == null ? Collections.emptySet() : Collections.singleton(o.getAPN());
	}

	@Override
	public BlockPos getPosition() {
		return getPos();
	}
	
	@Override
	public double getX() {
		return getPos().getX()+0.5;
	}
	
	@Override
	public double getY() {
		return getPos().getY()+0.5;
	}
	
	@Override
	public double getZ() {
		return getPos().getZ()+0.5;
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
			Optical o = Correlated.getDataFor(world).getWirelessManager().getOptical(getPos());
			if (o != null) {
				data.add(new ProbeData(new TextComponentTranslation("tooltip.correlated.apn", o.getAPN())));
			}
		}
	}

}
