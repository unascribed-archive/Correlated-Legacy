package com.elytradev.correlated.item;

import com.elytradev.correlated.helper.Numbers;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import com.elytradev.correlated.C28n;
import com.elytradev.correlated.ColorType;

public class ItemMemory extends Item {
	public static final int[] tierSizes = {
			1024 * 8,
			4096 * 8,
	};

	public ItemMemory() {
		setMaxStackSize(1);
	}

	public int getTierColor(ItemStack stack) {
		return ColorType.TIER.getColor(stack.getMetadata());
	}

	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return C28n.format("item.correlated.memory.normal.name", Numbers.humanReadableBits(getMaxBits(stack)));
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			for (int i = 0; i < tierSizes.length; i++) {
				subItems.add(new ItemStack(this, 1, i));
			}
		}
	}

	public int getMaxBits(ItemStack stack) {
		return tierSizes[stack.getItemDamage() % tierSizes.length];
	}

}
