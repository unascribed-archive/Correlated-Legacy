package com.elytradev.correlated.client;

import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class IBMFontRenderer {
	private static final Pattern COLOR_CODE = Pattern.compile("\u00A7.");
	private static final ResourceLocation BIOS = new ResourceLocation("correlated", "textures/gui/bios.png");
	public static final String CP437 =
			  "\0☺☻♥♦♣♠•◘○◙♂♀♪♫☼►◄↕‼¶§▬↨↑↓→←∟↔▲▼"
			+ " !\"#$%&'()*+,-./0123456789:;<=>?"
			+ "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_"
			+ "`abcdefghijklmnopqrstuvwxyz{|}~⌂"
			+ "ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜ¢£¥₧ƒ"
			+ "áíóúñÑªº¿⌐¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐"
			+ "└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀"
			+ "αβΓπΣσμτΦΘΩδ∞φε∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u00a0";
	
	// Common substitutes as seen at https://en.wikipedia.org/wiki/Code_page_437#Notes
	private static final Map<Character, Character> substitutes = ImmutableMap.<Character, Character>builder()
			.put('ß', 'β') // Sharp S = beta
			.put('Π', 'π') // Pi = pi
			.put('∏', 'π') // N-ary Product = pi
			.put('∑', 'Σ') // N-ary Summation = Sigma
			.put('µ', 'μ') // Micro = mu
			.put('Ω', 'Ω') // Ohm = Omega
			.put('ð', 'δ') // eth = delta
			.put('∂', 'δ') // Partial Derivative = delta
			.put('∅', 'φ') // Empty Set = phi
			.put('ϕ', 'φ') // Phi Symbol = phi
			.put('⌀', 'φ') // Diameter Sign = phi 
			.put('ø', 'φ') // Lowercase O with Stroke = phi
			.put('∈', 'ε') // Element Of = epsilon
			.put('€', 'ε') // Euro = epsilon
			.build();
	
	public static final int DIM_WHITE = 0xFFA8A8A8;
	
	public static void drawStringInverseVideo(int x, int y, String str, int color) {
		str = COLOR_CODE.matcher(str).replaceAll("");
		// this is kind of magic, so I'll explain it for anyone who happens to
		// be reading this that is curious
		GlStateManager.enableDepth();
		
			// enable depth writes
			GlStateManager.depthMask(true);
			// disable color writes
			GlStateManager.colorMask(false, false, false, false);
			
			GlStateManager.pushMatrix();
				// draw the text with a high depth value into the depth buffer
				GlStateManager.translate(0, 0, 1);
				drawString(x, y, str, 0);
			GlStateManager.popMatrix();
			
			// disable depth writes
			GlStateManager.depthMask(false);
			// enable color writes
			GlStateManager.colorMask(true, true, true, true);
			
			GlStateManager.pushMatrix();
				GlStateManager.scale(0.5f, 0.5f, 1);
				// due to depth test, the parts of the rectangle that are
				// "behind" our invisible text will not be rendered 
				Gui.drawRect(x*2, y*2, (x*2)+measureHalf(str), (y*2)+16, color);
			GlStateManager.popMatrix();
			
		GlStateManager.disableDepth();
	}
	public static void drawString(int x, int y, String str, int color) {
		str = COLOR_CODE.matcher(str).replaceAll("");
		if (!canRender(str)) {
			// fall back to unicode font - this usually happens when rendering non-english text
			boolean oldUnicode = Minecraft.getMinecraft().fontRenderer.getUnicodeFlag();
			Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(true);
			Minecraft.getMinecraft().fontRenderer.drawString(str, x, y, color);
			Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(oldUnicode);
			return;
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(BIOS);
		x*=2;
		y*=2;
		GlStateManager.pushMatrix();
		GlStateManager.color(((color >> 16)&0xFF)/255f, ((color >> 8)&0xFF)/255f, (color&0xFF)/255f);
		GlStateManager.scale(0.5, 0.5, 1);
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (substitutes.containsKey(c)) {
				c = substitutes.get(c);
			}
			int pos = CP437.indexOf(c);
			if (pos == -1) continue;
			int u = (pos%32)*9;
			int v = (pos/32)*16;
			Gui.drawModalRectWithCustomSizedTexture(x+(i*9), y, u, v, 9, 16, 288, 147);
		}
		GlStateManager.popMatrix();
	}

	public static boolean canRender(char c) {
		return substitutes.containsKey(c) || CP437.contains(Character.toString(c));
	}
	public static boolean canRender(String str) {
		str = COLOR_CODE.matcher(str).replaceAll("");
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!canRender(c)) {
				return false;
			}
		}
		return true;
	}
	public static int measureHalf(String str) {
		str = COLOR_CODE.matcher(str).replaceAll("");
		if (!canRender(str)) {
			return measureUnicode(str)*2;
		}
		return str.length()*9;
	}
	public static int measure(String str) {
		str = COLOR_CODE.matcher(str).replaceAll("");
		if (!canRender(str)) {
			measureUnicode(str);
		}
		return (int)(measureHalf(str)/2f);
	}
	private static int measureUnicode(String str) {
		boolean oldUnicode = Minecraft.getMinecraft().fontRenderer.getUnicodeFlag();
		Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(true);
		int len = Minecraft.getMinecraft().fontRenderer.getStringWidth(str);
		Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(oldUnicode);
		return len;
	}
}
