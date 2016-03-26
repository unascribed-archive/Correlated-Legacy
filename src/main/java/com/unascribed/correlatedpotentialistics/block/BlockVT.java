package com.unascribed.correlatedpotentialistics.block;

import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.tile.TileEntityNetworkMember;
import com.unascribed.correlatedpotentialistics.tile.TileEntityVT;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockVT extends Block {
	public static final IProperty<EnumFacing> facing = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public BlockVT() {
		super(Material.iron);
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityNetworkMember) {
			TileEntityNetworkMember tenm = (TileEntityNetworkMember)te;
			if (tenm.hasController()) {
				if (!world.isRemote) {
					switch (world.getBlockState(tenm.getController().getPos()).getValue(BlockController.state)) {
						case BOOTING:
							player.addChatMessage(new ChatComponentTranslation("msg.correlatedpotentialistics.vt_booting"));
							break;
						case ERROR:
							player.addChatMessage(new ChatComponentTranslation("msg.correlatedpotentialistics.vt_error"));
							break;
						case OFF:
							player.addChatMessage(new ChatComponentTranslation("msg.correlatedpotentialistics.vt_no_power"));
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
			player.addChatMessage(new ChatComponentTranslation("msg.correlatedpotentialistics.vt_no_controller"));
		}
		return true;
	}
	
}
