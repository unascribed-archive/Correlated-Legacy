package com.elytradev.correlated.block.item;

import com.elytradev.correlated.block.BlockDungeonLithographene;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockDungeonLithographene extends ItemBlock {

	public ItemBlockDungeonLithographene(Block block) {
		super(block);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "tile.correlated.lithographene_"+BlockDungeonLithographene.Variant.VALUES[stack.getMetadata()%BlockDungeonLithographene.Variant.VALUES.length].getName();
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}

}
