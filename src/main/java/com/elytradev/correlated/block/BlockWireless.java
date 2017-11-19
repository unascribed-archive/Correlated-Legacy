package com.elytradev.correlated.block;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.elytradev.correlated.ColorType;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.helper.Blocks;
import com.elytradev.correlated.tile.TileEntityBeaconLens;
import com.elytradev.correlated.tile.TileEntityMicrowaveBeam;
import com.elytradev.correlated.tile.TileEntityOpticalTransceiver;
import com.elytradev.correlated.wifi.Beacon;
import com.elytradev.correlated.wifi.Beam;
import com.elytradev.correlated.wifi.Optical;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class BlockWireless extends Block {
	
	public enum State implements IStringSerializable {
		DEAD,
		ERROR,
		OK;
		public static final State[] VALUES = values();
		private final String lowerName;
		private State() {
			lowerName = name().toLowerCase();
		}
		@Override public String getName() { return lowerName; }
		@Override public String toString() { return lowerName; }
	}
	
	public enum Variant implements IStringSerializable {
		MICROWAVE,
		OPTICAL,
		BEACON;
		public static final Variant[] VALUES = values();
		private final String lowerName;
		private Variant() {
			lowerName = name().toLowerCase();
		}
		@Override public String getName() { return lowerName; }
		@Override public String toString() { return lowerName; }
	}
	
	public static final IProperty<State> STATE = PropertyEnum.create("state", State.class);
	public static final IProperty<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
	
	@SuppressWarnings("unchecked")
	private static final ImmutableList<Pair<Variant, State>> PERMUTATIONS = ImmutableList.of(
			Pair.of(Variant.MICROWAVE, State.DEAD),
			Pair.of(Variant.MICROWAVE, State.ERROR),
			Pair.of(Variant.MICROWAVE, State.OK),
			
			Pair.of(Variant.OPTICAL, State.DEAD),
			Pair.of(Variant.OPTICAL, State.ERROR),
			Pair.of(Variant.OPTICAL, State.OK),
			
			Pair.of(Variant.BEACON, State.DEAD),
			Pair.of(Variant.BEACON, State.ERROR),
			Pair.of(Variant.BEACON, State.OK),
			
			Pair.of(null, State.DEAD),
			Pair.of(null, State.ERROR),
			Pair.of(null, State.OK),
			
			Pair.of(null, State.DEAD),
			Pair.of(null, State.ERROR),
			Pair.of(null, State.OK)
			);
	
	private AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 0.3125, 1);
	
	public BlockWireless() {
		super(Material.IRON);
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).ordinal();
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote && state.getValue(VARIANT) == Variant.BEACON) {
			BlockBeacon.updateColorAsync(worldIn, pos);
		}
	}
	
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(VARIANT) == Variant.OPTICAL ? 255 : 0;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return state.getValue(VARIANT) == Variant.OPTICAL;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return state.getValue(VARIANT) == Variant.OPTICAL;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public float[] getBeaconColorMultiplier(IBlockState state, World world, BlockPos pos, BlockPos beaconPos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityBeaconLens) {
			String name = "primary";
			switch (state.getValue(BlockWireless.STATE)) {
				case DEAD:
					return null;
				case ERROR:
					name = "error";
					break;
				default:
					break;
			}
			int c = ColorType.OTHER.getColor(name);
			return new float[] {
				((c >> 16) & 0xFF) / 255f,
				((c >> 8) & 0xFF) / 255f,
				(c & 0xFF) / 255f
			};
		}
		return null;
	}
	
	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.SOLID;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.SOLID;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean piston) {
		if (state.getValue(VARIANT) == Variant.OPTICAL) {
			super.addCollisionBoxToList(state, worldIn, pos, mask, list, collidingEntity, piston);
			return;
		}
		AxisAlignedBB base = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX()+1, pos.getY()+(4/16d), pos.getZ()+1);
		if (mask.intersects(base)) {
			list.add(base);
		}
		if (state.getValue(VARIANT) == Variant.MICROWAVE) {
			AxisAlignedBB pole = new AxisAlignedBB(pos.getX()+(7/16d), pos.getY()+(4/16d), pos.getZ()+(7/16d), pos.getX()+(9/16d), pos.getY()+(16/16d), pos.getZ()+(9/16d));
			if (mask.intersects(pole)) {
				list.add(pole);
			}
		}
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return state.getValue(VARIANT) == Variant.OPTICAL ? FULL_BLOCK_AABB : aabb;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		switch (state.getValue(VARIANT)) {
			case MICROWAVE:
				return new TileEntityMicrowaveBeam();
			case OPTICAL:
				return new TileEntityOpticalTransceiver();
			case BEACON:
				return new TileEntityBeaconLens();
		}
		return null;
	}
	
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity te = world.getTileEntity(pos);
		if (!world.isRemote) {
			if (te instanceof TileEntityMicrowaveBeam) {
				TileEntityMicrowaveBeam temb = (TileEntityMicrowaveBeam)te;
				if (stack.hasTagCompound() && stack.getTagCompound().hasKey("OtherSide", NBT.TAG_LONG)) {
					BlockPos other = BlockPos.fromLong(stack.getTagCompound().getLong("OtherSide"));
					if (world.getTileEntity(other) instanceof TileEntityMicrowaveBeam) {
						temb.link(other);
					}
				}
			} else if (te instanceof TileEntityOpticalTransceiver) {
				CorrelatedWorldData data = CorrelatedWorldData.getFor(world);
				Optical o = new Optical(data, pos, 32, placer.getName());
				data.getWirelessManager().add(o);
			} else if (te instanceof TileEntityBeaconLens) {
				CorrelatedWorldData data = CorrelatedWorldData.getFor(world);
				Beacon b = new Beacon(data, pos, ImmutableSet.of(placer.getName()));
				data.getWirelessManager().add(b);
			}
		}
	}
	
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
		list.add(new ItemStack(this, 1, 2));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, STATE, VARIANT);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return PERMUTATIONS.indexOf(Pair.of(state.getValue(VARIANT), state.getValue(BlockWireless.STATE)));
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(VARIANT, PERMUTATIONS.get(meta).getLeft())
				.withProperty(STATE, PERMUTATIONS.get(meta).getRight());
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityMicrowaveBeam) {
			CorrelatedWorldData data = CorrelatedWorldData.getFor(world);
			Beam b = data.getWirelessManager().getBeam(pos);
			data.getWirelessManager().remove(b);
		} else if (te instanceof TileEntityOpticalTransceiver) {
			CorrelatedWorldData data = CorrelatedWorldData.getFor(world);
			Optical o = data.getWirelessManager().getOptical(pos);
			data.getWirelessManager().remove(o);
		} else if (te instanceof TileEntityBeaconLens) {
			CorrelatedWorldData data = CorrelatedWorldData.getFor(world);
			Beacon b = data.getWirelessManager().getBeacon(pos);
			data.getWirelessManager().remove(b);
		}
		if (!world.isRemote && state.getValue(VARIANT) == Variant.BEACON) {
			BlockBeacon.updateColorAsync(world, pos);
		}
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Blocks.tryWrench(world, pos, player, hand, side, hitZ, hitZ, hitZ)) {
			return true;
		}
		if (state.getValue(VARIANT) == Variant.OPTICAL) {
			Correlated.proxy.showAPNChangeMenu(pos, false, false);
			return true;
		} else if (state.getValue(VARIANT) == Variant.BEACON) {
			Correlated.proxy.showAPNChangeMenu(pos, true, true);
			return true;
		}
		return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
	}
}
