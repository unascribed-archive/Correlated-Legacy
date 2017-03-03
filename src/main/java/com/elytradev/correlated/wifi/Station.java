package com.elytradev.correlated.wifi;

import java.util.Set;

import net.minecraft.entity.Entity;

public interface Station {
	Set<String> getAPNs();
	boolean isInRange(Entity e);
	double distanceTo(Entity e);
	double getRadius();
}
