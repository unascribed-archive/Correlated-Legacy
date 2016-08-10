package io.github.elytra.copo.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

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
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(I18n.translateToLocal("tooltip.correlatedpotentialistics.keycard"));
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < colors.length; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}
}
