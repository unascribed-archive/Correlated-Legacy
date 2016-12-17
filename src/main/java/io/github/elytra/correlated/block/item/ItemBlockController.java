package io.github.elytra.correlated.block.item;

import java.util.List;

import io.github.elytra.correlated.Correlated;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockController extends ItemBlock {

	public ItemBlockController(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (stack.getMetadata() == 8) {
			tooltip.add(I18n.format("tooltip.correlated.rf_usage", 0));
		} else {
			tooltip.add(I18n.format("tooltip.correlated.rf_usage", Correlated.inst.controllerRfUsage));
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return stack.getMetadata() >= 8 ? "tile.correlated.cheaty_controller" : "tile.correlated.controller";
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
