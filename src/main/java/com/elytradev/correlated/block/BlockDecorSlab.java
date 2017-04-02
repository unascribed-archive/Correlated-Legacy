package com.elytradev.correlated.block;

import java.util.Random;

import com.elytradev.correlated.init.CBlocks;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class BlockDecorSlab extends BlockSlab {

	private boolean dbl;
	
	public BlockDecorSlab(boolean dbl) {
		super(Material.IRON);
		this.dbl = dbl;
		IBlockState ibs = blockState.getBaseState();

		if (!this.isDouble()) {
			ibs = ibs.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
		}

		setDefaultState(ibs.withProperty(BlockDecor.VARIANT, BlockDecor.Variant.DUNGEONCRETE));
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(CBlocks.DECOR_SLAB);
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(BlockDecor.VARIANT).ordinal();
	}
	
	@Override
	public String getUnlocalizedName(int meta) {
		return "tile.correlated."+getTypeForMeta(meta).getName()+"_slab";
	}

	@Override
	public boolean isDouble() {
		return dbl;
	}

	@Override
	public IProperty<?> getVariantProperty() {
		return BlockDecor.VARIANT;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState iblockstate = this.getDefaultState();

		if (!isDouble()) {
			iblockstate = iblockstate.withProperty(HALF,
					(meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM
							: BlockSlab.EnumBlockHalf.TOP);
		}
		
		iblockstate = iblockstate.withProperty(BlockDecor.VARIANT, BlockDecor.Variant.VALUES[(meta & 7)%BlockDecor.Variant.VALUES.length]);
		
		return iblockstate;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(BlockDecor.VARIANT).ordinal();

		if (!isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
			i |= 8;
		}

		return i;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return isDouble()
				? new BlockStateContainer(this, BlockDecor.VARIANT)
				: new BlockStateContainer(this, HALF, BlockDecor.VARIANT);
	}

	@Override
	public BlockDecor.Variant getTypeForItem(ItemStack stack) {
		return getTypeForMeta(stack.getMetadata());
	}
	
	public BlockDecor.Variant getTypeForMeta(int meta) {
		return BlockDecor.Variant.VALUES[meta%BlockDecor.Variant.VALUES.length];
	}
	
	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
		for (BlockDecor.Variant v : BlockDecor.Variant.VALUES) {
			list.add(new ItemStack(itemIn, 1, v.ordinal()));
		}
	}

}
