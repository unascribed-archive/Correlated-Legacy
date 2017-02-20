package com.elytradev.correlated.tile;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockWirelessEndpoint;
import com.elytradev.correlated.block.BlockWirelessEndpoint.State;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

public abstract class TileEntityWirelessEndpoint extends TileEntityNetworkMember implements ITickable {
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos(), getPos().add(1,1,1));
	}
	
	@Override
	public void update() {
		if (hasWorld() && !world.isRemote) {
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == Correlated.wireless_endpoint) {
				State newState;
				if (hasStorage() && getStorage().isPowered()) {
					newState = getCurrentState();
				} else {
					newState = State.DEAD;
				}
				if (newState != state.getValue(BlockWirelessEndpoint.state)) {
					getWorld().setBlockState(getPos(), state.withProperty(BlockWirelessEndpoint.state, newState));
				}
			}
		}
	}

	protected abstract State getCurrentState();
	
}
