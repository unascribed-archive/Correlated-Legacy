package com.unascribed.correlatedpotentialistics.block;

import com.unascribed.correlatedpotentialistics.helper.Blocks;
import com.unascribed.correlatedpotentialistics.item.ItemDrive;
import com.unascribed.correlatedpotentialistics.tile.TileEntityDriveBay;
import com.unascribed.correlatedpotentialistics.tile.TileEntityNetworkMember;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDriveBay extends Block {
	public static final IProperty<EnumFacing> facing = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public BlockDriveBay() {
		super(Material.iron);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityDriveBay();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(facing).getHorizontalIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(facing, EnumFacing.getHorizontal(meta));
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facingIn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState()
				.withProperty(facing, placer.getHorizontalFacing().getOpposite());
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
		if (te instanceof TileEntityDriveBay) {
			TileEntityDriveBay tedb = (TileEntityDriveBay) te;
			for (int i = 0; i < 8; i++) {
				if (tedb.hasDriveInSlot(i)) {
					spawnAsEntity(world, pos, tedb.getDriveInSlot(i));
					tedb.setDriveInSlot(i, null);
				}
			}
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Blocks.tryWrench(world, pos, player, side, hitZ, hitZ, hitZ)) {
			return true;
		}
		ItemStack inHand = player.getHeldItem();
		int slot = getLookedAtSlot(state, side, hitX, hitY, hitZ);

		if (slot != -1) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityDriveBay) {
				TileEntityDriveBay tedb = (TileEntityDriveBay) te;
				if (tedb.hasDriveInSlot(slot)) {
					if (!world.isRemote) {
						EntityItem ent = new EntityItem(world, pos.getX()+hitX+(side.getFrontOffsetX()*0.2),
								pos.getY()+hitY+(side.getFrontOffsetY()*0.2), pos.getZ()+hitZ+(side.getFrontOffsetZ()*0.2));
						ent.setEntityItemStack(tedb.getDriveInSlot(slot));
						ent.setNoPickupDelay();
						world.spawnEntityInWorld(ent);
						tedb.setDriveInSlot(slot, null);
					}
					return true;
				} else {
					if (inHand != null && inHand.getItem() instanceof ItemDrive) {
						if (!world.isRemote) {
							tedb.setDriveInSlot(slot, inHand);
							player.setCurrentItemOrArmor(0, null);
						}
						return true;
					}
				}
			}
		}
		return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
	}

	public int getLookedAtSlot(IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (side != state.getValue(facing)) return -1;
		float x;
		float y = 1-hitY;
		switch (side) {
			case NORTH:
				x = 1-hitX;
				break;
			case EAST:
				x = 1-hitZ;
				break;
			case SOUTH:
				x = hitX;
				break;
			case WEST:
				x = hitZ;
				break;
			default:
				x = 0;
		}
		int slot = -1;
		if (withinRegion(x, y, 3, 3)) {
			slot = 0;
		} else if (withinRegion(x, y, 9, 3)) {
			slot = 1;
		} else if (withinRegion(x, y, 3, 6)) {
			slot = 2;
		} else if (withinRegion(x, y, 9, 6)) {
			slot = 3;
		} else if (withinRegion(x, y, 3, 9)) {
			slot = 4;
		} else if (withinRegion(x, y, 9, 9)) {
			slot = 5;
		} else if (withinRegion(x, y, 3, 12)) {
			slot = 6;
		} else if (withinRegion(x, y, 9, 12)) {
			slot = 7;
		}
		return slot;
	}

	private boolean withinRegion(float x, float y, int regionX, int regionY) {
		return x >= (regionX/16f) && x <= ((regionX+4)/16f)
				&& y >= (regionY/16f) && y <= ((regionY+2)/16f);
	}
}
