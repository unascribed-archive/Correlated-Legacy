package com.elytradev.correlated.client.gui;

import java.io.IOException;
import java.util.Locale;

import com.elytradev.correlated.entity.EntityAutomaton.AutomatonStatus;
import com.elytradev.correlated.inventory.ContainerAutomaton;
import com.elytradev.correlated.network.automaton.SetAutomatonNameMessage;
import com.elytradev.correlated.proxy.ClientProxy;
import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import com.elytradev.correlated.C28n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;

public class GuiAutomaton extends GuiTerminal implements GuiResponder {
	private static final ResourceLocation background = new ResourceLocation("correlated", "textures/gui/container/automaton.png");
	private ContainerAutomaton container;
	private GuiTextField name;
	
	public GuiAutomaton(ContainerAutomaton inventorySlotsIn) {
		super(inventorySlotsIn);
		this.container = inventorySlotsIn;
		xSize = 196;
		ySize = 222;
	}

	@Override
	public void initGui() {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		
		int btnX = x+(190-AutomatonStatus.VALUES.length*12);
		for (AutomatonStatus s : AutomatonStatus.VALUES) {
			GuiButtonExt btn = new GuiButtonExt((-s.ordinal())-30, btnX, y+34, 10, 10, "");
			if (s == AutomatonStatus.EXEC) {
				btn.enabled = false;
			}
			buttonList.add(btn);
			btnX += 12;
		}
		GuiButtonExt mute = new GuiButtonExt(-60, x+83, y+34, 10, 10, "");
		if (container.automaton.hasModule("speech")) {
			mute.enabled = false;
		}
		buttonList.add(mute);
		buttonList.add(new GuiButtonExt(-59, x+95, y+34, 10, 10, ""));
		name = new GuiTextField(-55, fontRenderer, x+83, y+5, 106, 10);
		name.setText(container.automaton.getName());
		name.setGuiResponder(this);
		super.initGui();
	}
	
	@Override
	protected int getXOffset() {
		return -60;
	}
	
	@Override
	protected int getYOffset() {
		return 43;
	}
	
	@Override
	protected int getScrollTrackX() {
		return 176;
	}
	
	@Override
	protected int getScrollTrackY() {
		return 44;
	}
	
	@Override
	protected int getScrollTrackHeight() {
		return 70;
	}
	
	@Override
	protected boolean hasStatusLine() {
		return false;
	}
	
	@Override
	protected int getEnergyUnitX() {
		return 8;
	}
	
	@Override
	protected int getEnergyUnitY() {
		return 115;
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		name.updateCursorCounter();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id <= -30 && button.id >= -60) {
			mc.playerController.sendEnchantPacket(container.windowId, button.id);
		} else {
			super.actionPerformed(button);
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		GlStateManager.translate(x, y, 0);
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(0, 0, 0, 0, 196, 222);
		GlStateManager.popMatrix();
		int ofsX = 63;
		int ofsY = 35;
		GuiInventory.drawEntityOnScreen(x + ofsX, y + ofsY, 30, (float)(x + ofsX) - mouseX, (float)(y + 5) - mouseY, container.automaton);
		buttonList.get(AutomatonStatus.VALUES.length).enabled = !container.automaton.hasModule("speech");
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(background);
		int btnX = (193-AutomatonStatus.VALUES.length*12);
		for (AutomatonStatus s : AutomatonStatus.VALUES) {
			GlStateManager.color(1, 1, 1);
			drawTexturedModalRect(btnX, 37, 244, s.ordinal()*4, 4, 4);
			if (container.automaton.getStatus() == s) {
				drawTexturedModalRect(btnX-3, 34, 246, 246, 10, 10);
			}
			btnX += 12;
		}
		drawTexturedModalRect(86, 37, 248, container.automaton.isMuted() ? 4 : 0, 4, 4);
		drawTexturedModalRect(98, 37, 252, container.automaton.getFollowDistance()*4, 4, 4);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		GlStateManager.pushMatrix();
		GlStateManager.translate(-x, -y, 0);
		name.drawTextBox();
		GlStateManager.popMatrix();
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		GlStateManager.disableLighting();
		GlStateManager.color(1, 1, 1);
		mc.getTextureManager().bindTexture(background);
		int w = (int)(104*(container.automaton.getHealth()/container.automaton.getMaxHealth()));
		drawTexturedModalRect(84, 21, 0, 222, w, 9);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.ZERO, DestFactor.SRC_COLOR);
		int t = (int) (ClientProxy.ticks);
		drawTexturedModalRect(84, 21, t, 231, w, 9);
		GlStateManager.disableBlend();
		for (AutomatonStatus s : AutomatonStatus.VALUES) {
			if (buttonList.get(s.ordinal()).isMouseOver()) {
				GuiUtils.drawHoveringText(
						Lists.newArrayList(C28n.format("tooltip.correlated.automaton.state."+s.name().toLowerCase(Locale.ROOT))),
						mouseX-((width-xSize)/2), mouseY-((height-ySize)/2), width, height, 80, fontRenderer);
			}
		}
		if (buttonList.get(AutomatonStatus.VALUES.length+1).isMouseOver()) {
			GuiUtils.drawHoveringText(
					Lists.newArrayList(
							C28n.format("tooltip.correlated.automaton.followDistance"),
							"\u00A77"+C28n.format("tooltip.correlated.automaton.followDistance."+container.automaton.getFollowDistance())
						),
					mouseX-((width-xSize)/2), mouseY-((height-ySize)/2), width, height, 80, fontRenderer);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		name.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		name.textboxKeyTyped(typedChar, keyCode);
		if (!name.isFocused()) {
			super.keyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	protected String getTitle() {
		return "";
	}

	@Override
	public void setEntryValue(int id, boolean value) {
	}

	@Override
	public void setEntryValue(int id, float value) {
	}

	@Override
	public void setEntryValue(int id, String value) {
		if (id == -55) {
			new SetAutomatonNameMessage(container.windowId, value).sendToServer();
		}
	}


}
