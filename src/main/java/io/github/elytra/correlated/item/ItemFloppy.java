package io.github.elytra.correlated.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemFloppy extends Item {
	public ItemFloppy() {
		setHasSubtypes(true);
		setMaxStackSize(1);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (isWriteProtected(stack)) {
			tooltip.add("\u00A77"+I18n.translateToLocal("tooltip.correlated.floppy.write_protected"));
		}
	}
	
	public boolean isWriteProtected(ItemStack stack) {
		return stack.getMetadata() == 1;
	}
	
	public void setWriteProtected(ItemStack stack, boolean writeProtected) {
		stack.setItemDamage(writeProtected ? 1 : 0);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		setWriteProtected(itemStackIn, !isWriteProtected(itemStackIn));
		return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
	}
}
