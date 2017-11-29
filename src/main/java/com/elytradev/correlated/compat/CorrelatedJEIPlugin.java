package com.elytradev.correlated.compat;

import java.util.List;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.client.gui.GuiTerminal;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.inventory.ContainerTerminal;
import com.google.common.collect.Lists;

import mezz.jei.Internal;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@JEIPlugin
public class CorrelatedJEIPlugin implements IModPlugin {

	private IIngredientHelper<ItemStack> itemStackHelper;
	
	@Override
	public void register(IModRegistry registry) {
		Correlated.inst.jeiAvailable = true;
		registry.addRecipeCatalyst(new ItemStack(CBlocks.TERMINAL), VanillaRecipeCategoryUid.CRAFTING);
		registry.getRecipeTransferRegistry().addRecipeTransferHandler(new IRecipeTransferHandler<ContainerTerminal>() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public IRecipeTransferError transferRecipe(ContainerTerminal container, IRecipeLayout layout, EntityPlayer player, boolean max, boolean doTransfer) {
				if (doTransfer) {
					List<List<ItemStack>> matrix = Lists.newArrayList();
					for (int i = 0; i < 9; i++) {
						List<ItemStack> possibilities = Lists.newArrayList();
						IGuiIngredient<ItemStack> ingredient = layout.getItemStacks().getGuiIngredients().get(i+1);
						possibilities.addAll(ingredient.getAllIngredients());
						matrix.add(possibilities);
					}
					if (Minecraft.getMinecraft().currentScreen instanceof RecipesGui) {
						RecipesGui rg = (RecipesGui)Minecraft.getMinecraft().currentScreen;
						if (rg.getParentScreen() instanceof GuiTerminal) {
							GuiTerminal gt = (GuiTerminal)rg.getParentScreen();
							gt.setRecipe(matrix);
						}
					}
				}
				return null;
			}
			
			@Override
			public Class<ContainerTerminal> getContainerClass() {
				return ContainerTerminal.class;
			}
		}, VanillaRecipeCategoryUid.CRAFTING);
		itemStackHelper = registry.getIngredientRegistry().getIngredientHelper(ItemStack.class);
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		Correlated.inst.jeiQueryUpdater = jeiRuntime.getIngredientFilter()::setFilterText;
		Correlated.inst.jeiQueryReader = jeiRuntime.getIngredientFilter()::getFilterText;
		Correlated.inst.colorSearcher = (query, stack) -> {
			for (String name : Internal.getColorNamer().getColorNames(itemStackHelper.getColors(stack), true)) {
				if (name.contains(query)) return true;
			}
			return false;
		};
	}

}
