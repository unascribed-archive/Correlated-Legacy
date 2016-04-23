package com.unascribed.correlatedpotentialistics.block;

import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.ITweakable;
import com.unascribed.correlatedpotentialistics.helper.Blocks;
import com.unascribed.correlatedpotentialistics.tile.TileEntityInterface;
import com.unascribed.correlatedpotentialistics.tile.TileEntityNetworkMember;
import com.unascribed.correlatedpotentialistics.tile.TileEntityInterface.FaceMode;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockInterface extends Block implements ITweakable {
	public static final IProperty<FaceMode> top = PropertyEnum.create("top", FaceMode.class);
	public static final IProperty<FaceMode> bottom = PropertyEnum.create("bottom", FaceMode.class);
	public static final IProperty<FaceMode> north = PropertyEnum.create("north", FaceMode.class);
	public static final IProperty<FaceMode> east = PropertyEnum.create("east", FaceMode.class);
	public static final IProperty<FaceMode> south = PropertyEnum.create("south", FaceMode.class);
	public static final IProperty<FaceMode> west = PropertyEnum.create("west", FaceMode.class);

	public BlockInterface() {
		super(Material.iron);
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, top, bottom, north, east, south, west);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityInterface) {
			TileEntityInterface tei = (TileEntityInterface)te;
			state = state.withProperty(top, tei.getModeForFace(EnumFacing.UP));
			state = state.withProperty(bottom, tei.getModeForFace(EnumFacing.DOWN));
			state = state.withProperty(north, tei.getModeForFace(EnumFacing.NORTH));
			state = state.withProperty(east, tei.getModeForFace(EnumFacing.EAST));
			state = state.withProperty(south, tei.getModeForFace(EnumFacing.SOUTH));
			state = state.withProperty(west, tei.getModeForFace(EnumFacing.WEST));
		}
		return super.getActualState(state, world, pos);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityInterface();
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityNetworkMember) {
			((TileEntityNetworkMember)te).handleNeighborChange(world, pos, neighbor);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityInterface) {
			TileEntityInterface tei = (TileEntityInterface)te;
			for (int i = 0; i < tei.getSizeInventory(); i++) {
				ItemStack is = tei.getStackInSlot(i);
				if (is != null) {
					spawnAsEntity(world, pos, is);
					tei.setInventorySlotContents(i, null);
				}
			}
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Blocks.tryWrench(world, pos, player, side, hitX, hitY, hitZ)) {
			return true;
		}
		if (!player.isSneaking()) {
			player.openGui(CoPo.inst, 2, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
	}

	@Override
	public void onTweak(World world, BlockPos pos, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityInterface) {
			TileEntityInterface tei = (TileEntityInterface)te;
			FaceMode[] values = FaceMode.values();
			FaceMode cur = tei.getModeForFace(side);
			FaceMode nw = values[(cur.ordinal()+1)%values.length];
			tei.setModeForFace(side, nw);
			world.markBlockForUpdate(pos);
		}
	}

}
