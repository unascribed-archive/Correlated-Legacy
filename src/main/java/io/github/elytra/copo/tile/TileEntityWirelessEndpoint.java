package io.github.elytra.copo.tile;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.block.BlockWirelessEndpoint;
import io.github.elytra.copo.block.BlockWirelessEndpoint.State;
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
		if (hasWorldObj() && !worldObj.isRemote) {
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == CoPo.wireless_endpoint) {
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
