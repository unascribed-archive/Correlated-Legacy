package com.elytradev.correlated.client.render.entity;

import com.elytradev.correlated.client.render.entity.layer.LayerAutomatonArmor;
import com.elytradev.correlated.client.render.entity.layer.LayerAutomatonCustomHead;
import com.elytradev.correlated.client.render.entity.layer.LayerAutomatonDrives;
import com.elytradev.correlated.client.render.entity.layer.LayerAutomatonHeldItems;
import com.elytradev.correlated.client.render.entity.layer.LayerAutomatonLights;
import com.elytradev.correlated.client.render.entity.model.ModelAutomaton;
import com.elytradev.correlated.entity.EntityAutomaton;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderAutomaton extends RenderLiving<EntityAutomaton> {
	private static final ResourceLocation AUTOMATON = new ResourceLocation("correlated", "textures/entity/automaton.png");
	
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
