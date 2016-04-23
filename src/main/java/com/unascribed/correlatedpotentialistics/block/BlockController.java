package com.unascribed.correlatedpotentialistics.block;

import java.util.Locale;

import com.unascribed.correlatedpotentialistics.helper.Blocks;
import com.unascribed.correlatedpotentialistics.tile.TileEntityController;
import com.unascribed.correlatedpotentialistics.tile.TileEntityNetworkMember;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockController extends Block {
	public enum State implements IStringSerializable {
		POWERED,
		OFF,
		ERROR,
		BOOTING;
		private static final State[] values = values();
		private final String name = name().toLowerCase(Locale.ROOT);
		@Override
		public String getName() {
			return name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	public static final IProperty<State> state = PropertyEnum.create("state", State.class);
	public BlockController() {
		super(Material.iron);
	}

	@Override
	public boolean getUseNeighborBrightness() {
		return true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityController) {
			((TileEntityController) te).scanNetwork();
		}
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityController();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, state);
	}

	@Override
	public int getMetaFromState(IBlockState blockState) {
		return blockState.getValue(state).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(state, State.values[meta%State.values.length]);
	}

	@Override
	public int getRenderType() {
		return 3;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Blocks.tryWrench(worldIn, pos, playerIn, side, hitZ, hitZ, hitZ)) {
			return true;
		}
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityController) {
			((TileEntityController) te).scanNetwork();
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ);
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityNetworkMember) {
			((TileEntityNetworkMember)te).handleNeighborChange(world, pos, neighbor);
		}
	}
}
