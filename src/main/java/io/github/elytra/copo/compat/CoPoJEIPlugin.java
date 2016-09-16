package io.github.elytra.copo.compat;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.inventory.ContainerVT;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class CoPoJEIPlugin extends BlankModPlugin {

	@Override
	public void register(IModRegistry registry) {
		registry.addRecipeCategoryCraftingItem(new ItemStack(CoPo.vt), VanillaRecipeCategoryUid.CRAFTING);
		registry.getRecipeTransferRegistry().addRecipeTransferHandler(new IRecipeTransferHandler() {
			
			@Override
			public IRecipeTransferError transferRecipe(Container container, IRecipeLayout layout, EntityPlayer player, boolean max, boolean doTransfer) {
				if (doTransfer) {
					
				}
				return null;
			}
			
			@Override
			public String getRecipeCategoryUid() {
				return VanillaRecipeCategoryUid.CRAFTING;
			}
			
			@Override
			public Class<? extends Container> getContainerClass() {
				return ContainerVT.class;
			}
		});
	}

}
