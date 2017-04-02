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

public class BlockDecor extends Block {
	
	public enum Variant implements IStringSerializable {
		DUNGEONCRETE,
		DUNGEONCRETE_GRATE,
		DUNGEONCRETE_LARGETILE,
		DUNGEONCRETE_VERTICAL,
		
		ELUCID_BRICK,
		ELUCID_GRIT,
		ELUCID_SCALE,
		
		PLATING;
		
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
	
	public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
	
	public BlockDecor() {
		super(Material.IRON);
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
	public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
		for (Variant v : Variant.VALUES) {
			list.add(new ItemStack(itemIn, 1, v.ordinal()));
		}
	}
	
}
