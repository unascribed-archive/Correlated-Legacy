package io.github.elytra.copo.client.render.entity;

import io.github.elytra.copo.client.render.entity.layer.LayerAutomatonArmor;
import io.github.elytra.copo.client.render.entity.layer.LayerAutomatonCustomHead;
import io.github.elytra.copo.client.render.entity.layer.LayerAutomatonDrives;
import io.github.elytra.copo.client.render.entity.layer.LayerAutomatonHeldItems;
import io.github.elytra.copo.client.render.entity.layer.LayerAutomatonLights;
import io.github.elytra.copo.client.render.entity.model.ModelAutomaton;
import io.github.elytra.copo.entity.EntityAutomaton;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderAutomaton extends RenderLiving<EntityAutomaton> {
	private static final ResourceLocation AUTOMATON = new ResourceLocation("correlatedpotentialistics", "textures/entity/automaton.png");
	
	public RenderAutomaton(RenderManager renderManager) {
		super(renderManager, new ModelAutomaton(), 0.5f);
		addLayer(new LayerAutomatonLights(this));
		addLayer(new LayerAutomatonArmor(this));
		addLayer(new LayerAutomatonCustomHead(mainModel.boxList.get(0)));
		addLayer(new LayerAutomatonHeldItems());
		addLayer(new LayerAutomatonDrives());
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityAutomaton entity) {
		return AUTOMATON;
	}

}
