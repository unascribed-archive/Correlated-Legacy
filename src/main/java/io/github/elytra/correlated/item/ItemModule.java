package io.github.elytra.correlated.item;

import java.util.List;

import io.github.elytra.correlated.Correlated;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class ItemModule extends Item {
	public static final String[] types = {
		"speech",
		"antigrav"
	};
	public ItemModule() {
		setMaxStackSize(1);
	}

	public int getTypeColor(ItemStack stack) {
		return Correlated.proxy.getColor("pci", stack.getMetadata());
	}
	
	public String getType(ItemStack stack) {
		if (stack.getMetadata() < 0 || stack.getMetadata() >= types.length) return null;
		return types[stack.getMetadata()];
	}

	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.translateToLocal("item.correlated.module."+getType(stack)+".name");
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < types.length; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

}
