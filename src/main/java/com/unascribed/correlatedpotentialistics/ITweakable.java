package com.unascribed.correlatedpotentialistics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface ITweakable {

	void onTweak(World world, BlockPos pos, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ);

}
