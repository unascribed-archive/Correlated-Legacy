package com.elytradev.correlated.init;

import com.elytradev.correlated.world.LimboProvider;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class CDimensions {
	
	public static DimensionType LIMBO;

	public static void register() {
		LIMBO = DimensionType.register("Limbo", "_correlateddungeon", CConfig.limboDimId, LimboProvider.class, false);
		DimensionManager.registerDimension(CConfig.limboDimId, LIMBO);
	}

}
