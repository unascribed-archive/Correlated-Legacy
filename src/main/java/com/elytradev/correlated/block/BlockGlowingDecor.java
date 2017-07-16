package com.elytradev.correlated.block;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockGlowingDecor extends Block {
	
	public enum Variant implements IStringSerializable {
		LITHOGRAPHENE_OFF(false),
		LITHOGRAPHENE_ON(true),
		LITHOGRAPHENE_OFF_VARIANT(false),
		LITHOGRAPHENE_ON_VARIANT(true),
		LANTERN(true);
		
		public static final Variant[] VALUES = values();
		private final String lower;
		
		public final boolean glow;
		
		private Variant(boolean glow) {
			lower = name().toLowerCase(Locale.ROOT);
			this.glow = glow;
		}

		@Override
		public String getName() {
			return lower;
		}
	}
	
	public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
	
	public BlockGlowingDecor() {
		super(Material.IRON);
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(VARIANT).glow ? 15 : 2;
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).ordinal();
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(VARIANT, Variant.VALUES[meta%Variant.VALUES.length]);
	}
	
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (Variant v : Variant.VALUES) {
			list.add(new ItemStack(this, 1, v.ordinal()));
		}
	}
	
}
