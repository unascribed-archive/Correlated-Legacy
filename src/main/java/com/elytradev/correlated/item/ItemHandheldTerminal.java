package com.elytradev.correlated.item;

import com.elytradev.correlated.Correlated;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemHandheldTerminal extends Item {
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (!worldIn.isRemote) {
			playerIn.openGui(Correlated.inst, 3, worldIn, handIn == EnumHand.OFF_HAND ? -1 : playerIn.inventory.currentItem, 0, 0);
		}
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}
	
}
