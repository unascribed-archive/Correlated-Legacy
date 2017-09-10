package com.elytradev.correlated.client.gui;

import com.elytradev.correlated.inventory.ContainerInterface;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import com.elytradev.correlated.C28n;
import net.minecraft.util.ResourceLocation;

public class GuiInterface extends GuiContainer {
	private static final ResourceLocation background = new ResourceLocation("correlated", "textures/gui/container/interface.png");

	// this may be used in the future, if this GUI gets more complicated
	@SuppressWarnings("unused")
	private ContainerInterface container;
	public GuiInterface(ContainerInterface container) {
		super(container);
		this.container = container;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(background);
		drawTexturedModalRect((width - xSize)/2, (height - ySize)/2, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(C28n.format("gui.inventory"), 8, 73, 0x404040);
	}

}
