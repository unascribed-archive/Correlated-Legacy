package com.elytradev.correlated;

import java.util.Locale;

import com.elytradev.correlated.init.config.IConfigSerializable;

public enum ImportMode implements IConfigSerializable {
	REFUND_ALL,
	REFUND_SOME,
	REFUND_CONTENT,
	DESTROY,
	LEAVE;
	
	@Override
	public String toConfigString() {
		return name().toLowerCase(Locale.ROOT);
	}

	@Override
	public boolean matches(String configName) {
		return configName.toUpperCase(Locale.ROOT).equals(name());
	}
}
