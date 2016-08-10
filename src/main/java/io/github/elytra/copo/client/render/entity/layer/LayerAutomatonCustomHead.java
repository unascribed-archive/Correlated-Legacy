package io.github.elytra.copo.client.render.entity.layer;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.entity.EntityLivingBase;

public class LayerAutomatonCustomHead extends LayerCustomHead {

	public LayerAutomatonCustomHead(ModelRenderer p_i46120_1_) {
		super(p_i46120_1_);
	}
	
	@Override
	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.45f, 0);
		super.doRenderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.popMatrix();
	}

}
