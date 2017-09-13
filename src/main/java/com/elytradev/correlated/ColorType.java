package com.elytradev.correlated;

public enum ColorType {
	TIER,
	FADE,
	MODULE,
	PALETTE,
	OTHER;
	
	public int getColor(int idx) {
		return Correlated.proxy.getColorValues(this).getColor(idx);
	}
	
	public int getColor(String name) {
		return Correlated.proxy.getColorValues(this).getColor(name);
	}
	
}