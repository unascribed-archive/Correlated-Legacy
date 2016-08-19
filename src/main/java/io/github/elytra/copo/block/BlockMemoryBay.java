package io.github.elytra.copo.block;

import io.github.elytra.copo.helper.Blocks;
import io.github.elytra.copo.item.ItemMemory;
import io.github.elytra.copo.tile.TileEntityMemoryBay;
import io.github.elytra.copo.tile.TileEntityNetworkMember;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMemoryBay extends Block {
	public static final IProperty<EnumFacing> facing = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool lit = PropertyBool.create("lit");
	
	public BlockMemoryBay() {
		super(Material.IRON);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityMemoryBay();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, facing, lit);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(facing).getHorizontalIndex() & 0b0011)
				| (state.getValue(lit) ? 0b0100 : 0);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(facing, EnumFacing.getHorizontal(meta&0b0011))
				.withProperty(lit, (meta&0b0100) != 0);
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
		if (te instanceof TileEntityMemoryBay) {
			TileEntityMemoryBay temb = (TileEntityMemoryBay) te;
			for (int i = 0; i < 12; i++) {
				if (temb.hasMemoryInSlot(i)) {
					spawnAsEntity(world, pos, temb.getMemoryInSlot(i));
					temb.setMemoryInSlot(i, null);
				}
			}
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Blocks.tryWrench(world, pos, player, hand, side, hitZ, hitZ, hitZ)) {
			return true;
		}
		ItemStack inHand = player.getHeldItem(hand);
		int slot = getLookedAtSlot(state, side, hitX, hitY, hitZ);

		if (slot != -1) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityMemoryBay) {
				TileEntityMemoryBay tedb = (TileEntityMemoryBay) te;
				if (tedb.hasMemoryInSlot(slot)) {
					if (!world.isRemote) {
						EntityItem ent = new EntityItem(world, pos.getX()+hitX+(side.getFrontOffsetX()*0.2),
								pos.getY()+hitY+(side.getFrontOffsetY()*0.2), pos.getZ()+hitZ+(side.getFrontOffsetZ()*0.2));
						ent.setEntityItemStack(tedb.getMemoryInSlot(slot));
						ent.setNoPickupDelay();
						world.spawnEntityInWorld(ent);
						tedb.setMemoryInSlot(slot, null);
					}
					return true;
				} else {
					if (inHand != null && inHand.getItem() instanceof ItemMemory) {
						if (!world.isRemote) {
							tedb.setMemoryInSlot(slot, inHand);
							if (hand == EnumHand.MAIN_HAND) {
								player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, null);
							} else {
								player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, null);
							}
						}
						return true;
					}
				}
			}
		}
		return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
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
		} else if (withinRegion(x, y, 3, 5)) {
			slot = 2;
		} else if (withinRegion(x, y, 9, 5)) {
			slot = 3;
		} else if (withinRegion(x, y, 3, 7)) {
			slot = 4;
		} else if (withinRegion(x, y, 9, 7)) {
			slot = 5;
		} else if (withinRegion(x, y, 3, 9)) {
			slot = 6;
		} else if (withinRegion(x, y, 9, 9)) {
			slot = 7;
		} else if (withinRegion(x, y, 3, 11)) {
			slot = 8;
		} else if (withinRegion(x, y, 9, 11)) {
			slot = 9;
		} else if (withinRegion(x, y, 3, 13)) {
			slot = 10;
		} else if (withinRegion(x, y, 9, 13)) {
			slot = 11;
		}
		return slot;
	}

	private boolean withinRegion(float x, float y, int regionX, int regionY) {
		return x >= (regionX/16f) && x <= ((regionX+4)/16f)
				&& y >= (regionY/16f) && y <= ((regionY+1)/16f);
	}
}
