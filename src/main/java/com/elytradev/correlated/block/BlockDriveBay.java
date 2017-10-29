package com.elytradev.correlated.block;

import com.elytradev.correlated.helper.Blocks;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.item.ItemDrive;
import com.elytradev.correlated.tile.TileEntityDriveBay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDriveBay extends Block {
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool LIT = PropertyBool.create("lit");
	
	public BlockDriveBay() {
		super(Material.IRON);
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
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, LIT);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(FACING).getHorizontalIndex() & 0b0011)
				| (state.getValue(LIT) ? 0b0100 : 0);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, EnumFacing.getHorizontal(meta&0b0011))
				.withProperty(LIT, (meta&0b0100) != 0);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityDriveBay) {
			TileEntityDriveBay tedb = (TileEntityDriveBay) te;
			for (ItemStack drive : tedb) {
				spawnAsEntity(world, pos, drive);
			}
			tedb.clear();
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Blocks.tryWrench(world, pos, player, hand, side, hitZ, hitZ, hitZ)) {
			return true;
		}
		ItemStack inHand = player.getHeldItem(hand);
		int slot = getLookedAtSlot(state, side, hitX, hitY, hitZ);

		if (slot != -1) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityDriveBay) {
				TileEntityDriveBay tedb = (TileEntityDriveBay) te;
				if (tedb.hasDriveInSlot(slot)) {
					ItemStack drive = tedb.getDriveInSlot(slot);
					if (CConfig.restrictCreativeDrives && drive.getItem() instanceof ItemDrive && ((ItemDrive)drive.getItem()).isCreativeDrive(drive) && !player.capabilities.isCreativeMode) return false;
					if (!world.isRemote) {
						EntityItem ent = new EntityItem(world, pos.getX()+hitX+(side.getFrontOffsetX()*0.2),
								pos.getY()+hitY+(side.getFrontOffsetY()*0.2), pos.getZ()+hitZ+(side.getFrontOffsetZ()*0.2));
						ent.setItem(tedb.getDriveInSlot(slot));
						ent.setNoPickupDelay();
						world.spawnEntity(ent);
						tedb.setDriveInSlot(slot, ItemStack.EMPTY);
					}
					return true;
				} else {
					if (inHand != null && inHand.getItem() instanceof ItemDrive) {
						if (CConfig.restrictCreativeDrives && ((ItemDrive)inHand.getItem()).isCreativeDrive(inHand) && !player.capabilities.isCreativeMode) return false;
						if (!world.isRemote) {
							tedb.setDriveInSlot(slot, inHand.copy());
							inHand.setCount(0);
						}
						return true;
					}
				}
			}
		}
		return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
	}

	public int getLookedAtSlot(IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (side != state.getValue(FACING)) return -1;
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
	
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}
}
