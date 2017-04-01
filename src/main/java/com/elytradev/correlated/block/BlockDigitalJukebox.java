package com.elytradev.correlated.block;

import com.elytradev.correlated.Correlated;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDigitalJukebox extends Block {

	public BlockDigitalJukebox() {
		super(Material.IRON);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		playerIn.openGui(Correlated.inst, 6, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

}
