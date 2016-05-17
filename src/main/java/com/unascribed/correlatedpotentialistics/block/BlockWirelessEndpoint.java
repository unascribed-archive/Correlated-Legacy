package com.unascribed.correlatedpotentialistics.block;

import java.util.List;

import com.google.common.base.Supplier;
import com.unascribed.correlatedpotentialistics.tile.TileEntityWirelessReceiver;
import com.unascribed.correlatedpotentialistics.tile.TileEntityWirelessTransmitter;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;

public class BlockWirelessEndpoint extends Block {
	public enum Kind implements IStringSerializable {
		RECEIVER(TileEntityWirelessReceiver::new),
		TRANSMITTER(TileEntityWirelessTransmitter::new);
		public static final Kind[] VALUES = values();
		private final Supplier<TileEntity> teConstructor;
		private final String lowerName;
		private Kind(Supplier<TileEntity> teConstructor) {
			lowerName = name().toLowerCase();
			this.teConstructor = teConstructor;
		}
		@Override public String getName() { return lowerName; }
		@Override public String toString() { return lowerName; }
		public TileEntity createTileEntity() {
			return teConstructor.get();
		}
	}
	public enum State implements IStringSerializable {
		DEAD,
		ERROR,
		LINKED;
		public static final State[] VALUES = values();
		private final String lowerName;
		private State() {
			lowerName = name().toLowerCase();
		}
		@Override public String getName() { return lowerName; }
		@Override public String toString() { return lowerName; }
	}
	
	public static final IProperty<Kind> kind = PropertyEnum.create("kind", Kind.class);
	public static final IProperty<State> state = PropertyEnum.create("state", State.class);
	
	public BlockWirelessEndpoint() {
		super(Material.iron);
		setBlockBounds(0, 0, 0, 1, 0.3125f, 1);
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean isFullCube() {
		return false;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		AxisAlignedBB base = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX()+1, pos.getY()+(4/16d), pos.getZ()+1);
		AxisAlignedBB pole = new AxisAlignedBB(pos.getX()+(7/16d), pos.getY()+(4/16d), pos.getZ()+(7/16d), pos.getX()+(9/16d), pos.getY()+(16/16d), pos.getZ()+(9/16d));
		if (mask.intersectsWith(base)) {
			list.add(base);
		}
		if (mask.intersectsWith(pole)) {
			list.add(pole);
		}
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return state.getValue(kind).createTileEntity();
	}
	
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(kind, meta == 0 ? Kind.RECEIVER : Kind.TRANSMITTER);
	}
	
	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, state, kind);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(BlockWirelessEndpoint.state).ordinal() & 0b0111)
				| (state.getValue(kind) == Kind.RECEIVER ? 0b1000 : 0);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(state, State.VALUES[meta%State.VALUES.length])
				.withProperty(kind, ((meta & 0b1000) != 0) ? Kind.RECEIVER : Kind.TRANSMITTER);
	}
	
	@Override
	public int getRenderType() {
		return 3;
	}
	
	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		list.add(new ItemStack(itemIn, 1, 0));
		list.add(new ItemStack(itemIn, 1, 1));
	}
}
