package com.elytradev.correlated.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemKeycard extends Item {
	public static final String[] colors = {
			"amber",
			"black",
			"green",
			"indigo",
			"pink",
			"red",
			"sky",
			"white",
			"yellow"
		};
	
	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack)+"_"+colors[stack.getItemDamage()%colors.length];
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.translateToLocal("tooltip.correlated.keycard"));
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (int i = 0; i < colors.length; i++) {
			subItems.add(new ItemStack(this, 1, i));
		}
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}
}
