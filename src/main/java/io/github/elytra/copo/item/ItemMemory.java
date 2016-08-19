package io.github.elytra.copo.item;

import java.util.List;
import io.github.elytra.copo.helper.Numbers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class ItemMemory extends Item {
	private final int[] tierColors = {
			0xFF1744, // Red A400
			0xFF9100, // Orange A400
			0x76FF03, // Light Green A400
			0x1DE9B6, // Teal A400
			0x00B0FF, // Light Blue A400
	};
	private final int[] tierSizes = {
			1024 * 8,
			4096 * 8,
			16384 * 8,
			65536 * 8,
			131072 * 8
	};

	public ItemMemory() {
		setMaxStackSize(1);
	}

	public int getTierColor(ItemStack stack) {
		return tierColors[stack.getItemDamage() % tierColors.length];
	}

	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.getMetadata() == 5) return I18n.translateToLocal("item.correlatedpotentialistics.memory.infinite.name");
		return I18n.translateToLocalFormatted("item.correlatedpotentialistics.memory.normal.name", Numbers.humanReadableBytes(getMaxBits(stack)/8));
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < tierColors.length; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	public int getMaxBits(ItemStack stack) {
		return tierSizes[stack.getItemDamage() % tierSizes.length];
	}

}
