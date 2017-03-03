package com.elytradev.correlated.block;

import java.util.List;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.helper.Blocks;
import com.elytradev.correlated.tile.TileEntityMicrowaveBeam;
import com.elytradev.correlated.wifi.Beam;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class BlockMicrowaveBeam extends Block {
	
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
	
	public static final IProperty<State> state = PropertyEnum.create("state", State.class);
	
	private AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 0.3125, 1);
	
	public BlockMicrowaveBeam() {
		super(Material.IRON);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}	

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean piston) {
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
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return aabb;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityMicrowaveBeam();
	}
	
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityMicrowaveBeam && !world.isRemote) {
			TileEntityMicrowaveBeam temb = (TileEntityMicrowaveBeam)te;
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("OtherSide", NBT.TAG_LONG)) {
				BlockPos other = BlockPos.fromLong(stack.getTagCompound().getLong("OtherSide"));
				if (world.getTileEntity(other) instanceof TileEntityMicrowaveBeam) {
					temb.link(other);
				}
			}
		}
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, state);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(BlockMicrowaveBeam.state).ordinal() & 0b0111);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(state, State.VALUES[meta%State.VALUES.length]);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityMicrowaveBeam) {
			CorrelatedWorldData data = Correlated.getDataFor(world);
			Beam b = data.getWirelessManager().getBeam(pos);
			data.getWirelessManager().remove(b);
		}
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Blocks.tryWrench(world, pos, player, hand, side, hitZ, hitZ, hitZ)) {
			return true;
		}
		return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
	}
}
