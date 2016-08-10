package io.github.elytra.copo.block;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.helper.Blocks;
import io.github.elytra.copo.tile.TileEntityNetworkMember;
import io.github.elytra.copo.tile.TileEntityVT;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockVT extends Block {
	public static final IProperty<EnumFacing> facing = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool lit = PropertyBool.create("lit");
	
	public BlockVT() {
		super(Material.IRON);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityVT();
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Blocks.tryWrench(world, pos, player, hand, side, hitZ, hitZ, hitZ)) {
			return true;
		}
		if (!player.isSneaking()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityNetworkMember) {
				TileEntityNetworkMember tenm = (TileEntityNetworkMember)te;
				if (tenm.hasStorage()) {
					if (!world.isRemote) {
						switch (world.getBlockState(tenm.getStorage().getPos()).getValue(BlockController.state)) {
							case BOOTING:
								player.addChatMessage(new TextComponentTranslation("msg.correlatedpotentialistics.vt_booting"));
								break;
							case ERROR:
								player.addChatMessage(new TextComponentTranslation("msg.correlatedpotentialistics.vt_error"));
								break;
							case OFF:
								player.addChatMessage(new TextComponentTranslation("msg.correlatedpotentialistics.vt_no_power"));
								break;
							case POWERED:
								player.openGui(CoPo.inst, 0, world, pos.getX(), pos.getY(), pos.getZ());
								break;
							default:
								break;

						}
					}
					return true;
				}
			}
			if (!world.isRemote) {
				player.addChatMessage(new TextComponentTranslation("msg.correlatedpotentialistics.vt_no_controller"));
			}
			return true;
		}
		return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
	}

}
