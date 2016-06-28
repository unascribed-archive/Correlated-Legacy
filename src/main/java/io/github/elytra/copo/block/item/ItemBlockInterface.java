package io.github.elytra.copo.block.item;

import java.util.List;

import io.github.elytra.copo.CoPo;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockInterface extends ItemBlock {

	public ItemBlockInterface(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(I18n.format("tooltip.correlatedpotentialistics.rf_usage", CoPo.inst.interfaceRfUsage));
	}

}
