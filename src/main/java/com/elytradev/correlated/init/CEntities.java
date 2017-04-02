package com.elytradev.correlated.init;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.entity.EntityAutomaton;
import com.elytradev.correlated.entity.EntityThrownItem;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class CEntities {

	public static void register() {
		EntityRegistry.registerModEntity(new ResourceLocation("correlated", "thrown_item"), EntityThrownItem.class, "thrown_item", 0, Correlated.inst, 64, 10, true);
		EntityRegistry.registerModEntity(new ResourceLocation("correlated", "automaton"), EntityAutomaton.class, "automaton", 1, Correlated.inst, 64, 1, true);
		
		EntityRegistry.registerEgg(new ResourceLocation("correlated", "automaton"), 0x37474F, 0x00F8C1);
	}

}
