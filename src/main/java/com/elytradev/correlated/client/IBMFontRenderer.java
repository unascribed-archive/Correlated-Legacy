package com.elytradev.correlated.client;

import java.util.Map;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
	
	public static void drawStringInverseVideo(float x, float y, String str, int color) {
		str = COLOR_CODE.matcher(str).replaceAll("");
		// this is kind of magic, so I'll explain it for anyone who happens to
		// be reading this that is curious
		GlStateManager.enableDepth();
			GlStateManager.depthFunc(GL11.GL_LEQUAL);
		
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
				drawRect(x*2, y*2, (x*2)+measureDirect(str), (y*2)+16, color);
			GlStateManager.popMatrix();
			
		GlStateManager.disableDepth();
	}
	public static void drawString(float x, float y, String str, int color) {
		str = COLOR_CODE.matcher(str).replaceAll("");
		if (!canRender(str)) {
			// fall back to unicode font - this usually happens when rendering non-english text
			boolean oldUnicode = Minecraft.getMinecraft().fontRenderer.getUnicodeFlag();
			Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(true);
			Minecraft.getMinecraft().fontRenderer.drawString(str, x, y, color, false);
			Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(oldUnicode);
			return;
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(BIOS);
		x*=2;
		y*=2;
		GlStateManager.pushMatrix();
		GlStateManager.color(((color >> 16)&0xFF)/255f, ((color >> 8)&0xFF)/255f, (color&0xFF)/255f);
		GlStateManager.scale(0.5, 0.5, 1);
		GlStateManager.translate(x, y, 0);
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (substitutes.containsKey(c)) {
				c = substitutes.get(c);
			}
			int pos = CP437.indexOf(c);
			if (pos == -1) continue;
			int u = (pos%32)*9;
			int v = (pos/32)*16;
			Gui.drawModalRectWithCustomSizedTexture(0, 0, u, v, 9, 16, 288, 147);
			GlStateManager.translate(9, 0, 0);
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
	public static int measureDirect(String str) {
		str = COLOR_CODE.matcher(str).replaceAll("");
		if (!canRender(str)) {
			return measureUnicode(str)*2;
		}
		return str.length()*9;
	}
	public static float measure(String str) {
		str = COLOR_CODE.matcher(str).replaceAll("");
		if (!canRender(str)) {
			measureUnicode(str);
		}
		return measureDirect(str)/2f;
	}
	private static int measureUnicode(String str) {
		boolean oldUnicode = Minecraft.getMinecraft().fontRenderer.getUnicodeFlag();
		Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(true);
		int len = Minecraft.getMinecraft().fontRenderer.getStringWidth(str);
		Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(oldUnicode);
		return len;
	}
	public static void drawRect(float left, float top, float right, float bottom, int color) {
		if (left < right) {
			float swap = left;
			left = right;
			right = swap;
		}

		if (top < bottom) {
			float swap = top;
			top = bottom;
			bottom = swap;
		}

		float a = (color >> 24 & 255) / 255f;
		float r = (color >> 16 & 255) / 255f;
		float g = (color >> 8 & 255) / 255f;
		float b = (color & 255) / 255f;
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(
				SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
				SourceFactor.ONE, DestFactor.ZERO);
		GlStateManager.color(r, g, b, a);
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		vb.pos(left, bottom, 0).endVertex();
		vb.pos(right, bottom, 0).endVertex();
		vb.pos(right, top, 0).endVertex();
		vb.pos(left, top, 0).endVertex();
		tess.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
}
