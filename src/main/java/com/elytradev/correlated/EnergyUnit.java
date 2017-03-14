package com.elytradev.correlated;

public enum EnergyUnit {
	POTENTIAL("Potential", "P", 0x00DBAD),
	REDSTONE_FLUX("Redstone Flux", "RF", 0xD50000),
	ENERGY_UNITS("Energy Units", "EU", 0xD50000),
	TESLA("Tesla", "T", 0x2196F3),
	MINECRAFT_JOULES("Minecraft Joules", "MJ", 0xD50000),
	JOULES("Joules", "J", 0x4CAF50),
	GLYPHS("Glyphs", "gl", 0xD500F9),
	DANKS("Danks", "Dk", 0x673AB7),
	FORGE_UNITS("Forge Units", "FU", 0xD50000),
	FORGE_ENERGY("Forge Energy", "FE", 0xD50000);
	public final String displayName;
	public final String abbreviation;
	public final int color;
	private EnergyUnit(String displayName, String abbreviation, int color) {
		this.displayName = displayName;
		this.abbreviation = abbreviation;
		this.color = color;
	}
}
