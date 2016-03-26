package com.unascribed.correlatedpotentialistics.helper;

import buildcraft.api.tools.IToolWrench;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class Blocks {

	public static boolean tryWrench(World world, BlockPos pos, EntityPlayer player) {
		try {
			ItemStack inHand = player.getHeldItem();
			if (inHand != null && inHand.getItem() instanceof IToolWrench) {
				IToolWrench tool = ((IToolWrench)inHand.getItem());
				if (tool.canWrench(player, pos)) {
					if (!world.isRemote) {
						IBlockState state = world.getBlockState(pos);
						if (player.isSneaking()) {
							state.getBlock().dropBlockAsItem(world, pos, state, 0);
							world.setBlockToAir(pos);
						} else if (state.getBlock() instanceof BlockDirectional) {
							world.setBlockState(pos, state.withProperty(BlockDirectional.FACING,
											EnumFacing.getHorizontal(state.getValue(BlockDirectional.FACING).getHorizontalIndex()+1)));
						}
					}
					return true;
				}
			}
		} catch (Throwable t) {}
		return false;
	}

}
