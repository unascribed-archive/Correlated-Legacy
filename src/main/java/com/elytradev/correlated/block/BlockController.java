package com.elytradev.correlated.block;

import java.util.Locale;

import com.elytradev.correlated.helper.Blocks;
import com.elytradev.correlated.tile.TileEntityController;
import com.elytradev.correlated.tile.TileEntityNetworkMember;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
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
	public static final PropertyBool cheaty = PropertyBool.create("cheaty");
	public static final IProperty<State> state = PropertyEnum.create("state", State.class);
	public BlockController() {
		super(Material.IRON);
	}

	
	@Override
	public boolean getUseNeighborBrightness(IBlockState state) {
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
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, state, cheaty);
	}

	@Override
	public int getMetaFromState(IBlockState blockState) {
		return blockState.getValue(state).ordinal() | (blockState.getValue(cheaty) ? 0b1000 : 0);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(state, State.values[meta&0b0111%State.values.length]).withProperty(cheaty, (meta&0b1000) != 0);
	}

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(itemIn, 1, 0));
		list.add(new ItemStack(itemIn, 1, 8));
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Blocks.tryWrench(worldIn, pos, playerIn, hand, side, hitZ, hitZ, hitZ)) {
			return true;
		}
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityController) {
			((TileEntityController) te).scanNetwork();
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityNetworkMember) {
			((TileEntityNetworkMember)te).handleNeighborChange(world, pos, neighbor);
		}
	}
}
