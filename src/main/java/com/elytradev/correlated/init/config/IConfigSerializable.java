package com.elytradev.correlated.init.config;

public interface IConfigSerializable {
	public String toConfigString();
	public boolean matches(String configName);
}
