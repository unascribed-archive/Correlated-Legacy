package com.elytradev.correlated.tile;

public class TileEntityVTImporter extends TileEntityImporter {

	public TileEntityVTImporter() {
		super(2);
	}
	
	@Override
	protected void doImport() {
		substitute(new TileEntityTerminal(), true);
	}

}
