package com.elytradev.correlated.item;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.helper.Numbers;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;

public class ItemMemory extends Item {
	private final int[] tierSizes = {
			1024 * 8,
			4096 * 8,
	};

	public ItemMemory() {
		setMaxStackSize(1);
	}

	public int getTierColor(ItemStack stack) {
		return Correlated.proxy.getColor("tier", stack.getMetadata());
	}

	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.translateToLocalFormatted("item.correlated.memory.normal.name", Numbers.humanReadableBits(getMaxBits(stack)));
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (int i = 0; i < tierSizes.length; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	public int getMaxBits(ItemStack stack) {
		return tierSizes[stack.getItemDamage() % tierSizes.length];
	}

}
