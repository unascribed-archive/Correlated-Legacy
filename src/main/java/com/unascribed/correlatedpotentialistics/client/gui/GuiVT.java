package com.unascribed.correlatedpotentialistics.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.base.Objects;
import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.helper.Numbers;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT.SlotVirtual;
import com.unascribed.correlatedpotentialistics.network.SetSearchQueryMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public class GuiVT extends GuiContainer {
	private static final ResourceLocation generic_54 = new ResourceLocation("textures/gui/container/generic_54.png");
	private static final ResourceLocation tab_item_search = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
	private static final ResourceLocation tabs = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	
	private ContainerVT container;
	private GuiTextField searchField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 0, 0, 85, 8);
	private String lastSearchQuery = "";
	
	public GuiVT(ContainerVT container) {
		super(container);
		searchField.setEnableBackgroundDrawing(false);
		searchField.setTextColor(-1);
		this.container = container;
		xSize = 195;
		ySize = 222;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		// IT'S ALIVE!!!
		GlStateManager.pushMatrix();
		GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
		mc.getTextureManager().bindTexture(generic_54);
		drawTexturedModalRect(0, 17, 0, 17, 195, 222);
		mc.getTextureManager().bindTexture(tab_item_search);
		drawTexturedModalRect(0, 0, 0, 0, 195, 17);
		drawTexturedModalRect(173, 17, 173, 17, 22, 118);
		drawTexturedModalRect(175, 135, 175, 135, 20, 120);
		GlStateManager.popMatrix();
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(I18n.format("gui.correlatedpotentialistics.vt"), 8, 6, 0x404040);
		fontRendererObj.drawString(I18n.format("gui.inventory"), 8, 128, 0x404040);
		GlStateManager.pushMatrix();
		GlStateManager.disableDepth();
		GlStateManager.scale(0.5f, 0.5f, 1);
		for (Slot slot : inventorySlots.inventorySlots) {
			if (slot instanceof SlotVirtual) {
				SlotVirtual sv = ((SlotVirtual)slot);
				if (sv.getCount() > 0) {
					String str = Numbers.humanReadableItemCount(sv.getCount());
					int x = sv.xDisplayPosition*2;
					int y = sv.yDisplayPosition*2;
					x += (32-mc.fontRendererObj.getStringWidth(str));
					y += (32-mc.fontRendererObj.FONT_HEIGHT);
					mc.fontRendererObj.drawStringWithShadow(str, x, y, -1);
				}
			}
		}
		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
		
		mc.getTextureManager().bindTexture(tabs);
		int u = 232;
		if (container.rows <= 6) {
			u += 12;
		}
		int y = 18;
		GlStateManager.color(1, 1, 1);
		drawTexturedModalRect(175, y+(scrollKnobY-6), u, 0, 12, 15);
		GlStateManager.pushMatrix();
		GlStateManager.translate(-(width - xSize) / 2, -(height - ySize) / 2, 0);
		searchField.drawTextBox();
		GlStateManager.popMatrix();
	}
	
	private boolean draggingScrollKnob = false;
	private int scrollKnobY = 6;
	private int ticksSinceLastQueryChange = 0;
	
	@Override
	public void initGui() {
		super.initGui();
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		searchField.xPosition = x+82;
		searchField.yPosition = y+6;
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		if (container.rows > 0) {
			int dWheel = Mouse.getDWheel()/container.rows;
			if (dWheel != 0) {
				scrollKnobY = Math.max(Math.min(101, scrollKnobY-dWheel), 6);
				mc.playerController.sendEnchantPacket(container.windowId, Math.round(((scrollKnobY-6)/101f)*(container.rows-6)));
			}
		} else {
			scrollKnobY = 6;
		}
		searchField.updateCursorCounter();
		if (!Objects.equal(searchField.getText(), lastSearchQuery)) {
			lastSearchQuery = searchField.getText();
			ticksSinceLastQueryChange = 0;
			if (scrollKnobY != 6) {
				scrollKnobY = 6;
				mc.playerController.sendEnchantPacket(container.windowId, 0);
			}
		}
		ticksSinceLastQueryChange++;
		if (ticksSinceLastQueryChange == 4) {
			CoPo.inst.network.sendToServer(new SetSearchQueryMessage(container.windowId, lastSearchQuery));
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (searchField.isFocused()) {
			if (keyCode == Keyboard.KEY_ESCAPE) {
				mc.thePlayer.closeScreen();
			} else {
				searchField.textboxKeyTyped(typedChar, keyCode);
			}
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		int width = 12;
		int height = 110;
		x += 175;
		y += 18;
		if (mouseButton == 0
				&& mouseX >= x && mouseX <= x+width
				&& mouseY >= y && mouseY <= y+height) {
			draggingScrollKnob = true;
			mouseClickMove(mouseX, mouseY, mouseButton, 0);
			return;
		}
		if (mouseButton == 1
				&& mouseX >= searchField.xPosition && mouseX <= searchField.xPosition+searchField.width
				&& mouseY >= searchField.yPosition && mouseY <= searchField.yPosition+searchField.height) {
			searchField.setText("");
		}
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
		if (draggingScrollKnob && container.rows > 0) {
			int y = (height - ySize) / 2;
			scrollKnobY = Math.max(Math.min(101, (mouseY-24)-y), 6);
			mc.playerController.sendEnchantPacket(container.windowId, Math.round(((scrollKnobY-6)/101f)*(container.rows-6)));
		}
		super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			draggingScrollKnob = false;
		}
		super.mouseReleased(mouseX, mouseY, mouseButton);
	}
	
}
