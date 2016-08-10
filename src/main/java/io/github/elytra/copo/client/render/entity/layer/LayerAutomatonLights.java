package io.github.elytra.copo.client.render.entity.layer;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.elytra.copo.client.render.entity.RenderAutomaton;
import io.github.elytra.copo.entity.EntityAutomaton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class LayerAutomatonLights implements LayerRenderer<EntityAutomaton> {
	private static final ResourceLocation FRIENDLY = new ResourceLocation("correlatedpotentialistics", "textures/entity/automaton_eyes.png");
	private static final ResourceLocation ANGRY = new ResourceLocation("correlatedpotentialistics", "textures/entity/automaton_eyes_angry.png");
	
	private final RenderAutomaton render;
	private final List<ResourceLocation> locs = Lists.newArrayList();
	
	public LayerAutomatonLights(RenderAutomaton render) {
		this.render = render;
	}
	
	@Override
	public void doRenderLayer(EntityAutomaton en, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (en.getHealth() >= 1) {
			locs.clear();
			if (en.isAngry()) {
				locs.add(ANGRY);
			} else {
				locs.add(FRIENDLY);
			}
			locs.add(en.getStatus().texture);
			GlStateManager.disableLighting();
			GlStateManager.depthMask(!en.isInvisible());
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			GlStateManager.enableLighting();
			for (ResourceLocation loc : locs) {
				GlStateManager.color(1, 1, 1);
				render.bindTexture(loc);
				render.getMainModel().render(en, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			}
			render.setLightmap(en, partialTicks);
			GlStateManager.depthMask(true);
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}


}
