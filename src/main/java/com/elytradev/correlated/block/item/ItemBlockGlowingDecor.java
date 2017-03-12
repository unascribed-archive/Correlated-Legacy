package com.elytradev.correlated.block.item;

import com.elytradev.correlated.block.BlockGlowingDecor;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockGlowingDecor extends ItemBlock {

	public ItemBlockGlowingDecor(Block block) {
		super(block);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "tile.correlated."+BlockGlowingDecor.Variant.VALUES[stack.getMetadata()%BlockGlowingDecor.Variant.VALUES.length].getName();
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}

}
