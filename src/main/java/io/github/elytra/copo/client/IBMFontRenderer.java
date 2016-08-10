package io.github.elytra.copo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class IBMFontRenderer {
	private static final ResourceLocation BIOS = new ResourceLocation("correlatedpotentialistics", "textures/gui/bios.png");
	private static final Gui GUI = new Gui() {};
	private static final String IBM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz.,()_-:\\>/?[]";
	
	public static void drawString(int x, int y, String str) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(BIOS);
		x*=2;
		y*=2;
		GlStateManager.pushMatrix();
		GlStateManager.color(0.8f, 0.8f, 0.8f);
		GlStateManager.scale(0.5, 0.5, 1);
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int pos = IBM.indexOf(c);
			if (pos == -1) continue;
			int u = (pos%8)*9;
			int v = (pos/8)*13;
			GUI.drawTexturedModalRect(x+(i*9), y, u, v, 9, 13);
		}
		GlStateManager.popMatrix();
	}
}
