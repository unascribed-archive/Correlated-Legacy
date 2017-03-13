package com.elytradev.correlated.wifi;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.util.math.BlockPos;

public interface IWirelessClient {
	void setAPNs(Set<String> apn);
	Set<String> getAPNs();
	BlockPos getPosition();
	double getX();
	double getY();
	double getZ();
	
	default void setAPNs(String... apns) {
		setAPNs(Sets.newHashSet(apns));
	}
}
