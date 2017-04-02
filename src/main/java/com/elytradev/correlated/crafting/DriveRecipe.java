package com.elytradev.correlated.crafting;

import com.elytradev.correlated.init.CItems;
import com.elytradev.correlated.item.ItemDrive;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class DriveRecipe extends ShapedOreRecipe {

	public DriveRecipe(Block result, Object... recipe) {
		super(result, recipe);
	}

	public DriveRecipe(Item result, Object... recipe) {
		super(result, recipe);
	}

	public DriveRecipe(ItemStack result, Object... recipe) {
		super(result, recipe);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		ItemStack stack = super.getCraftingResult(var1);
		NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound().copy() : new NBTTagCompound();
		NBTTagList ingredients = new NBTTagList();
		for (int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack is = var1.getStackInSlot(i);
			if (!is.isEmpty()) {
				if (!is.getItem().hasContainerItem(is)) {
					NBTTagCompound ingredient = new NBTTagCompound();
					ItemStack copy = is.copy();
					copy.setCount(1);
					copy.writeToNBT(ingredient);
					ingredient.setInteger("Slot", i);
					ingredients.appendTag(ingredient);
				}
				if (is.getItem() == CItems.MISC && is.getMetadata() == 8) {
					if (stack.getItem() instanceof ItemDrive) {
						ItemDrive id = ((ItemDrive)stack.getItem());
						if (CItems.DRIVE.getKilobitsUsed(is) > id.getMaxKilobits(stack)) {
							return ItemStack.EMPTY;
						}
					}
					tag.merge(is.getTagCompound());
				}
			}
		}
		tag.setTag("Ingredients", ingredients);
		stack.setTagCompound(tag);
		return stack;
	}

}
