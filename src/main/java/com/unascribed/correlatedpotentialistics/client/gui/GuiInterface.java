package com.unascribed.correlatedpotentialistics.client.gui;

import com.unascribed.correlatedpotentialistics.inventory.ContainerInterface;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiInterface extends GuiContainer {
	private static final ResourceLocation background = new ResourceLocation("correlatedpotentialistics", "textures/gui/container/interface.png");
	
	// this may be used in the future, if this GUI gets more complicated
	@SuppressWarnings("unused")
	private ContainerInterface container;
	public GuiInterface(ContainerInterface container) {
		super(container);
		this.container = container;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(background);
		drawTexturedModalRect((width - xSize)/2, (height - ySize)/2, 0, 0, xSize, ySize);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(I18n.format("gui.inventory"), 8, 73, 0x404040);
	}

}
