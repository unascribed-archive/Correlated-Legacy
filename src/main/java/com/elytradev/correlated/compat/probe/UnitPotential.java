package com.elytradev.correlated.compat.probe;

import com.elytradev.correlated.Correlated;
import com.elytradev.probe.api.UnitDictionary;
import com.elytradev.probe.api.impl.SIUnit;

public class UnitPotential extends SIUnit {

	public static final UnitPotential INSTANCE = new UnitPotential();
	
	public UnitPotential() {
		super("potential", "p", 0x00DBAD);
	}
	
	@Override
	public int getBarColor() {
		return Correlated.inst.preferredUnit.color;
	}
	
	@Override
	public String getAbbreviation() {
		return Correlated.inst.preferredUnit.abbreviation;
	}
	
	@Override
	public String format(double d) {
		return super.format(Correlated.convertFromPotential((int)d, Correlated.inst.preferredUnit));
	}

	public static void register() {
		UnitDictionary.getInstance().register(INSTANCE);
	}
	
}
