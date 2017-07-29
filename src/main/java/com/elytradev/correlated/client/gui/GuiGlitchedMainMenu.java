package com.elytradev.correlated.client.gui;

import java.io.IOException;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.elytradev.correlated.init.CSoundEvents;
import com.elytradev.correlated.network.dungeon.EnterDungeonMessage;
import com.elytradev.correlated.proxy.ClientProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiGlitchedMainMenu extends GuiScreen {
	private static final ResourceLocation MINECRAFT_TITLE_TEXTURES = new ResourceLocation("textures/gui/title/minecraft.png");
	private Random rand = new Random();
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		super.drawScreen(mouseX, mouseY, partialTicks);
		int j = this.width / 2 - 274 / 2;
		int k = 30;
		GlStateManager.color(1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURES);
		int stripeSize = 4;
		for (int i = 0; i < 44/stripeSize; i++) {
			int glitch;
			if ((ClientProxy.ticks % 100) < 20) {
				glitch = rand.nextInt(10)-5;
			} else {
				glitch = rand.nextInt(3)-2;
			}
			this.drawTexturedModalRect(j + glitch, k + i*stripeSize, 0, i*stripeSize, 155, stripeSize);
			this.drawTexturedModalRect(j + 155 + glitch, k + i*stripeSize, 0, 45+i*stripeSize, 155, stripeSize);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_GRAVE) || ClientProxy.glitchTicks >= 80) {
			new EnterDungeonMessage().sendToServer();
			Minecraft.getMinecraft().displayGuiScreen(null);
			ClientProxy.glitchTicks = -1;
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (ClientProxy.glitchTicks == -1) {
			ClientProxy.seed = null;
			ClientProxy.glitchTicks = 0;
			Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(CSoundEvents.GLITCHTRAVEL, 1f));
		}
	}
	
	@Override public void handleKeyboardInput() throws IOException {}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void initGui() {
		int j = this.height / 4 + 48;

		this.buttonList.add(new GuiButton(1, this.width / 2 - 100, j, I18n.format("menu.singleplayer")));
		this.buttonList.add(new GuiButton(2, this.width / 2 - 100, j + 24 * 1, I18n.format("menu.multiplayer")));
	}
}
