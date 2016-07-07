package io.github.elytra.copo.compat;

import io.github.elytra.copo.CoPo;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class CoPoJEIPlugin extends BlankModPlugin {

	@Override
	public void register(IModRegistry registry) {
		registry.addRecipeCategoryCraftingItem(new ItemStack(CoPo.vt), VanillaRecipeCategoryUid.CRAFTING);
	}

}
