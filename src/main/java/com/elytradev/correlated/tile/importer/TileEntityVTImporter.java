package com.elytradev.correlated.tile.importer;

import com.elytradev.correlated.tile.TileEntityTerminal;

public class TileEntityVTImporter extends TileEntityImporter {

	public TileEntityVTImporter() {
		super(2);
	}
	
	@Override
	protected void doImport() {
		substitute(new TileEntityTerminal(), true);
	}

}
