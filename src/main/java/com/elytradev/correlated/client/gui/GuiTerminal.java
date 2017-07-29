package com.elytradev.correlated.client.gui;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.EnergyUnit;
import com.elytradev.correlated.client.IBMFontRenderer;
import com.elytradev.correlated.client.gui.shell.GuiTerminalShell;
import com.elytradev.correlated.helper.Numbers;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.inventory.ContainerTerminal;
import com.elytradev.correlated.inventory.ContainerTerminal.CraftingAmount;
import com.elytradev.correlated.inventory.ContainerTerminal.CraftingTarget;
import com.elytradev.correlated.inventory.ContainerTerminal.SlotVirtual;
import com.elytradev.correlated.inventory.ContainerTerminal.SortMode;
import com.elytradev.correlated.network.inventory.InsertAllMessage;
import com.elytradev.correlated.network.inventory.SetSearchQueryServerMessage;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiTerminal extends GuiContainer {
	private static final ResourceLocation background = new ResourceLocation("correlated", "textures/gui/container/terminal.png");
	private static final ResourceLocation ENERGY = new ResourceLocation("correlated", "textures/misc/energy.png");

	private ContainerTerminal container;
	private GuiTextField searchField = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 85, 8);
	private String lastSearchQuery = "";
	private GuiButtonExt sortDirection;
	private GuiButtonExt sortMode;
	private GuiButtonExt craftingTarget;
	private GuiButtonExt craftingAmount;
	private GuiButtonExt clearGrid;
	private GuiButtonExt focusByDefault;
	private GuiButtonExt jeiSync;
	private GuiButtonExt dump;
	private GuiButtonExt partition;
	private GuiButtonExt preferredEnergySystem;
	
	private String lastJeiQuery;
	
	public int signalStrength = -1;
	
	public GuiTerminal(ContainerTerminal container) {
		super(container);
		searchField.setEnableBackgroundDrawing(false);
		searchField.setTextColor(-1);
		searchField.setFocused(container.searchFocusedByDefault);
		this.container = container;
		xSize = 256;
		ySize = 222;
		lastJeiQuery = Correlated.inst.jeiQueryReader.get();
	}

	protected boolean hasStatusLine() {
		return true;
	}
	
	protected boolean hasSearchAndSort() {
		return true;
	}
	
	protected ResourceLocation getBackground() {
		return background;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		if (container.status.isEmpty()) {
			if (Math.random() == 0.5) {
				container.status.add(new TextComponentTranslation("correlated.shell.readyEgg"));
			} else {
				container.status.add(signalStrength != -1 ? new TextComponentTranslation("correlated.shell.ready_wireless") : new TextComponentTranslation("correlated.shell.ready"));
			}
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
		mc.getTextureManager().bindTexture(getBackground());
		drawTexturedModalRect(0, 0, 0, 0, 256, 136);
		drawTexturedModalRect(61, 136, 61, 136, 176, 87);
		drawTexturedModalRect(237, 135, 237, 135, 19, 11);
		if (container.terminal.hasMaintenanceSlot()) {
			drawTexturedModalRect(8, 152, 8, 152, 44, 34);
			dump.enabled = !container.terminal.getMaintenanceSlotContent().isEmpty();
			partition.enabled = !container.terminal.getMaintenanceSlotContent().isEmpty();
		}
		GlStateManager.popMatrix();
	}
	
	protected String getTitle() {
		return I18n.format("gui.correlated.terminal");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(getTitle(), 8, 6, 0x404040);
		if (hasStatusLine()) {
			String lastLine = signalStrength == 0 ? I18n.format("gui.correlated.noSignal") : container.status.get(container.status.size()-1).getFormattedText().trim();
			int maxLength = signalStrength == -1 ? 160 : 144;
			int len = (int)IBMFontRenderer.measure(lastLine);
			if (len > maxLength) {
				String s = lastLine;
				for (int i = 0; i < lastLine.length(); i++) {
					String str = lastLine.substring(0, i)+"...";
					int slen = (int)IBMFontRenderer.measure(str);
					if (slen > maxLength) break;
					s = str;
				}
				lastLine = s;
			}
			int left = 68+container.playerInventoryOffsetX;
			int top = 90+container.playerInventoryOffsetY;
			int right = 162+68+container.playerInventoryOffsetX;
			int bottom = 11+89+container.playerInventoryOffsetY;
			
			drawRect(left, top, right, bottom, 0xFF006D4B);
			IBMFontRenderer.drawString(left+2, top+1, lastLine, 0x00DBAD);
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.disableDepth();
		GlStateManager.scale(0.5f, 0.5f, 1);
		for (Slot slot : inventorySlots.inventorySlots) {
			if (slot instanceof SlotVirtual) {
				SlotVirtual sv = ((SlotVirtual)slot);
				if (sv.getCount() > 0) {
					String str = Numbers.humanReadableItemCount(sv.getCount());
					int x = sv.xPos*2;
					int y = sv.yPos*2;
					x += (32-mc.fontRenderer.getStringWidth(str));
					y += (32-mc.fontRenderer.FONT_HEIGHT);
					mc.fontRenderer.drawStringWithShadow(str, x, y, -1);
				}
			}
		}
		GlStateManager.popMatrix();

		int u = 232;
		if (container.rows <= container.slotsTall) {
			u += 12;
		}
		int y = 18;
		mc.getTextureManager().bindTexture(getBackground());
		if (hasStatusLine() && signalStrength != -1) {
			int color = Correlated.proxy.getColor("other", 64);
			float r = ((color >> 16)&0xFF)/255f;
			float g = ((color >> 8)&0xFF)/255f;
			float b = (color&0xFF)/255f;
			
			GlStateManager.color(r, g, b);
			int right = 162+68+container.playerInventoryOffsetX;
			int top = 90+container.playerInventoryOffsetY;
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			drawTexturedModalRect(right-18, top+1, 0, 224, 16, 8);
			drawTexturedModalRect(right-18, top+1, 0, 232, 5+(signalStrength*2), 8);
			GlStateManager.disableBlend();
		}
		GlStateManager.enableDepth();
		GlStateManager.color(1, 1, 1);
		drawTexturedModalRect(getScrollTrackX(), getScrollTrackY()+y+(scrollKnobY-6), u, 241, 12, 15);

		GlStateManager.pushMatrix();
		GlStateManager.translate(-(width - xSize) / 2, -(height - ySize) / 2, 0);
		if (container.hasCraftingMatrix) {
			drawTexturedModalRect(clearGrid.xPosition+2, clearGrid.yPosition+2, 0, 190, 2, 10);
			drawTexturedModalRect(craftingTarget.xPosition+2, craftingTarget.yPosition+2, container.craftingTarget.ordinal()*8, 200, 8, 8);
			drawTexturedModalRect(craftingAmount.xPosition+2, craftingAmount.yPosition+2, container.craftingAmount.ordinal()*8, 208, 8, 8);			
		}
		
		if (container.terminal.hasMaintenanceSlot()) {
			if (!dump.enabled) {
				GlStateManager.color(0.5f, 0.5f, 0.5f);
			} else if (container.isDumping || container.isFilling) {
				GlStateManager.color(1, 1, 0.25f);
			}
			drawTexturedModalRect(dump.xPosition+2, dump.yPosition+2, 16, (container.isFilling || isShiftKeyDown()) ? 232 : 240, 8, 8);
			if (!partition.enabled) {
				GlStateManager.color(0.5f, 0.5f, 0.5f);
			} else {
				GlStateManager.color(1, 1, 1);
			}
			drawTexturedModalRect(partition.xPosition+2, partition.yPosition+2, 16, 248, 8, 8);
			GlStateManager.color(1, 1, 1);
		}
		
		if (hasSearchAndSort()) {
			drawTexturedModalRect(sortDirection.xPosition+2, sortDirection.yPosition+2, container.sortAscending ? 0 : 8, 216, 8, 8);
			drawTexturedModalRect(sortMode.xPosition+2, sortMode.yPosition+2, 16+(container.sortMode.ordinal()*8), 216, 8, 8);
			drawTexturedModalRect(focusByDefault.xPosition+2, focusByDefault.yPosition+2, container.searchFocusedByDefault ? 8 : 0, 240, 8, 8);
			if (jeiSync != null) {
				drawTexturedModalRect(jeiSync.xPosition+2, jeiSync.yPosition+2, container.jeiSyncEnabled ? 8 : 0, 248, 8, 8);
			}
			searchField.drawTextBox();
			if (sortMode.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						I18n.format("tooltip.correlated.sortmode"),
						"\u00A77"+I18n.format("tooltip.correlated.sortmode."+container.sortMode.lowerName)
					), mouseX, mouseY);
			}
			if (sortDirection.isMouseOver()) {
				String str = (container.sortAscending ? "ascending" : "descending");
				drawHoveringText(Lists.newArrayList(
						I18n.format("tooltip.correlated.sortdirection"),
						"\u00A77"+I18n.format("tooltip.correlated.sortdirection."+str)
					), mouseX, mouseY);
			}
			if (focusByDefault.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						I18n.format("tooltip.correlated.focus_search_by_default."+container.searchFocusedByDefault)
					), mouseX, mouseY);
			}
			if (preferredEnergySystem.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						I18n.format("tooltip.correlated.preferred_energy"),
						"\u00A77"+CConfig.preferredUnit.displayName
					), mouseX, mouseY);
			}
			if (jeiSync != null && jeiSync.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						I18n.format("tooltip.correlated.jei_sync."+container.jeiSyncEnabled)
					), mouseX, mouseY);
			}
			if (container.hasCraftingMatrix) {
				if (craftingAmount.isMouseOver()) {
					drawHoveringText(Lists.newArrayList(
							I18n.format("tooltip.correlated.crafting_amount"),
							"\u00A77"+I18n.format("tooltip.correlated.crafting.only_shift_click"),
							"\u00A77"+I18n.format("tooltip.correlated.crafting_amount."+container.craftingAmount.lowerName)
						), mouseX, mouseY);
				}
				if (craftingTarget.isMouseOver()) {
					drawHoveringText(Lists.newArrayList(
							I18n.format("tooltip.correlated.crafting_target"),
							"\u00A77"+I18n.format("tooltip.correlated.crafting.only_shift_click"),
							"\u00A77"+I18n.format("tooltip.correlated.crafting_target."+container.craftingTarget.lowerName)
						), mouseX, mouseY);
				}
				if (clearGrid.isMouseOver()) {
					drawHoveringText(Lists.newArrayList(I18n.format("tooltip.correlated.clear_crafting_grid")), mouseX, mouseY);
				}
			}
		}
		if (container.terminal.hasMaintenanceSlot()) {
			if (dump.enabled && dump.isMouseOver()) {
				if (isShiftKeyDown() || container.isFilling) {
					drawHoveringText(Lists.newArrayList(
							I18n.format("tooltip.correlated.fill"),
							"\u00A77"+I18n.format("tooltip.correlated.release_shift_to_dump")
						), mouseX, mouseY);
				} else {
					drawHoveringText(Lists.newArrayList(
							I18n.format("tooltip.correlated.dump"),
							"\u00A77"+I18n.format("tooltip.correlated.shift_to_fill")
						), mouseX, mouseY);
				}
			}
			if (partition.enabled && partition.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						I18n.format("tooltip.correlated.partition")
					), mouseX, mouseY);
			}
		}
		GlStateManager.disableLighting();
		mc.renderEngine.bindTexture(ENERGY);
		GlStateManager.color(1, 1, 1);
		drawModalRectWithCustomSizedTexture(preferredEnergySystem.xPosition+2, preferredEnergySystem.yPosition+2, 0, CConfig.preferredUnit.ordinal()*8, 8, 8, 8, 80);
		GlStateManager.popMatrix();

	}

	private boolean draggingScrollKnob = false;
	private int scrollKnobY = 6;
	private int ticksSinceLastQueryChange = 0;

	@Override
	public void initGui() {
		super.initGui();
		int x = (width - xSize) / 2;
		x += getXOffset();
		int y = (height - ySize) / 2;
		y += getYOffset();
		if (hasSearchAndSort()) {
			searchField.xPosition = x+143;
			searchField.yPosition = y+6;
			buttonList.add(sortDirection = new GuiButtonExt(0, x+236, y+4, 12, 12, ""));
			buttonList.add(sortMode = new GuiButtonExt(1, x+128, y+4, 12, 12, ""));
			buttonList.add(focusByDefault = new GuiButtonExt(5, x+114, y+4, 12, 12, ""));
			buttonList.add(preferredEnergySystem = new GuiButtonExt(9, x+100, y+4, 12, 12, ""));
			if (Correlated.inst.jeiAvailable) {
				buttonList.add(jeiSync = new GuiButtonExt(6, x-getXOffset()+getJeiSyncX(), y-getYOffset()+getJeiSyncY(), 12, 12, ""));
			}
		}
		if (container.hasCraftingMatrix) {
			buttonList.add(craftingAmount = new GuiButtonExt(2, x+51, y+99, 12, 12, ""));
			buttonList.add(craftingTarget = new GuiButtonExt(3, x+51, y+113, 12, 12, ""));
			buttonList.add(clearGrid = new GuiButtonExt(4, x+61, y+37, 6, 14, ""));
		}
		if (container.terminal.hasMaintenanceSlot()) {
			buttonList.add(dump = new GuiButtonExt(7, x+35, y+156, 12, 12, ""));
			buttonList.add(partition = new GuiButtonExt(8, x+35, y+170, 12, 12, ""));
		}
	}
	
	protected int getYOffset() {
		return 0;
	}
	
	protected int getXOffset() {
		return 0;
	}
	
	protected int getJeiSyncX() {
		return 236;
	}
	
	protected int getJeiSyncY() {
		return 130;
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
					container.sortMode = SortMode.LAST_MODIFIED;
					mc.playerController.sendEnchantPacket(container.windowId, -7);
					break;
				case LAST_MODIFIED:
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
		} else if (button.id == 5) {
			mc.playerController.sendEnchantPacket(container.windowId, container.searchFocusedByDefault ? -25 : -24);
			container.searchFocusedByDefault = !container.searchFocusedByDefault;
		} else if (button.id == 6) {
			mc.playerController.sendEnchantPacket(container.windowId, container.jeiSyncEnabled ? -27 : -26);
			container.jeiSyncEnabled = !container.jeiSyncEnabled;
		} else if (button.id == 7) {
			if (container.isDumping || container.isFilling) {
				mc.playerController.sendEnchantPacket(container.windowId, -29);
				container.isDumping = false;
				container.isFilling = false;
			} else {
				if (isShiftKeyDown()) {
					mc.playerController.sendEnchantPacket(container.windowId, -61);
					container.isDumping = false;
					container.isFilling = true;
				} else {
					mc.playerController.sendEnchantPacket(container.windowId, -28);
					container.isDumping = true;
					container.isFilling = false;
				}
			}
		} else if (button.id == 8) {
			mc.playerController.sendEnchantPacket(container.windowId, -62);
		} else if (button.id == 9) {
			int ordinal = CConfig.preferredUnit.ordinal();
			ordinal = (ordinal + 1) % EnergyUnit.values().length;
			EnergyUnit eu = EnergyUnit.values()[ordinal];
			if (eu == EnergyUnit.GLYPHS) {
				eu = EnergyUnit.DANKS;
			}
			CConfig.preferredUnit = eu;
			CConfig.save();
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (container.rows > container.slotsTall) {
			int dWheel = Mouse.getDWheel()/container.rows;
			if (dWheel != 0) {
				scrollKnobY = Math.max(Math.min(getScrollTrackHeight()-9, scrollKnobY-dWheel), 6);
				mc.playerController.sendEnchantPacket(container.windowId, Math.round(((scrollKnobY-6)/(float)(getScrollTrackHeight()-9))*(container.rows-container.slotsTall)));
			}
		} else {
			scrollKnobY = 6;
		}
		if (hasSearchAndSort()) {
			searchField.updateCursorCounter();
			if (container.jeiSyncEnabled) {
				String jeiQuery = Correlated.inst.jeiQueryReader.get();
				if (!Objects.equal(jeiQuery, lastJeiQuery)) {
					lastJeiQuery = jeiQuery;
					searchField.setText(jeiQuery);
				}
			}
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
				new SetSearchQueryServerMessage(container.windowId, lastSearchQuery).sendToServer();
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (searchField.isFocused()) {
			if (keyCode == Keyboard.KEY_ESCAPE) {
				mc.player.closeScreen();
			} else {
				searchField.textboxKeyTyped(typedChar, keyCode);
				if (container.jeiSyncEnabled) {
					Correlated.inst.jeiQueryUpdater.accept(searchField.getText());
				}
			}
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	protected int getScrollTrackX() {
		return 236;
	}
	
	protected int getScrollTrackY() {
		return 0;
	}
	
	protected int getScrollTrackHeight() {
		return 110;
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		
		int left = 68+container.playerInventoryOffsetX;
		int top = 90+container.playerInventoryOffsetY;
		int right = 162+68+container.playerInventoryOffsetX;
		int bottom = 11+89+container.playerInventoryOffsetY;
		if (hasStatusLine() && mouseButton == 0
				&& mouseX >= x+left && mouseX <= x+right
				&& mouseY >= y+top && mouseY <= y+bottom) {
			if (signalStrength != -1) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiSelectAPN(this, true, false, container));
			} else {
				Minecraft.getMinecraft().displayGuiScreen(new GuiTerminalShell(this, container));
			}
		}
		
		int width = 12;
		int height = getScrollTrackHeight();
		x += getScrollTrackX();
		y += getScrollTrackY();
		y += 18;
		if (mouseButton == 0
				&& mouseX >= x && mouseX <= x+width
				&& mouseY >= y && mouseY <= y+height) {
			draggingScrollKnob = true;
			mouseClickMove(mouseX, mouseY, mouseButton, 0);
			return;
		}
		if (hasSearchAndSort()) {
			if (mouseButton == 1
					&& mouseX >= searchField.xPosition && mouseX <= searchField.xPosition+searchField.width
					&& mouseY >= searchField.yPosition && mouseY <= searchField.yPosition+searchField.height) {
				searchField.setText("");
				if (container.jeiSyncEnabled) {
					Correlated.inst.jeiQueryUpdater.accept("");
				}
			} else if (mouseButton == 1 && preferredEnergySystem.isMouseOver()) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
				int ordinal = CConfig.preferredUnit.ordinal();
				ordinal = (ordinal - 1) % EnergyUnit.values().length;
				if (ordinal < 0) {
					ordinal += EnergyUnit.values().length;
				}
				EnergyUnit eu = EnergyUnit.values()[ordinal];
				if (eu == EnergyUnit.GLYPHS) {
					eu = EnergyUnit.JOULES;
				}
				CConfig.preferredUnit = eu;
				CConfig.save();
			}
			searchField.mouseClicked(mouseX, mouseY, mouseButton);
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
		if (draggingScrollKnob && container.rows > container.slotsTall) {
			int y = (height - ySize) / 2;
			y += getScrollTrackY();
			scrollKnobY = Math.max(Math.min(getScrollTrackHeight()-9, (mouseY-24)-y), 6);
			int s = Math.round(((scrollKnobY+12)/(float)(getScrollTrackHeight()-9))*(container.rows-container.slotsTall));
			mc.playerController.sendEnchantPacket(container.windowId, s);
		}
		super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			draggingScrollKnob = false;
		}
		Slot slot = getSlotAtPosition(mouseX, mouseY);
		if (doubleClick && slot != null && mouseButton == 0 && inventorySlots.canMergeSlot(ItemStack.EMPTY, slot)
				&& isShiftKeyDown()
				&& slot != null && slot.inventory != null && shiftClickedSlot != null) {
			if (!(slot instanceof SlotVirtual)) {
				new InsertAllMessage(inventorySlots.windowId, shiftClickedSlot).sendToServer();
			}
			doubleClick = false;
		} else {
			super.mouseReleased(mouseX, mouseY, mouseButton);
		}
	}

	public void updateSearchQuery(String query) {
		lastSearchQuery = query;
		if (hasSearchAndSort()) {
			searchField.setText(query);
			if (container.jeiSyncEnabled) {
				Correlated.inst.jeiQueryUpdater.accept(query);
			}
		}
	}

	public void addLine(ITextComponent line) {
		container.status.add(line);
	}

	public void focusSearch() {
		if (searchField != null) {
			searchField.setFocused(true);
		}
	}

}
