package com.elytradev.correlated.client.gui;

import java.io.IOException;
import java.util.List;

import com.elytradev.correlated.helper.Numbers;
import com.elytradev.correlated.init.CItems;
import com.elytradev.correlated.inventory.ContainerDrive;
import com.elytradev.correlated.item.ItemDrive.PartitioningMode;
import com.elytradev.correlated.item.ItemDrive.Priority;
import com.google.common.collect.Lists;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import com.elytradev.correlated.C28n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiDrive extends GuiContainer {
	private static final ResourceLocation background = new ResourceLocation("correlated", "textures/gui/container/drive_editor.png");
	private ContainerDrive container;

	private GuiButton priority;
	private GuiButton partition;
	
	private GuiTerminal gt;
	
	public GuiDrive(GuiTerminal gt, ContainerDrive container) {
		super(container);
		this.gt = gt;
		this.container = container;
		xSize = 212;
		ySize = 222;
		priority = new GuiButtonExt(0, 0, 0, 18, 18, "");
		partition = new GuiButtonExt(1, 0, 20, 18, 18, "");
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		priority.x = x+7;
		priority.y = y+107;

		partition.x = x+187;
		partition.y = priority.y;

		buttonList.add(priority);
		buttonList.add(partition);
		
		if (container.getDrive().getItem() == CItems.DRIVE && container.getDrive().getItemDamage() == 7) {
			partition.enabled = false;
		} else {
			partition.enabled = true;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1 || mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) {
			mc.playerController.sendEnchantPacket(container.windowId, 3);
			if (mc.player != null && gt != null) {
				mc.player.openContainer = gt.inventorySlots;
			}
			mc.displayGuiScreen(gt);
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == priority) {
			mc.playerController.sendEnchantPacket(container.windowId, 0);
		} else if (button == partition) {
			mc.playerController.sendEnchantPacket(container.windowId, 2);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (priority.isMouseOver() && mouseButton == 1) {
			priority.playPressSound(mc.getSoundHandler());
			mc.playerController.sendEnchantPacket(container.windowId, 1);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(0, 0, 0, 0, 212, 222);
		GlStateManager.popMatrix();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		FontRenderer renderer = container.getDrive().getItem().getFontRenderer(container.getDrive());
		if (renderer == null) {
			renderer = fontRenderer;
		}
		renderer.drawString(container.getDrive().getDisplayName(), 8, 6, 0x404040);
		fontRenderer.drawString(C28n.format("gui.inventory"), 8, 128, 0x404040);
		GlStateManager.pushMatrix();
		GlStateManager.disableDepth();
		GlStateManager.scale(0.5f, 0.5f, 1);
		for (int i = 1; i < 65; i++) {
			Slot slot = container.inventorySlots.get(i);
			if (slot.getHasStack()) {
				ItemStack stack = slot.getStack();
				int stored = container.getItemDrive().getAmountStored(container.getDrive(), stack);
				if (stored > 0) {
					String str = Numbers.humanReadableItemCount(stored);
					int x = slot.xPos*2;
					int y = slot.yPos*2;
					x += (32-mc.fontRenderer.getStringWidth(str));
					y += (32-mc.fontRenderer.FONT_HEIGHT);
					mc.fontRenderer.drawStringWithShadow(str, x, y, -1);
				}
			}
		}
		GlStateManager.enableDepth();
		GlStateManager.popMatrix();

		mc.getTextureManager().bindTexture(background);

		int color = container.getItemDrive().getBaseColor(container.getDrive());
		GlStateManager.color(((color >> 16)&0xFF)/255f, ((color >> 8)&0xFF)/255f, (color&0xFF)/255f);
		drawTexturedModalRect(195, 5, 222, 0, 10, 10);
		color = container.getItemDrive().getFullnessColor(container.getDrive());
		GlStateManager.color(((color >> 16)&0xFF)/255f, ((color >> 8)&0xFF)/255f, (color&0xFF)/255f);
		drawTexturedModalRect(195, 5, 212, 0, 10, 10);


		GlStateManager.color(1, 1, 1);
		GlStateManager.pushMatrix();
		GlStateManager.translate(-(width - xSize) / 2, -(height - ySize) / 2, 0);

		if (!partition.enabled) GlStateManager.color(0.6f, 0.6f, 0.6f);
		else GlStateManager.color(1, 1, 1);
		PartitioningMode part = container.getItemDrive().getPartitioningMode(container.getDrive());
		drawTexturedModalRect(partition.x+4, partition.y+2, 246, part.ordinal()*13, 10, 13);

		if (!priority.enabled) GlStateManager.color(0.6f, 0.6f, 0.6f);
		else GlStateManager.color(1, 1, 1);
		Priority pri = container.getItemDrive().getPriority(container.getDrive());
		drawTexturedModalRect(priority.x+4, priority.y+2, 246, 39+(pri.ordinal()*13), 10, 13);
		if (partition.isMouseOver()) {
			List<String> li = Lists.newArrayList(
					C28n.format("gui.correlated.partition_mode"),
					"\u00A79"+C28n.format("gui.correlated.partition_mode."+part.lowerName),
					"\u00A77"+C28n.format("gui.correlated.partition_mode."+part.lowerName+".desc")
					);
			drawHoveringText(li, mouseX, mouseY);
		}
		if (priority.isMouseOver()) {
			List<String> li = Lists.newArrayList(
					C28n.format("gui.correlated.priority"),
					pri.color+C28n.format("gui.correlated.priority."+pri.lowerName)
					);
			drawHoveringText(li, mouseX, mouseY);
		}
		GlStateManager.popMatrix();
	}

}
