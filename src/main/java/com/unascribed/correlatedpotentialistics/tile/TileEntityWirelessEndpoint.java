package com.unascribed.correlatedpotentialistics.tile;

import net.minecraft.util.AxisAlignedBB;

public abstract class TileEntityWirelessEndpoint extends TileEntityNetworkMember {

	@Override
	public int getEnergyConsumedPerTick() {
		return 24;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos(), getPos().add(1,1,1));
	}

}
