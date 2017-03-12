package com.elytradev.correlated.block.item;

import com.elytradev.correlated.block.BlockDecor;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockDecor extends ItemBlock {

	public ItemBlockDecor(Block block) {
		super(block);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "tile.correlated."+BlockDecor.Variant.VALUES[stack.getMetadata()%BlockDecor.Variant.VALUES.length].getName();
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}

}
