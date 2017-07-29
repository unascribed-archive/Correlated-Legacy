package com.elytradev.correlated.compat;

import java.util.List;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.inventory.ContainerTerminal;
import com.elytradev.correlated.network.inventory.RecipeTransferMessage;
import com.google.common.collect.Lists;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class CorrelatedJEIPlugin extends BlankModPlugin {

	@Override
	public void register(IModRegistry registry) {
		Correlated.inst.jeiAvailable = true;
		registry.addRecipeCategoryCraftingItem(new ItemStack(CBlocks.TERMINAL), VanillaRecipeCategoryUid.CRAFTING);
		registry.getRecipeTransferRegistry().addRecipeTransferHandler(new IRecipeTransferHandler<ContainerTerminal>() {
			
			@Override
			public IRecipeTransferError transferRecipe(ContainerTerminal container, IRecipeLayout layout, EntityPlayer player, boolean max, boolean doTransfer) {
				if (doTransfer) {
					List<List<ItemStack>> matrix = Lists.newArrayList();
					for (int i = 0; i < 9; i++) {
						List<ItemStack> possibilities = Lists.newArrayList();
						IGuiIngredient<ItemStack> ingredient = layout.getItemStacks().getGuiIngredients().get(i+1);
						possibilities.addAll(ingredient.getAllIngredients());
						matrix.add(possibilities);
					}
					new RecipeTransferMessage(container.windowId, matrix, max).sendToServer();
				}
				return null;
			}
			
			@Override
			public Class<ContainerTerminal> getContainerClass() {
				return ContainerTerminal.class;
			}
		}, VanillaRecipeCategoryUid.CRAFTING);
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		Correlated.inst.jeiQueryUpdater = jeiRuntime.getItemListOverlay()::setFilterText;
		Correlated.inst.jeiQueryReader = jeiRuntime.getItemListOverlay()::getFilterText;
	}

}
