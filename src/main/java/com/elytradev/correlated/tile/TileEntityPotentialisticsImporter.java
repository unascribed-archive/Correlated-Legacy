package com.elytradev.correlated.tile;

import com.elytradev.correlated.Correlated;

public class TileEntityPotentialisticsImporter extends TileEntityImporter {

	// extra classes to make the registry happy
	
	public static class A extends TileEntityPotentialisticsImporter {}
	public static class B extends TileEntityPotentialisticsImporter {}
	public static class C extends TileEntityPotentialisticsImporter {}
	public static class D extends TileEntityPotentialisticsImporter {}
	public static class E extends TileEntityPotentialisticsImporter {}
	public static class F extends TileEntityPotentialisticsImporter {}
	public static class G extends TileEntityPotentialisticsImporter {}
	public static class H extends TileEntityPotentialisticsImporter {}
	
	public TileEntityPotentialisticsImporter() {
		super(1);
	}
	
	@Override
	protected void doImport() {
		String old = capturedNbt.getString("id");
		String nw = old.replace("potentialistics", "");
		Correlated.log.info("Replacing {} with {}", old, nw);
		substitute(nw, true);
	}

}
