package com.elytradev.correlated.client.gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.lwjgl.opengl.GL11;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.client.DocumentationPage;
import com.elytradev.correlated.client.IBMFontRenderer;
import com.elytradev.correlated.network.AnimationSeenMessage;
import com.elytradev.correlated.proxy.ClientProxy;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;

public class GuiDocumentation extends GuiScreen {

	private static final ResourceLocation tablet = new ResourceLocation("correlated", "textures/gui/tablet.png");
	private static final ResourceLocation tablet_shadow = new ResourceLocation("correlated", "textures/gui/tablet_shadow.png");
	private static final ResourceLocation tablet_scanlines = new ResourceLocation("correlated", "textures/gui/tablet_scanlines.png");
	private static final ResourceLocation tablet_overlay = new ResourceLocation("correlated", "textures/gui/tablet_overlay.png");
	
	private static String[] bootText = {
			"Starting $version...",
			"",
			"Testing main memory... \r",
			":PAUSE 100",
			"done",
			"",
			"  COMDOS DIGITAL HANDBOOK  ",
			"       Model DOC-8412      ",
			"     Machine ID #$uuid     ",
			"---------------------------",
			"       Copyright (c)       ",
			"  Correlated Logistics Ltd ",
			"    All Rights Reserved    ",
			"",
			"sonic0: no cable attached",
			"xa0: no cable attached",
			"",
			":DATE",
			"WARN clock may be wrong",
			"",
			"syncing with server...",
			":PAUSE 10",
			"RETRIEVE /index.lst",
			":PAUSE 20",
			"ERR 004 Not Found",
			"",
			"WARN CITP error during sync",
			"using local documentation:",
			"v0.0.1a dated Sep 4 1987",
			"WARN documentation outdated",
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
			animationTicks = 30;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		
		float rot = 0;
		if (bootIdx >= bootText.length) {
			if ((animationTicks+partialTicks) > 20) {
				rot = 90;
			} else {
				float f = 1-((animationTicks+partialTicks)/20f);
				f *= Math.PI;
				double cos = (Math.cos(f)+1)*45;
				rot = (float)cos;
			}
		}
		int xSize = 144;
		int ySize = 236;
		GlStateManager.pushMatrix();
		
			int bg = Correlated.proxy.getColor("terminal", 0);
		
			GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
			GlStateManager.translate(xSize/2f, ySize/2f, 0);
			GlStateManager.rotate(rot, 0, 0, 1);
			GlStateManager.translate(-xSize/2f, -ySize/2f, 0);
			
			GlStateManager.color(1, 1, 1);
			int rows = 25;
			int cols = 27;
			
			GlStateManager.pushMatrix();
				GlStateManager.color(1, 1, 1);
				GlStateManager.colorMask(false, false, false, false);
				GlStateManager.depthMask(true);
				GlStateManager.enableDepth();
				GlStateManager.translate(0, 0, 32);
				
				drawRect(0, 0, 144, 236, 0xFFFFFFFF);
				
				GlStateManager.enableBlend();
				GlStateManager.disableAlpha();
				GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				
				GlStateManager.popMatrix(); GlStateManager.popMatrix();
					GlStateManager.pushMatrix();
						GlStateManager.translate((width - 256) / 2, (height - 256) / 2, 32);
						
						GlStateManager.depthFunc(GL11.GL_EQUAL);
						
						GlStateManager.color(((bg >> 16)&0xFF)/255f, ((bg >> 8)&0xFF)/255f, (bg&0xFF)/255f);
						GlStateManager.colorMask(true, true, true, true);
						GlStateManager.depthMask(false);
						Minecraft.getMinecraft().getTextureManager().bindTexture(tablet);
						drawTexturedModalRect(0, 0, 0, 0, 256, 256);
					GlStateManager.popMatrix();
					
					GlStateManager.pushMatrix();
						GlStateManager.translate(0, 0.5f, 0);
						GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 32);
						GlStateManager.translate(xSize/2f, ySize/2f, 0);
						GlStateManager.rotate(rot, 0, 0, 1);
						GlStateManager.translate(-xSize/2f, -ySize/2f, 0);
						GlStateManager.color(1, 1, 1);
						GlStateManager.translate(11, 12, 0);
						if (bootIdx < bootText.length) {
							int x = 0;
							int y = 0;
							int skip = anim.size() < rows ? 0 : anim.size()-rows;
							for (String str : anim) {
								if (skip > 0) {
									skip--;
									continue;
								}
								boolean merge = str.endsWith("\r");
								if (merge) {
									str = str.substring(0, str.length()-1);
								}
								IBMFontRenderer.drawString(x, y, str, Correlated.proxy.getColor("terminal", 1));
								if (merge) {
									x = IBMFontRenderer.measure(str);
								} else {
									x = 0;
									y += 8;
								}
							}
							IBMFontRenderer.drawString(x, y, spinner[animationTicks%spinner.length], Correlated.proxy.getColor("terminal", 1));
						} else {
							GlStateManager.translate(0, ySize-25, 0);
							GlStateManager.rotate(-90, 0, 0, 1);
							Future<DocumentationPage> future = ClientProxy.documentationManager.getPage(domain, topic);
							if (future.isDone()) {
								if (future.isCancelled()) {
									IBMFontRenderer.drawString(108, 50, "!", Correlated.proxy.getColor("terminal", 1));
									IBMFontRenderer.drawString(88, 58, "Interrupted", Correlated.proxy.getColor("terminal", 1));
								} else {
									try {
										DocumentationPage page = future.get();
										if (page == null) {
											IBMFontRenderer.drawString(108, 50, "‼", Correlated.proxy.getColor("terminal", 1));
											IBMFontRenderer.drawString(90, 58, "Not Found", Correlated.proxy.getColor("terminal", 1));
										} else {
											int fg = Correlated.proxy.getColor("terminal", 1);
											page.render(0, 0, 0, 0, Math.min(page.getWidth(), ySize), Math.min(page.getHeight(), xSize), fg);
										}
									} catch (Exception e) {
										e.printStackTrace();
										IBMFontRenderer.drawString(108, 50, "!", Correlated.proxy.getColor("terminal", 1));
										String str = e.toString();
										IBMFontRenderer.drawString(98, 58, "Failed", Correlated.proxy.getColor("terminal", 1));
										IBMFontRenderer.drawString(108-(IBMFontRenderer.measure(str)/2), 66, str, Correlated.proxy.getColor("terminal", 1));
									}
								}
							} else {
								IBMFontRenderer.drawString(108, 50, spinner[animationTicks%spinner.length], Correlated.proxy.getColor("terminal", 1));
								IBMFontRenderer.drawString(95, 58, "Loading", Correlated.proxy.getColor("terminal", 1));
							}
						}
					GlStateManager.popMatrix();
					
