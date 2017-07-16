package com.elytradev.correlated.block.item;

import java.util.List;

import com.elytradev.correlated.EnergyHelper;
import com.elytradev.correlated.init.CConfig;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBlockDriveBay extends ItemBlock {

	public ItemBlockDriveBay(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(EnergyHelper.formatPotentialUsage(CConfig.driveBayPUsage));
	}

}
