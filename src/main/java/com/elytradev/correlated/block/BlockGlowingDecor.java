package com.elytradev.correlated.block;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockGlowingDecor extends Block {
	
	public enum Variant implements IStringSerializable {
		LITHOGRAPHENE_OFF,
		LITHOGRAPHENE_ON,
		LITHOGRAPHENE_OFF_VARIANT,
		LITHOGRAPHENE_ON_VARIANT;
		
		public static final Variant[] VALUES = values();
		private final String lower;
		
		private Variant() {
			lower = name().toLowerCase(Locale.ROOT);
		}

		@Override
		public String getName() {
			return lower;
		}
	}
	
	public static final PropertyEnum<Variant> variant = PropertyEnum.create("variant", Variant.class);
	
	public BlockGlowingDecor() {
		super(Material.IRON);
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(variant).ordinal()%2 == 1 ? 15 : 2;
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(variant).ordinal();
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, variant);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(variant).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(variant, Variant.VALUES[meta%Variant.VALUES.length]);
	}
	
	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
		for (Variant v : Variant.VALUES) {
			list.add(new ItemStack(itemIn, 1, v.ordinal()));
		}
	}
	
}
