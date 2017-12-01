package com.elytradev.correlated;

import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.C28n;

public class EnergyHelper {

	public static double convertToPotential(double input, EnergyUnit unit) {
		switch (unit) {
			case DANKS:
			case TESLA:
				return (input / CConfig.teslaConversionRate);
			case ENERGY_UNITS:
				return (input / CConfig.euConversionRate);
			case FORGE_UNITS:
			case FORGE_ENERGY:
				return (input / CConfig.fuConversionRate);
			case JOULES:
				return (input / CConfig.jConversionRate); 
			case MINECRAFT_JOULES:
				return (input / CConfig.mjConversionRate);
			case REDSTONE_FLUX:
				return (input / CConfig.rfConversionRate);
			case POTENTIAL:
			default:
				return input;
			
		}
	}

	public static double convertFromPotential(double input, EnergyUnit unit) {
		switch (unit) {
			case DANKS:
			case TESLA:
				return (int)(input * CConfig.teslaConversionRate);
			case ENERGY_UNITS:
				return (int)(input * CConfig.euConversionRate);
			case FORGE_UNITS:
			case FORGE_ENERGY:
				return (int)(input * CConfig.fuConversionRate);
			case JOULES:
				return (int)(input * CConfig.jConversionRate); 
			case MINECRAFT_JOULES:
				return (int)(input * CConfig.mjConversionRate);
			case REDSTONE_FLUX:
				return (int)(input * CConfig.rfConversionRate);
			case POTENTIAL:
			default:
				return input;
			
		}
	}

	public static String formatPotentialUsage(double p) {
		return C28n.format("tooltip.correlated.energy_usage_tip", convertFromPotential(p, CConfig.preferredUnit), CConfig.preferredUnit.abbreviation);
	}

}
