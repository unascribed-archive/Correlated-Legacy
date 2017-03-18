package com.elytradev.correlated.client.gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.client.IBMFontRenderer;
import com.elytradev.correlated.network.AnimationSeenMessage;
import com.elytradev.correlated.proxy.ClientProxy;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;

public class GuiDocumentation extends GuiScreen {

	private static final ResourceLocation tablet = new ResourceLocation("correlated", "textures/gui/tablet.png");
	private static final ResourceLocation tablet_overlay = new ResourceLocation("correlated", "textures/gui/tablet_overlay.png");
	
	private static String[] bootText = {
			"",
			"Testing main memory... \r",
			":PAUSE 100",
			"done",
			"",
			"  COMDOS DIGITAL HANDBOOK  ",
			"       Model DOC-8412      ",
			"     Machine ID #01823     ",
			"---------------------------",
			"       Copyright (c)       ",
			"  Correlated Logistics Ltd ",
			"    All Rights Reserved    ",
			"",
			"sonic0: no cable attached",
			"xa0: no cable attached",
			"",
			":DATE",
			":INVERSE WARN clock may be wrong",
			"",
			"syncing with server...",
			":PAUSE 10",
			"RETRIEVE /index.lst",
			":PAUSE 20",
			":INVERSE ERR 004 Not Found",
			"",
			":INVERSE WARN CITP error during sync",
			"using local documentation:",
			"v0.0.1a dated Sep 4 1987",
			":INVERSE WARN documentation outdated",
			"",
			"compiling index... \r",
			":PAUSE 80",
			"done",
			":PAUSE 80",
			"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
			"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
		};
	
	private static final String[] spinner = {
			"\\",
			"|",
			"/",
			"-",
	};
	
	private String topic;
	private String domain;
	
	private int animationTicks = 0;
	private int delayTicks = 0;
	
	private int bootIdx = -3;
	private List<String> anim = Lists.newArrayList();
	
	public GuiDocumentation(String topic, String domain, boolean playAnimation) {
		this.topic = topic;
		this.domain = domain;
		if (!playAnimation) {
			bootIdx = bootText.length+1;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		int xSize = 144;
		int ySize = 236;
		GlStateManager.pushMatrix();
			GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
			int bg = Correlated.proxy.getColor("terminal", 0);
			GlStateManager.color(((bg >> 16)&0xFF)/255f, ((bg >> 8)&0xFF)/255f, (bg&0xFF)/255f);
			Minecraft.getMinecraft().getTextureManager().bindTexture(tablet);
			drawTexturedModalRect(0, 0, 0, 0, 144, 236);
			
			GlStateManager.color(1, 1, 1);
			int rows = 25;
			int cols = 27;
			GlStateManager.pushMatrix();
				GlStateManager.translate(11, 12, 0);
				int x = 0;
				int y = 0;
				int skip = anim.size() < rows ? 0 : anim.size()-rows;
				for (String str : anim) {
					if (skip > 0) {
						skip--;
						continue;
					}
					boolean merge = str.endsWith("\r");
					boolean inverse = str.startsWith(":INVERSE ");
					if (merge) {
						str = str.substring(0, str.length()-1);
					}
					if (inverse) {
						str = str.substring(9);
					}
					if (inverse) {
						IBMFontRenderer.drawStringInverseVideo(x, y, str, Correlated.proxy.getColor("terminal", 1));
					} else {
						IBMFontRenderer.drawString(x, y, str, Correlated.proxy.getColor("terminal", 1));
					}
					if (merge) {
						x = IBMFontRenderer.measure(str);
					} else {
						x = 0;
						y += 8;
					}
				}
				if (bootIdx < bootText.length) {
					IBMFontRenderer.drawString(x, y, spinner[animationTicks%spinner.length], Correlated.proxy.getColor("terminal", 1));
				}
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
				GlStateManager.colorMask(false, false, false, false);
				GlStateManager.depthMask(true);
				GlStateManager.enableDepth();
				GlStateManager.translate(0, 0, 16);
				
				Gui.drawRect(10, 10, 10+(xSize-20), 10+(ySize-11), 0xFFFFFFFF);
				
				GlStateManager.colorMask(true, true, true, true);
				GlStateManager.depthMask(false);
				
				GlStateManager.depthFunc(GL11.GL_EQUAL);
				
				GlStateManager.translate(0, (ClientProxy.ticks%336)-100, 0);
				int c = Correlated.proxy.getColor("terminal", 1) & 0x00FFFFFF;
				drawGradientRect(11, 0, 336, 100, c, c | 0x44000000);
				
				GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.popMatrix();
			
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			
			GlStateManager.pushMatrix();
				GlStateManager.color(1, 1, 1);
				GlStateManager.translate(0, 0, 32);
				Minecraft.getMinecraft().getTextureManager().bindTexture(tablet_overlay);
				drawTexturedModalRect(0, 0, 0, 0, 144, 236);
			GlStateManager.popMatrix();
			
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}
	
	@Override
	public void updateScreen() {
		animationTicks++;
		delayTicks--;
		if (delayTicks <= 0) {
			bootIdx++;
			if (bootIdx < bootText.length) {
				String str;
				if (bootIdx == -2) {
					str = "Real memory: "+(((Runtime.getRuntime().totalMemory())/1024)/1024)+"M";
				} else if (bootIdx == -1) {
					str = "Free memory: "+(((Runtime.getRuntime().freeMemory())/1024)/1024)+"M";
				} else {
					str = bootText[bootIdx];
				}
				if (str.startsWith(":PAUSE ")) {
					delayTicks = Integer.parseInt(str.substring(7));
				} else if (str.startsWith(":MERGE")) {
					anim.set(anim.size()-1, anim.get(anim.size()-1)+str.substring(6));
					delayTicks = 5+(int)(((Math.random()-0.5))*2);
				} else if (str.equals(":DATE")) {
					anim.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").format(new Date()));
					delayTicks = 5+(int)(((Math.random()-0.5))*2);
				} else {
					anim.add(str);
					delayTicks = str.isEmpty() ? 1 : 5+(int)(((Math.random()-0.5))*8);
				}
			} else if (bootIdx == bootText.length) {
				new AnimationSeenMessage().sendToServer();
			}
		}
	}
	
}
