package io.github.elytra.correlated.tile;

import io.github.elytra.correlated.Correlated;

public class TileEntityPotentialisticsImporter extends TileEntityImporter {

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
