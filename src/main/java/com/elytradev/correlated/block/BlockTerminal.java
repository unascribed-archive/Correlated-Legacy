package com.elytradev.correlated.block;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.helper.Blocks;
import com.elytradev.correlated.item.ItemFloppy;
import com.elytradev.correlated.network.ShowTerminalErrorMessage;
import com.elytradev.correlated.tile.TileEntityTerminal;

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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class BlockTerminal extends Block {
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool LIT = PropertyBool.create("lit");
	public static final PropertyBool FLOPPY = PropertyBool.create("floppy");
	
	public BlockTerminal() {
		super(Material.IRON);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityTerminal();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, LIT, FLOPPY);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(FACING).getHorizontalIndex() & 0b0011)
				| (state.getValue(LIT) ? 0b0100 : 0)
				| (state.getValue(FLOPPY) ? 0b1000 : 0);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, EnumFacing.getHorizontal(meta&0b0011))
				.withProperty(LIT, (meta&0b0100) != 0)
				.withProperty(FLOPPY, (meta&0b1000) != 0);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Blocks.tryWrench(world, pos, player, hand, side, hitZ, hitZ, hitZ)) {
			return true;
		}
		ItemStack heldItem = player.getHeldItem(hand);
		if (!player.isSneaking()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityTerminal) {
				TileEntityTerminal tet = (TileEntityTerminal)te;
				if (side == state.getValue(FACING)) {
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
					if (withinRegion(x, y, 5, 13)) {
						if (heldItem.getItem() instanceof ItemFloppy) {
							tet.setInventorySlotContents(1, heldItem.copy());
							heldItem.setCount(0);
							return true;
						} else if (tet.getStackInSlot(1) != null) {
							if (!world.isRemote) {
								EntityItem ent = new EntityItem(world, pos.getX()+hitX+(side.getFrontOffsetX()*0.2),
										pos.getY()+hitY+(side.getFrontOffsetY()*0.2), pos.getZ()+hitZ+(side.getFrontOffsetZ()*0.2));
								ent.setItem(tet.removeStackFromSlot(1));
								ent.setNoPickupDelay();
								world.spawnEntity(ent);
							}
							return true;
						}
					}
				}
				if (tet.hasController()) {
					if (!world.isRemote) {
						switch (world.getBlockState(tet.getController().getPos()).getValue(BlockController.STATE)) {
							case BOOTING:
								player.sendMessage(new TextComponentTranslation("msg.correlated.terminal_booting"));
								break;
							case ERROR:
								new ShowTerminalErrorMessage(pos).sendTo(player);
								break;
							case OFF:
								player.sendMessage(new TextComponentTranslation("msg.correlated.terminal_no_power"));
								break;
							case POWERED:
								player.openGui(Correlated.inst, 0, world, pos.getX(), pos.getY(), pos.getZ());
								break;
							default:
								break;

						}
					}
					return true;
				} else if (!world.isRemote && tet.getPotentialStored() > tet.getPotentialConsumedPerTick()) {
					player.openGui(Correlated.inst, 0, world, pos.getX(), pos.getY(), pos.getZ());
					return true;
				}
			}
			if (!world.isRemote) {
				player.sendMessage(new TextComponentTranslation("msg.correlated.terminal_no_power"));
			}
			return true;
		}
		return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
	}
	
	private boolean withinRegion(float x, float y, int regionX, int regionY) {
		return x >= (regionX/16f) && x <= ((regionX+6)/16f)
				&& y >= (regionY/16f) && y <= ((regionY+1)/16f);
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
