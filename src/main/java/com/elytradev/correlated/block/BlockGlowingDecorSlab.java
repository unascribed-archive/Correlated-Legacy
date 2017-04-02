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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockGlowingDecorSlab extends BlockSlab {

	private boolean dbl;
	
	public BlockGlowingDecorSlab(boolean dbl) {
		super(Material.IRON);
		this.dbl = dbl;
		IBlockState ibs = blockState.getBaseState();

		if (!this.isDouble()) {
			ibs = ibs.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
		}

		setDefaultState(ibs.withProperty(BlockGlowingDecor.VARIANT, BlockGlowingDecor.Variant.LITHOGRAPHENE_OFF));
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(CBlocks.GLOWING_DECOR_SLAB);
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(BlockGlowingDecor.VARIANT).ordinal();
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
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(BlockGlowingDecor.VARIANT).glow ? 15 : 2;
	}

	@Override
	public IProperty<?> getVariantProperty() {
		return BlockGlowingDecor.VARIANT;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState iblockstate = this.getDefaultState();

		if (!isDouble()) {
			iblockstate = iblockstate.withProperty(HALF,
					(meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM
							: BlockSlab.EnumBlockHalf.TOP);
		}
		
		iblockstate = iblockstate.withProperty(BlockGlowingDecor.VARIANT, BlockGlowingDecor.Variant.VALUES[(meta & 7)%BlockGlowingDecor.Variant.VALUES.length]);
		
		return iblockstate;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(BlockGlowingDecor.VARIANT).ordinal();

		if (!isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
			i |= 8;
		}

		return i;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return isDouble()
				? new BlockStateContainer(this, BlockGlowingDecor.VARIANT)
				: new BlockStateContainer(this, HALF, BlockGlowingDecor.VARIANT);
	}

	@Override
	public BlockGlowingDecor.Variant getTypeForItem(ItemStack stack) {
		return getTypeForMeta(stack.getMetadata());
	}
	
	public BlockGlowingDecor.Variant getTypeForMeta(int meta) {
		return BlockGlowingDecor.Variant.VALUES[meta%BlockGlowingDecor.Variant.VALUES.length];
	}
	
	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
		for (BlockGlowingDecor.Variant v : BlockGlowingDecor.Variant.VALUES) {
			list.add(new ItemStack(itemIn, 1, v.ordinal()));
		}
	}

}
