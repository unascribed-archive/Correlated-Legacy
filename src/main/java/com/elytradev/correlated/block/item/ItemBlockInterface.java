package com.elytradev.correlated.block.item;

import java.util.List;

import com.elytradev.correlated.Correlated;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockInterface extends ItemBlock {

	public ItemBlockInterface(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(Correlated.formatPotentialUsage(Correlated.inst.interfacePUsage));
	}

}
