package com.elytradev.correlated.block.item;

import java.util.List;

import com.elytradev.correlated.EnergyHelper;
import com.elytradev.correlated.init.CConfig;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockTerminal extends ItemBlock {

	public ItemBlockTerminal(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(EnergyHelper.formatPotentialUsage(CConfig.terminalPUsage));
	}

}
