package com.elytradev.correlated.tile;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockWireless;
import com.elytradev.correlated.block.BlockWireless.State;
import com.elytradev.correlated.wifi.Beacon;
import com.elytradev.correlated.wifi.IWirelessClient;
import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import com.elytradev.probe.api.impl.ProbeData;
import com.google.common.base.Joiner;

import net.minecraft.block.BlockBeacon;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityBeaconLens extends TileEntity implements IWirelessClient, ITickable {

	private static final Accessor<Boolean> isComplete = Accessors.findField(TileEntityBeacon.class, "field_146015_k", "isComplete", "j");
	
	@Override
	public void update() {
		if (!hasWorld() || getWorld().isRemote) return;
		Beacon b = Correlated.getDataFor(world).getWirelessManager().getBeacon(getPos());
		State newState = State.DEAD;
		TileEntity te = getWorld().getTileEntity(getPos().down());
		if (te == null || !(te instanceof TileEntityBeacon) || !isComplete.get(te)) {
			newState = State.DEAD;
		} else {
			boolean hasAnyStorage = false;
			for (String apn : b.getAPNs()) {
				if (!b.getStorages(apn).isEmpty()) {
					hasAnyStorage = true;
					break;
				}
			}
			if (b != null && hasAnyStorage) {
				newState = State.OK;
			} else {
				newState = State.ERROR;
			}
		}
		IBlockState ibs = world.getBlockState(getPos());
		if (ibs.getBlock() == Correlated.wireless) {
			if (ibs.getValue(BlockWireless.state) != newState) {
				world.setBlockState(getPos(), ibs.withProperty(BlockWireless.state, newState));
				BlockBeacon.updateColorAsync(world, getPos());
			}
		}
	}
	
	@Override
	public void setAPNs(Set<String> apn) {
		Beacon b = Correlated.getDataFor(getWorld()).getWirelessManager().getBeacon(getPos());
		if (b != null) {
			b.setAPNs(apn);
		}
	}

	@Override
	public Set<String> getAPNs() {
		Beacon b = Correlated.getDataFor(getWorld()).getWirelessManager().getBeacon(getPos());
		if (b != null) {
			return b.getAPNs();
		}
		return Collections.emptySet();
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
			Beacon b = Correlated.getDataFor(world).getWirelessManager().getBeacon(getPos());
			if (b != null) {
				data.add(new ProbeData(new TextComponentTranslation("tooltip.correlated.apns", Joiner.on(", ").join(b.getAPNs()))));
			}
		}
	}

}
