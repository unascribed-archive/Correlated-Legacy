package com.elytradev.correlated.tile;

import static net.minecraft.util.math.MathHelper.*;

import java.util.List;
import java.util.UUID;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.CorrelatedWorldData.Transmitter;
import com.elytradev.correlated.block.BlockWirelessEndpoint;
import com.elytradev.correlated.block.BlockWirelessEndpoint.State;

import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityWirelessReceiver extends TileEntityWirelessEndpoint {
	private UUID transmitter;
	private Transmitter transmitterCache;
	private float syncedYaw;
	private float syncedPitch;
	
	public float getYaw(float partialTicks) {
		switch (getCurrentState()) {
			case ERROR:
				return (world.getTotalWorldTime()+partialTicks)%360;
			case LINKED:
				if (hasWorld() && getWorld().isRemote) {
					return syncedYaw;
				} else {
					Vec3d dir = getDirectionToTransmitter();
					return (float)Math.toDegrees(atan2(dir.xCoord, dir.zCoord));
				}
			default:
				return 0;
		}
	}

	public float getPitch(float partialTicks) {
		switch (getCurrentState()) {
			case ERROR:
				return (((MathHelper.sin((world.getTotalWorldTime()+partialTicks)/20f)+1)/2)*90)-20;
			case LINKED:
				if (hasWorld() && getWorld().isRemote) {
					return syncedPitch;
				} else {
					Vec3d dir = getDirectionToTransmitter();
					Vec3d xz = new Vec3d(dir.xCoord, 0, dir.zCoord);
					double xzLength = xz.lengthVector();
					return (float)-Math.toDegrees(atan2(dir.yCoord, xzLength));
				}
			default:
				return 20;
		}
	}

	public Vec3d getFacing(float partialTicks) {
		switch (getCurrentState()) {
			case ERROR:
				float yr = (float)Math.toRadians(getYaw(partialTicks));
				float pr = (float)Math.toRadians(getPitch(partialTicks));
				return new Vec3d(-(sin(yr)*cos(pr)), sin(pr), -(cos(yr)*cos(pr)));
			case LINKED:
				return getDirectionToTransmitter();
			default:
				return new Vec3d(0, 0.265, -0.735);
		}
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("x", getPos().getX());
		tag.setInteger("y", getPos().getY());
		tag.setInteger("z", getPos().getZ());
		if (getCurrentState() == State.LINKED) {
			tag.setFloat("Yaw", getYaw(0));
			tag.setFloat("Pitch", getPitch(0));
		}
		return tag;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		syncedYaw = pkt.getNbtCompound().getFloat("Yaw");
		syncedPitch = pkt.getNbtCompound().getFloat("Pitch");
	}
	
	public boolean hasTransmitter() {
		return getTransmitter() != null;
	}
	
	public Transmitter getTransmitter() {
		if (!hasWorld()) return null;
		if (transmitter == null) return null;
		if (transmitterCache != null && transmitterCache.isValid()) return transmitterCache;
		if (hasStorage() && !getStorage().isCheckingInfiniteLoop()) {
			getStorage().checkInfiniteLoop();
		}
		Transmitter t = Correlated.getDataFor(getWorld()).getTransmitterById(transmitter);
		if (t == null || t.position.distanceSqToCenter(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5) > t.range*t.range) return null;
		IBlockState state = getWorld().getBlockState(getPos());
		getWorld().notifyBlockUpdate(getPos(), state, state, 8);
		transmitterCache = t;
		return t;
	}
	
	public void setTransmitter(UUID transmitter) {
		this.transmitter = transmitter;
		transmitterCache = null;
	}
	
	private Vec3d getDirectionToTransmitter() {
		if (!hasTransmitter()) return new Vec3d(0, 0, 0);
		BlockPos sub = getPos().subtract(getTransmitter().position);
		return new Vec3d(sub).normalize();
	}
	
	@Override
	protected State getCurrentState() {
		if (hasWorld() && getWorld().isRemote) {
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == Correlated.wireless_endpoint) {
				return state.getValue(BlockWirelessEndpoint.state);
			}
		}
		return hasTransmitter() ? State.LINKED : State.ERROR;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setLong("TransmitterUUIDMost", transmitter.getMostSignificantBits());
		compound.setLong("TransmitterUUIDLeast", transmitter.getLeastSignificantBits());
		return compound;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		setTransmitter(new UUID(compound.getLong("TransmitterUUIDMost"), compound.getLong("TransmitterUUIDLeast")));
	}

	public TileEntityController getTransmitterController() {
		if (!hasTransmitter()) return null;
		Transmitter t = getTransmitter();
		TileEntity te = getWorld().getTileEntity(t.position);
		if (te != null && te instanceof TileEntityWirelessTransmitter) {
			TileEntityController controller = ((TileEntityWirelessTransmitter)te).getStorage();
			if (controller == null || !controller.isPowered()) return null;
			return controller;
		}
		return null;
	}

	@Override
	public int getEnergyConsumedPerTick() {
		return Correlated.inst.receiverRfUsage;
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
			// TODO should we display who we're linked to somehow? showing a UUID directly isn't very user-friendly...
		}
	}
	
}
