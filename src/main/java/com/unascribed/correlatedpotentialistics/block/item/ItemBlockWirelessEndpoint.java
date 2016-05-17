package com.unascribed.correlatedpotentialistics.block.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockWirelessEndpoint extends ItemBlock {

	public ItemBlockWirelessEndpoint(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(I18n.format("tooltip.correlatedpotentialistics.rf_usage", 24));
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return stack.getItemDamage() == 0 ? 
				"tile.correlatedpotentialistics.wireless_receiver" :
				"tile.correlatedpotentialistics.wireless_transmitter";
	}
	
	@Override
	public boolean getHasSubtypes() {
		return true;
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}
	
}
