package io.github.elytra.copo.client.gui;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.text.NumberFormat;
import java.util.Iterator;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.IBMFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeVersion;

public class GuiFakeReboot extends GuiScreen {
	private static final ResourceLocation BIOS = new ResourceLocation("correlatedpotentialistics", "textures/gui/bios.png");
	
	private int ticks = -40;
	private int cursorY = 0;
	
	private float heap = 0;
	private float perm = 0;
	
	public GuiFakeReboot() {
		Iterator<MemoryPoolMXBean> iter = ManagementFactory.getMemoryPoolMXBeans().iterator();
		while (iter.hasNext()) {
			MemoryPoolMXBean item = iter.next();
			String name = item.getName();
			MemoryType type = item.getType();
			MemoryUsage usage = item.getUsage();
			if ("Perm Gen".equals(name) || "Metaspace".equals(name)) {
				perm += usage.getCommitted();
			} else if (type == MemoryType.HEAP) {
				heap += usage.getCommitted();
			}
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawRect(0, 0, width, height, 0xFF000000);
		if (ticks < 0) return;
		float ticks = this.ticks+partialTicks;
		Minecraft.getMinecraft().getTextureManager().bindTexture(BIOS);
		GlStateManager.color(1, 1, 1);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		if (ticks <= 20) {
			int logoColor = 0xD9000D;
			int opacity = 255;
			if (ticks > 8) {
				opacity -= (int)(((ticks-8)/8f)*255);
				if (opacity <= 1) {
					opacity = 0;
				}
			}
			int w = (int)((Math.min(ticks, 8)/8f)*126);
			drawTexturedModalRect(10, 10, 72, 0, w, 19);
			drawRect(10, 10, w+10, 29, logoColor | (opacity << 24));
		} else {
			if (ticks < 125) drawTexturedModalRect(10, 10, 72, 0, 126, 19);
			if (ticks > 22) drawIBMString(10, 40, "Ender 80386 ROM BIOS PLUS Version "+Minecraft.getMinecraft().getVersion()+" "+ForgeVersion.buildVersion);
			if (ticks > 26) drawIBMString(10, 47, "Copyright (C) 2009-2016 Mojang AB.");
			if (ticks > 28) drawIBMString(10, 54, "All Rights Reserved");
			if (ticks > 28) drawIBMString(10, 68, "19920225151230");
			if (ticks > 94) {
				float heap = this.heap;
				float perm = this.perm;
				int heapLen = Integer.toString((int)(heap/1024/1024)).length();
				int permLen = Integer.toString((int)(perm/1024/1024)).length();
				if (ticks < 100) {
					float div = 16-(ticks-84);
					if (div > 1) {
						heap /= div;
						perm /= div;
					}
				}
				NumberFormat permF = NumberFormat.getIntegerInstance();
				permF.setMinimumIntegerDigits(permLen);
				permF.setGroupingUsed(false);
				NumberFormat heapF = NumberFormat.getIntegerInstance();
				heapF.setMinimumIntegerDigits(heapLen);
				heapF.setGroupingUsed(false);
				drawIBMString(10, 96, permF.format((int)(perm/1024/1024))+"M Base Memory, "+heapF.format((int)(heap/1024/1024))+"M Extended");
			}
			if (ticks > 110 && ticks <= 130) {
				int opacity = (int)(((ticks-110)/20f)*255);
				if (opacity > 10 && opacity < 250) {
					drawRect(10, 10, 136, 29, opacity << 24);
				}
			}
			if (ticks > 130) drawIBMString(10, 117, "Starting MC-DOS...");
			if (ticks > 180) drawIBMString(10, 145, "HIMEM is testing extended memory..."+(ticks > 200?"done":""));
			if (ticks > 220) drawIBMString(10, 159, "C:\\>C:\\DOS\\SMARTDRV.EXE /X");
			if (ticks > 225) drawIBMString(10, 166, "C:\\>C:\\MOJANG\\MINECRFT.EXE");
			if (ticks > 230) drawIBMString(10, 173, "Starting Minecraft...");
			if (this.ticks % 10 < 5) {
				drawIBMString(10, cursorY, "_");
			}
		}
	}
	
	private void drawIBMString(int x, int y, String str) {
		IBMFontRenderer.drawString(x, y, str);
		cursorY = y+7;
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void updateScreen() {
		ticks++;
		if (ticks == 30) {
			Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(CoPo.glitchfloppy, 1f));
		}
		if (ticks == 115) {
			Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(CoPo.glitchboot, 1f));
		}
		if (ticks > 250) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiGlitchedMainMenu());
		}
	}
	
	@Override public void handleKeyboardInput() throws IOException {}
}
