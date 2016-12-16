package io.github.elytra.correlated.client.render.entity.layer;

import io.github.elytra.correlated.entity.EntityAutomaton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

public class LayerAutomatonHeldItems implements LayerRenderer<EntityAutomaton> {

	@Override
	public void doRenderLayer(EntityAutomaton entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		boolean flag = entitylivingbaseIn.getPrimaryHand() == EnumHandSide.RIGHT;
		ItemStack itemstack = flag ? entitylivingbaseIn.getHeldItemOffhand() : entitylivingbaseIn.getHeldItemMainhand();
		ItemStack itemstack1 = flag ? entitylivingbaseIn.getHeldItemMainhand() : entitylivingbaseIn.getHeldItemOffhand();

		if (itemstack != null || itemstack1 != null) {
			GlStateManager.pushMatrix();
			GlStateManager.rotate(netHeadYaw, 0, 1, 0);
			this.renderHeldItem(entitylivingbaseIn, itemstack1, ItemCameraTransforms.TransformType.FIXED, EnumHandSide.RIGHT);
			this.renderHeldItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.FIXED, EnumHandSide.LEFT);
			GlStateManager.popMatrix();
		}
	}

	private void renderHeldItem(EntityAutomaton automaton, ItemStack stack, ItemCameraTransforms.TransformType transform, EnumHandSide handSide) {
		if (stack != null) {
			GlStateManager.pushMatrix();
			boolean flag = handSide == EnumHandSide.LEFT;
			GlStateManager.translate(flag ? -0.165f : 0.165f, 1.25f, 0.05f);
			if (flag && stack.getItem() == Items.SHIELD) {
				// ??
				GlStateManager.translate(0, -0.5f, 0);
			}
			if (flag || stack.getItem() != Items.BOW) {
				GlStateManager.rotate(90, 1, 0, 0);
			}
			GlStateManager.rotate(flag ? 90 : -90, 0, 1, 0);
			GlStateManager.rotate(-180, 1, 0, 0);
			GlStateManager.scale(0.75f, 0.75f, 0.75f);
			GlStateManager.translate(0.0625F, 0.125F, -0.625F);
			Minecraft.getMinecraft().getItemRenderer().renderItemSide(automaton, stack, transform, flag);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

}
