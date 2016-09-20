package io.github.elytra.copo.client.gui;

import io.github.elytra.copo.inventory.ContainerImporterChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiImporterChest extends GuiTerminal {
	private static final ResourceLocation BACKGROUND = new ResourceLocation("correlatedpotentialistics", "textures/gui/container/importer_chest.png");
	
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
		return I18n.format("gui.correlatedpotentialistics.importer_chest");
	}

}
