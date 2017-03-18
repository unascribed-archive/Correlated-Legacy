package com.elytradev.correlated.item;

import com.elytradev.correlated.network.OpenDocumentationMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemDocTablet extends Item {

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (!worldIn.isRemote) {
			boolean unlocked = playerIn.getEntityData().getBoolean("correlated:fullDocs");
			boolean seenAnimation = playerIn.getEntityData().getBoolean("correlated:seenAnimation");
			new OpenDocumentationMessage("index", unlocked ? "full" : "abridged", !seenAnimation).sendTo(playerIn);
		}
		return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}
	
}
