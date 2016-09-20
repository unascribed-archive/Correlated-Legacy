package io.github.elytra.copo.tile;

public class TileEntityVTImporter extends TileEntityImporter {

	public TileEntityVTImporter() {
		super(1);
	}
	
	@Override
	protected void doImport() {
		substitute(new TileEntityTerminal(), true);
	}

}
