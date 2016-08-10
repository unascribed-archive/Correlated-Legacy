package io.github.elytra.copo.client.render.entity;

import io.github.elytra.copo.entity.EntityThrownItem;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class RenderThrownItem extends RenderSnowball<EntityThrownItem> {

	public RenderThrownItem(RenderManager renderManagerIn, RenderItem itemRendererIn) {
		super(renderManagerIn, Items.APPLE, itemRendererIn);
	}
	
	@Override
	public ItemStack getStackToRender(EntityThrownItem entityIn) {
		return entityIn.getStack();
	}
}
