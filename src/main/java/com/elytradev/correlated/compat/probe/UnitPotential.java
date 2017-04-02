package com.elytradev.correlated.compat.probe;

import com.elytradev.correlated.EnergyHelper;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.probe.api.UnitDictionary;
import com.elytradev.probe.api.impl.SIUnit;

public class UnitPotential extends SIUnit {

	public static final UnitPotential INSTANCE = new UnitPotential();
	
	public UnitPotential() {
		super("potential", "p", 0x00DBAD);
	}
	
	@Override
	public int getBarColor() {
		return CConfig.preferredUnit.color;
	}
	
	@Override
	public String getAbbreviation() {
		return CConfig.preferredUnit.abbreviation;
	}
	
	@Override
	public String format(double d) {
		return super.format(EnergyHelper.convertFromPotential((int)d, CConfig.preferredUnit));
	}

	public static void register() {
		UnitDictionary.getInstance().register(INSTANCE);
	}
	
}
