package com.elytradev.correlated;

import com.elytradev.correlated.wifi.Beam;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldEventListener extends WorldEventListenerAdapter {

	@Override
	public void notifyBlockUpdate(World world, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		boolean isAir = newState.getBlock().isAir(newState, world, pos);
		for (Beam b : CorrelatedWorldData.getFor(world).getWirelessManager().allBeamsInChunk(world.getChunkFromBlockCoords(pos))) {
			if (b.intersects(pos)) {
				if (isAir) {
					b.removeObstruction(pos);
				} else {
					b.addObstruction(pos);
				}
			}
		}
	}
	
}
