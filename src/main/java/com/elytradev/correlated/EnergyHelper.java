package com.elytradev.correlated;

import com.elytradev.correlated.init.CConfig;
import com.google.common.primitives.Ints;

import net.minecraft.util.text.translation.I18n;

public class EnergyHelper {

	public static int convertToPotential(long input, EnergyUnit unit) {
		return convertToPotential(Ints.saturatedCast(input), unit);
	}

	public static int convertToPotential(int input, EnergyUnit unit) {
		switch (unit) {
			case DANKS:
			case TESLA:
				return (int)(input / CConfig.teslaConversionRate);
			case ENERGY_UNITS:
				return (int)(input / CConfig.euConversionRate);
			case FORGE_UNITS:
			case FORGE_ENERGY:
				return (int)(input / CConfig.fuConversionRate);
			case JOULES:
				return (int)(input / CConfig.jConversionRate); 
			case MINECRAFT_JOULES:
				return (int)(input / CConfig.mjConversionRate);
			case REDSTONE_FLUX:
				return (int)(input / CConfig.rfConversionRate);
			case POTENTIAL:
			default:
				return input;
			
		}
	}

	public static int convertFromPotential(int input, EnergyUnit unit) {
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

	@SuppressWarnings("deprecation")
	public static String formatPotentialUsage(int p) {
		return I18n.translateToLocalFormatted("tooltip.correlated.energy_usage_tip", convertFromPotential(p, CConfig.preferredUnit), CConfig.preferredUnit.abbreviation);
	}

}
