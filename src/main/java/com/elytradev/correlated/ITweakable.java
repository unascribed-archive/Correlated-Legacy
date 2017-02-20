package com.elytradev.correlated;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITweakable {

	void onTweak(World world, BlockPos pos, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ);

}