					GlStateManager.pushMatrix();
						GlStateManager.translate((width - 256) / 2, (height - 256) / 2, 32);
						GlStateManager.pushMatrix();
							GlStateManager.translate(0, (ClientProxy.ticks%384)-128, 0);
							int c = Correlated.proxy.getColor("terminal", 1) & 0x00FFFFFF;
							drawGradientRect(0, 0, 356, 128, c, c | 0x44000000);
						GlStateManager.popMatrix();
						GlStateManager.enableBlend();
						GlStateManager.disableAlpha();
						Minecraft.getMinecraft().getTextureManager().bindTexture(tablet_scanlines);
						drawTexturedModalRect(0, 0, 0, 0, 256, 256);
					GlStateManager.popMatrix();
					
					GlStateManager.pushMatrix();
						GlStateManager.translate(0, 0.5f, 0);
						GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 32);
						GlStateManager.translate(xSize/2f, ySize/2f, 0);
						GlStateManager.rotate(rot, 0, 0, 1);
						GlStateManager.translate(-xSize/2f, -ySize/2f, 0);
						GlStateManager.color(1, 1, 1);
						Minecraft.getMinecraft().getTextureManager().bindTexture(tablet_shadow);
						GlStateManager.enableBlend();
						GlStateManager.disableAlpha();
						drawTexturedModalRect(0, 0, 0, 0, 256, 256);
					GlStateManager.popMatrix();
				GlStateManager.pushMatrix(); GlStateManager.pushMatrix();
				
				GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
				GlStateManager.translate(xSize/2f, ySize/2f, 0);
				GlStateManager.rotate(rot, 0, 0, 1);
				GlStateManager.translate(-xSize/2f, -ySize/2f, 0);
				
				GlStateManager.disableDepth();
				
				GlStateManager.depthFunc(GL11.GL_LEQUAL);
				GlStateManager.color(1, 1, 1);
				Minecraft.getMinecraft().getTextureManager().bindTexture(tablet_overlay);
				drawTexturedModalRect(0, 0, 0, 0, 144, 236);
				
				GlStateManager.enableDepth();
			GlStateManager.popMatrix();
			
		GlStateManager.popMatrix();
		
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		
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
					if (str.contains("$uuid")) {
						int id = (int)((mc.player.getGameProfile().getId().getMostSignificantBits()>>32L)&0xFFFFFFFF);
						str = str.replace("$uuid", Strings.padStart(Integer.toString(Math.abs(id%100000)), 5, '0'));
					}
					if (str.contains("$version")) {
						str = str.replace("$version", Correlated.VERSION.equals("@VERSION@") ? "<dev>" : "v"+Correlated.VERSION);
					}
					anim.add(str);
					delayTicks = str.isEmpty() ? 1 : 5+(int)(((Math.random()-0.5))*8);
				}
			} else if (bootIdx == bootText.length) {
				new AnimationSeenMessage().sendToServer();
				animationTicks = 0;
			}
		}
	}
	
}
