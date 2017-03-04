package com.elytradev.correlated.block;

import java.util.Random;

import com.elytradev.correlated.Correlated;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class BlockDungeonLithographeneSlab extends BlockSlab {

	private boolean dbl;
	
	public BlockDungeonLithographeneSlab(boolean dbl) {
		super(Material.IRON);
		this.dbl = dbl;
		IBlockState ibs = blockState.getBaseState();

		if (!this.isDouble()) {
			ibs = ibs.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
		}

		setDefaultState(ibs.withProperty(BlockDungeonLithographene.variant, BlockDungeonLithographene.Variant.OFF));
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(Correlated.dungeon_slab);
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(BlockDungeonLithographene.variant).ordinal();
	}
	
	@Override
	public String getUnlocalizedName(int meta) {
		return "tile.correlated.lithographene_"+getTypeForMeta(meta).getName()+"_slab";
	}

	@Override
	public boolean isDouble() {
		return dbl;
	}

	@Override
	public IProperty<?> getVariantProperty() {
		return BlockDungeonLithographene.variant;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState iblockstate = this.getDefaultState();

		if (!isDouble()) {
			iblockstate = iblockstate.withProperty(HALF,
					(meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM
							: BlockSlab.EnumBlockHalf.TOP);
		}
		
		iblockstate = iblockstate.withProperty(BlockDungeonLithographene.variant, BlockDungeonLithographene.Variant.VALUES[(meta & 7)%BlockDungeonLithographene.Variant.VALUES.length]);
		
		return iblockstate;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(BlockDungeonLithographene.variant).ordinal();

		if (!isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
			i |= 8;
		}

		return i;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return isDouble()
				? new BlockStateContainer(this, BlockDungeonLithographene.variant)
				: new BlockStateContainer(this, HALF, BlockDungeonLithographene.variant);
	}

	@Override
	public BlockDungeonLithographene.Variant getTypeForItem(ItemStack stack) {
		return getTypeForMeta(stack.getMetadata());
	}
	
	public BlockDungeonLithographene.Variant getTypeForMeta(int meta) {
		return BlockDungeonLithographene.Variant.VALUES[meta%BlockDungeonLithographene.Variant.VALUES.length];
	}
	
	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
		for (BlockDungeonLithographene.Variant v : BlockDungeonLithographene.Variant.VALUES) {
			list.add(new ItemStack(itemIn, 1, v.ordinal()));
		}
	}

}
