package io.github.elytra.copo.client.render.entity.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelAutomaton extends ModelBase {
	private ModelRenderer body;
	public ModelAutomaton() {
		body = new ModelRenderer(this);
		body.setTextureSize(32, 16);
		body.setTextureOffset(0, 0);
		body.addBox(-4, 15, -4, 8, 8, 8, 0.5f);
	}
	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		body.rotateAngleY = netHeadYaw * 0.017453292F;
		body.render(scale);
	}
}
