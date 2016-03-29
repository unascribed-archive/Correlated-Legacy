package com.unascribed.correlatedpotentialistics.client.gui;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.helper.Numbers;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT.CraftingAmount;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT.CraftingTarget;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT.SlotVirtual;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT.SortMode;
import com.unascribed.correlatedpotentialistics.network.SetSearchQueryMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiVT extends GuiContainer {
	private static final ResourceLocation background = new ResourceLocation("correlatedpotentialistics", "textures/gui/container/vt.png");
	
	private ContainerVT container;
	private GuiTextField searchField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 0, 0, 85, 8);
	private String lastSearchQuery = "";
	private GuiButtonExt sortDirection;
	private GuiButtonExt sortMode;
	private GuiButtonExt craftingTarget;
	private GuiButtonExt craftingAmount;
	private GuiButtonExt clearGrid;
	
	public GuiVT(ContainerVT container) {
		super(container);
		searchField.setEnableBackgroundDrawing(false);
		searchField.setTextColor(-1);
		this.container = container;
		xSize = 256;
		ySize = 222;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(0, 0, 0, 0, 256, 222);
		GlStateManager.popMatrix();
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(I18n.format("gui.correlatedpotentialistics.vt"), 8, 6, 0x404040);
		fontRendererObj.drawString(I18n.format("container.inventory"), 69, 128, 0x404040);
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
		GlStateManager.popMatrix();
		GlStateManager.enableDepth();
		
		int u = 232;
		if (container.rows <= 6) {
			u += 12;
		}
		int y = 18;
		GlStateManager.color(1, 1, 1);
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(236, y+(scrollKnobY-6), u, 241, 12, 15);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(-(width - xSize) / 2, -(height - ySize) / 2, 0);
		drawTexturedModalRect(clearGrid.xPosition+2, clearGrid.yPosition+2, 0, 222, 2, 10);
		drawTexturedModalRect(sortDirection.xPosition+2, sortDirection.yPosition+2, container.sortAscending ? 0 : 8, 248, 8, 8);
		drawTexturedModalRect(sortMode.xPosition+2, sortMode.yPosition+2, 16+(container.sortMode.ordinal()*8), 248, 8, 8);
		drawTexturedModalRect(craftingTarget.xPosition+2, craftingTarget.yPosition+2, container.craftingTarget.ordinal()*8, 232, 8, 8);
		drawTexturedModalRect(craftingAmount.xPosition+2, craftingAmount.yPosition+2, container.craftingAmount.ordinal()*8, 240, 8, 8);
		searchField.drawTextBox();
		if (sortMode.isMouseOver()) {
			drawHoveringText(Lists.newArrayList(
					I18n.format("tooltip.correlatedpotentialistics.sortmode"),
					"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.sortmode."+container.sortMode.lowerName)
				), mouseX, mouseY);
		}
		if (sortDirection.isMouseOver()) {
			String str = (container.sortAscending ? "ascending" : "descending");
			drawHoveringText(Lists.newArrayList(
					I18n.format("tooltip.correlatedpotentialistics.sortdirection"),
					"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.sortdirection."+str)
				), mouseX, mouseY);
		}
		if (craftingAmount.isMouseOver()) {
			drawHoveringText(Lists.newArrayList(
					I18n.format("tooltip.correlatedpotentialistics.crafting_amount"),
					"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.crafting.only_shift_click"),
					"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.crafting_amount."+container.craftingAmount.lowerName)
				), mouseX, mouseY);
		}
		if (craftingTarget.isMouseOver()) {
			drawHoveringText(Lists.newArrayList(
					I18n.format("tooltip.correlatedpotentialistics.crafting_target"),
					"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.crafting.only_shift_click"),
					"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.crafting_target."+container.craftingTarget.lowerName)
				), mouseX, mouseY);
		}
		if (clearGrid.isMouseOver()) {
			drawHoveringText(Lists.newArrayList(I18n.format("tooltip.correlatedpotentialistics.clear_crafting_grid")), mouseX, mouseY);
		}
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
		searchField.xPosition = x+143;
		searchField.yPosition = y+6;
		buttonList.add(sortDirection = new GuiButtonExt(0, x+236, y+4, 12, 12, ""));
		buttonList.add(sortMode = new GuiButtonExt(1, x+128, y+4, 12, 12, ""));
		buttonList.add(craftingAmount = new GuiButtonExt(2, x+51, y+99, 12, 12, ""));
		buttonList.add(craftingTarget = new GuiButtonExt(3, x+51, y+113, 12, 12, ""));
		buttonList.add(clearGrid = new GuiButtonExt(4, x+61, y+37, 6, 14, ""));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			mc.playerController.sendEnchantPacket(container.windowId, container.sortAscending ? -2 : -1);
			container.sortAscending = !container.sortAscending;
		} else if (button.id == 1) {
			switch (container.sortMode) {
				case QUANTITY:
					container.sortMode = SortMode.MOD_MINECRAFT_FIRST;
					mc.playerController.sendEnchantPacket(container.windowId, -4);
					break;
				case MOD_MINECRAFT_FIRST:
					container.sortMode = SortMode.MOD;
					mc.playerController.sendEnchantPacket(container.windowId, -5);
					break;
				case MOD:
					container.sortMode = SortMode.NAME;
					mc.playerController.sendEnchantPacket(container.windowId, -6);
					break;
				case NAME:
					container.sortMode = SortMode.QUANTITY;
					mc.playerController.sendEnchantPacket(container.windowId, -3);
					break;
			}
		} else if (button.id == 2) {
			switch (container.craftingAmount) {
				case ONE:
					container.craftingAmount = CraftingAmount.STACK;
					mc.playerController.sendEnchantPacket(container.windowId, -11);
					break;
				case STACK:
					container.craftingAmount = CraftingAmount.MAX;
					mc.playerController.sendEnchantPacket(container.windowId, -12);
					break;
				case MAX:
					container.craftingAmount = CraftingAmount.ONE;
					mc.playerController.sendEnchantPacket(container.windowId, -10);
					break;
			}
		} else if (button.id == 3) {
			switch (container.craftingTarget) {
				case INVENTORY:
					container.craftingTarget = CraftingTarget.NETWORK;
					mc.playerController.sendEnchantPacket(container.windowId, -21);
					break;
				case NETWORK:
					container.craftingTarget = CraftingTarget.INVENTORY;
					mc.playerController.sendEnchantPacket(container.windowId, -20);
					break;
				
			}
		} else if (button.id == 4) {
			mc.playerController.sendEnchantPacket(container.windowId, -128);
		}
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		if (container.rows > 6) {
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
		x += 236;
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
		if (draggingScrollKnob && container.rows > 6) {
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

	public void updateSearchQuery(String query) {
		lastSearchQuery = query;
		searchField.setText(query);
	}
	
}
