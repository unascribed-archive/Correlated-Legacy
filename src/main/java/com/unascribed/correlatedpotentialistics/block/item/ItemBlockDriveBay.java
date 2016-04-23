package com.unascribed.correlatedpotentialistics.block.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockDriveBay extends ItemBlock {

	public ItemBlockDriveBay(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(I18n.format("tooltip.correlatedpotentialistics.rf_usage", 8));
	}

}
