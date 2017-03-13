package com.elytradev.correlated.tile;

import java.util.Collections;
import java.util.Set;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.wifi.Beacon;
import com.elytradev.correlated.wifi.IWirelessClient;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileEntityBeaconLens extends TileEntity implements IWirelessClient {

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

}
