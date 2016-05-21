package io.github.elytra.copo.helper;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.ITweakable;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Blocks {

	public static boolean tryWrench(World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack inHand = player.getHeldItem(hand);
		if (inHand != null && inHand.getItem() == CoPo.weldthrower) {
			if (!world.isRemote) {
				IBlockState state = world.getBlockState(pos);
				if (player.isSneaking()) {
					state.getBlock().dropBlockAsItem(world, pos, state, 0);
					world.setBlockToAir(pos);
				} else if (state.getBlock() instanceof BlockDirectional) {
					world.setBlockState(pos, state.withProperty(BlockDirectional.FACING,
									EnumFacing.getHorizontal(state.getValue(BlockDirectional.FACING).getHorizontalIndex()+1)));
				} else if (state.getBlock() instanceof ITweakable) {
					((ITweakable)state.getBlock()).onTweak(world, pos, player, side, hitX, hitY, hitZ);
				}
			}
			return true;
		}
		return false;
	}

}
