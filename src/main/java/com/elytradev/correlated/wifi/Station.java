package com.elytradev.correlated.wifi;

import java.util.List;
import java.util.Set;

import com.elytradev.correlated.storage.IDigitalStorage;
import com.google.common.collect.Sets;

public interface Station {
	Set<String> getAPNs();
	boolean isInRange(double x, double y, double z);
	double distanceTo(double x, double y, double z);
	double getRadius();
	boolean isOperational();
	
	List<IDigitalStorage> getStorages(String apn, Set<Station> alreadyChecked);
	
	default List<IDigitalStorage> getStorages(String apn) {
		return getStorages(apn, Sets.newHashSet(this));
	}
}
