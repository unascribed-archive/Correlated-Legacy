package com.elytradev.correlated.client.gui;

import com.elytradev.correlated.inventory.ContainerImporterChest;

import com.elytradev.correlated.C28n;
import net.minecraft.util.ResourceLocation;

public class GuiImporterChest extends GuiTerminal {
	private static final ResourceLocation BACKGROUND = new ResourceLocation("correlated", "textures/gui/container/importer_chest.png");
	
	public GuiImporterChest(ContainerImporterChest container) {
		super(container);
		xSize = 195;
		ySize = 222;
	}
	
	@Override
	protected boolean hasSearchAndSort() {
		return false;
	}
	
	@Override
	protected boolean hasStatusLine() {
		return false;
	}
	
	@Override
	protected int getScrollTrackX() {
		return 175;
	}
	
	@Override
	protected ResourceLocation getBackground() {
		return BACKGROUND;
	}
	
	@Override
	protected String getTitle() {
		return C28n.format("gui.correlated.importer_chest");
	}

}
