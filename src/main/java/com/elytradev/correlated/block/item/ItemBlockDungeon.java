package com.elytradev.correlated.block.item;

import com.elytradev.correlated.block.BlockDungeon;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockDungeon extends ItemBlock {

	public ItemBlockDungeon(Block block) {
		super(block);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "tile.correlated."+BlockDungeon.Variant.VALUES[stack.getMetadata()%BlockDungeon.Variant.VALUES.length].getName();
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}

}
