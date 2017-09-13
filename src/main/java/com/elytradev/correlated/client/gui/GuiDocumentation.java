package com.elytradev.correlated.client.gui;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.elytradev.correlated.ColorType;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.client.DocumentationPage;
import com.elytradev.correlated.client.IBMFontRenderer;
import com.elytradev.correlated.client.DocumentationPage.ClickRegion;
import com.elytradev.correlated.client.DocumentationPage.NavigateAction;
import com.elytradev.correlated.network.documentation.AnimationSeenMessage;
import com.elytradev.correlated.proxy.ClientProxy;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;

public class GuiDocumentation extends GuiScreen {

	private static class HistoryEntry {
		public String domain;
		public String topic;
		public float scroll;
		
		public HistoryEntry(String domain, String topic, float scroll) {
			this.domain = domain;
			this.topic = topic;
			this.scroll = scroll;
		}
	}
	
	private static final ResourceLocation tablet = new ResourceLocation("correlated", "textures/gui/tablet.png");
	private static final ResourceLocation tablet_shadow = new ResourceLocation("correlated", "textures/gui/tablet_shadow.png");
	private static final ResourceLocation tablet_scanlines = new ResourceLocation("correlated", "textures/gui/tablet_scanlines.png");
	private static final ResourceLocation tablet_overlay = new ResourceLocation("correlated", "textures/gui/tablet_overlay.png");
	private static final ResourceLocation tablet_buttons = new ResourceLocation("correlated", "textures/gui/tablet_buttons.png");
	
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
			"!! CONSISTENCY CHECK FAILED",
			"PLEASE TAKE YOUR HANDBOOK",
			"TO THE CLOSEST SERVICE",
			"CENTER FOR REPAIRS",
			":PAUSE 40",
			"continuing anyway...",
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
	
	private Deque<HistoryEntry> historyBackward = Queues.newArrayDeque();
	private Deque<HistoryEntry> historyForward = Queues.newArrayDeque();
	
	private String topic;
	private String domain;
	
	private int animationTicks = 0;
	private int delayTicks = 0;
	
	private int bootIdx = -3;
	private List<String> anim = Lists.newArrayList();
	
	private float scroll;
	private float lagScroll;
	
	private boolean highContrast = false;
	
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
		if (OpenGlHelper.isFramebufferEnabled()) {
			if (!Float.isFinite(scroll)) {
				scroll = 0;
			}
			if (!Float.isFinite(lagScroll)) {
				lagScroll = 0;
			}
			
			int bg = highContrast ? 0xFF222222 : ColorType.PALETTE.getColor(0);
			int fg = highContrast ? 0xFFFFFFFF : ColorType.PALETTE.getColor(1);
			
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
			
				GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
				GlStateManager.translate(xSize/2f, ySize/2f, 0);
				GlStateManager.rotate(rot, 0, 0, 1);
				GlStateManager.translate(-xSize/2f, -ySize/2f, 0);
				
				GlStateManager.color(1, 1, 1);
				
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
							
							GlStateManager.depthFunc(GL11.GL_GEQUAL);
							
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
								float x = 0;
								float y = 0;
								int skip = anim.size() < 25 ? 0 : anim.size()-25;
								for (String str : anim) {
									if (skip > 0) {
										skip--;
										continue;
									}
									boolean merge = str.endsWith("\r");
									if (merge) {
										str = str.substring(0, str.length()-1);
									}
									IBMFontRenderer.drawString(x, y, str, fg);
									if (merge) {
										x = IBMFontRenderer.measure(str);
									} else {
										x = 0;
										y += 8;
									}
								}
								if (!anim.isEmpty() && anim.get(anim.size()-1).endsWith("\r")) {
									IBMFontRenderer.drawString(x, y, spinner[animationTicks%spinner.length], fg);
								}
							} else {
								boolean drewStatusBar = false;
								GlStateManager.translate(0, ySize-25, 0);
								GlStateManager.rotate(-90, 0, 0, 1);
								Future<DocumentationPage> future = ClientProxy.documentationManager.getPage(domain, topic);
								if (future.isDone()) {
									if (future.isCancelled()) {
										IBMFontRenderer.drawString(0, 0, "CITP Error 001", fg);
										IBMFontRenderer.drawString(0, 8, "Operation Interrupted", fg);
									} else {
										try {
											DocumentationPage page = future.get();
											if (page == null) {
												IBMFontRenderer.drawString(0, 0, "CITP Error 004", fg);
												IBMFontRenderer.drawString(0, 8, "Document Not Found", fg);
											} else {
												page.render(0, 0, 0, (int)lagScroll, Math.min(page.getWidth(), ySize), Math.min(page.getHeight(), xSize-30), fg);
												GlStateManager.enableAlpha();
												if (page.getHeight() > 136) {
													float knobH = 12;
													float knobY = (lagScroll/(page.getHeight()-128))*(xSize-5-24-knobH);
													
													drawRect(ySize-25, 0, ySize-23, xSize-5-24, bg);
													drawRect(ySize-25, (int)knobY, ySize-23, (int)(knobY+knobH), fg);
													
													float prog = Math.min(Math.max(lagScroll / (page.getHeight()-128), 0), 1);
													int pct = (int)(prog*100);
													String nm = page.getKey().substring(page.getKey().indexOf('.')).replace('.', '/').toUpperCase(Locale.ROOT)+".MD";
													IBMFontRenderer.drawStringInverseVideo(-2, xSize-29, Strings.padEnd(" "+nm+" ", 43, ' ')+Strings.padStart(Integer.toString(pct), 3, ' ')+"%  ", fg);
													GlStateManager.enableDepth();
													GlStateManager.depthFunc(GL11.GL_GEQUAL);
													drewStatusBar = true;
												} else {
													drawRect(ySize-25, 0, ySize-23, xSize-8-24, fg);
												}
											}
										} catch (Exception e) {
											e.printStackTrace();
											IBMFontRenderer.drawString(0, 0, "CITP Error 005", fg);
											IBMFontRenderer.drawString(0, 8, "Unexpected Internal Error", fg);
											String str = e.toString();
											IBMFontRenderer.drawString(0, 16, str, fg);
										}
									}
								} else {
									IBMFontRenderer.drawString(36, 0, spinner[animationTicks%spinner.length], fg);
									IBMFontRenderer.drawString(0, 0, "Loading", fg);
								}
								
								if (!drewStatusBar) {
									GlStateManager.enableAlpha();
									String nm = "/"+topic.replace('.', '/').toUpperCase(Locale.ROOT)+".MD";
									IBMFontRenderer.drawStringInverseVideo(-2, xSize-29, Strings.padEnd(" "+nm+" ", 50, ' '), fg);
									GlStateManager.enableDepth();
									GlStateManager.depthFunc(GL11.GL_GEQUAL);
								}
							}
						GlStateManager.popMatrix();
						
						if (!highContrast) {
							GlStateManager.pushMatrix();
								GlStateManager.translate((width - 256) / 2, (height - 256) / 2, 32);
								GlStateManager.pushMatrix();
									GlStateManager.translate(0, (ClientProxy.ticks%384)-128, 0);
									int c = fg & 0x00FFFFFF;
									drawGradientRect(0, 0, 356, 128, c, c | 0x44000000);
								GlStateManager.popMatrix();
								GlStateManager.enableBlend();
								GlStateManager.disableAlpha();
								Minecraft.getMinecraft().getTextureManager().bindTexture(tablet_scanlines);
								drawTexturedModalRect(0, 0, 0, 0, 256, 256);
							GlStateManager.popMatrix();
						}
						
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
					
					GlStateManager.rotate(-90, 0, 0, 1);
					GlStateManager.translate(-(ySize-11), 0, 0);
					Minecraft.getMinecraft().getTextureManager().bindTexture(tablet_buttons);
					GlStateManager.color(1, 1, 1);
					drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 13, 10, 215, 20);
					drawModalRectWithCustomSizedTexture(13, 0, 13, 0, 13, 10, 215, 20);
					drawModalRectWithCustomSizedTexture(203, 0, 203, highContrast ? 10 : 0, 12, 10, 215, 20);
					
				GlStateManager.popMatrix();
				
			GlStateManager.popMatrix();
			
			if (highContrast) {
				String str = "NON-CANON HIGH CONTRAST MODE";
				fontRenderer.drawString(str, width/2 - fontRenderer.getStringWidth(str) / 2, (height/2)+63.5f, 0, false);
			}
			GlStateManager.enableDepth();
			
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
		} else {
			int y = -60;
			drawCenteredString(fontRenderer, "§m------------§r Oh no! §m------------", width/2, (height/2)+y, 0xFFFF5555);
			y += 12;
			drawCenteredString(fontRenderer, "Your version of OpenGL is too old.", width/2, (height/2)+y, -1);
			y += 24;
			drawCenteredString(fontRenderer, "The Doc Tablet uses complex visual effects", width/2, (height/2)+y, -1);
			y += 12;
			drawCenteredString(fontRenderer, "that are not supported by your computer.", width/2, (height/2)+y, -1);
			y += 24;
			drawCenteredString(fontRenderer, "Please read the documentation online at", width/2, (height/2)+y, -1);
			y += 12;
			drawCenteredString(fontRenderer, "§nhttps://unascribed.com/correlated/", width/2, (height/2)+y, 0xFF5555FF);
			y += 24;
			drawCenteredString(fontRenderer, "Press ESC to leave this screen.", width/2, (height/2)+y, -1);
		}
	}
	
	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		super.initGui();
	}
	
	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		super.onGuiClosed();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == mc.gameSettings.keyBindScreenshot.getKeyCode()) {
			Future<DocumentationPage> future = ClientProxy.documentationManager.getPage(domain, topic);
			if (future.isDone() && !future.isCancelled()) {
				try {
					DocumentationPage page = future.get();
					BufferedImage img = ScreenShotHelper.createScreenshot(page.fb.framebufferWidth, page.fb.framebufferHeight, page.fb);
					BufferedImage target = new BufferedImage(page.fb.framebufferWidth, page.fb.framebufferHeight, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = target.createGraphics();
					g2d.drawImage(img, 0, page.fb.framebufferHeight, page.fb.framebufferWidth, page.fb.framebufferHeight, null);
					g2d.dispose();
					String fname = "correlated_doc-"+page.getKey()+".md.png";
					File f = new File(new File(mc.mcDataDir, "screenshots"), fname);
					ImageIO.write(target, "PNG", f);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		super.keyTyped(typedChar, keyCode);
		if (keyCode == Keyboard.KEY_R
				|| (keyCode == Keyboard.KEY_L && isCtrlKeyDown())
				|| keyCode == Keyboard.KEY_F5) {
			// r  ^R  ^L            Repaint screen.
			ClientProxy.documentationManager.invalidateCache();
		} else if (keyCode == Keyboard.KEY_E || keyCode == Keyboard.KEY_J
				|| (keyCode == Keyboard.KEY_N && isCtrlKeyDown())
				|| keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_DOWN) {
			// e  ^E  j  ^N  CR  *  Forward  one line   (or N lines).
			scroll += 8;
		} else if (keyCode == Keyboard.KEY_Y || keyCode == Keyboard.KEY_K
				|| (keyCode == Keyboard.KEY_P && isCtrlKeyDown())
				|| keyCode == Keyboard.KEY_UP) {
			// y  ^Y  k  ^K  ^P  *  Backward one line   (or N lines).
			scroll -= 8;
		} else if (keyCode == Keyboard.KEY_F
				|| (keyCode == Keyboard.KEY_V && isCtrlKeyDown())
				|| keyCode == Keyboard.KEY_SPACE || keyCode == Keyboard.KEY_NEXT
				|| keyCode == Keyboard.KEY_Z) {
			// f  ^F  ^V  SPACE  *  Forward  one window (or N lines).
			// z                 *  Forward  one window (and set window to N).
			scroll += 136;
		} else if (keyCode == Keyboard.KEY_B || keyCode == Keyboard.KEY_W
				|| keyCode == Keyboard.KEY_PRIOR) {
			// b  ^B  ESC-v      *  Backward one window (or N lines).
			// w                 *  Backward one window (and set window to N).
			scroll -= 136;
		} else if (keyCode == Keyboard.KEY_D) {
			// d  ^D             *  Forward  one half-window (and set half-window to N).
			scroll += 68;
		} else if (keyCode == Keyboard.KEY_U) {
			// u  ^U             *  Backward one half-window (and set half-window to N).
			scroll -= 68;
		} else if (keyCode == Keyboard.KEY_HOME) {
			scroll = 0;
		} else if (keyCode == Keyboard.KEY_END) {
			Future<DocumentationPage> future = ClientProxy.documentationManager.getPage(domain, topic);
			if (future.isDone() && !future.isCancelled()) {
				try {
					scroll = future.get().getHeight()-128;
				} catch (Exception e) {
				}
			}
		} else if (keyCode == Keyboard.KEY_LEFT && isAltKeyDown()) {
			back();
		} else if (keyCode == Keyboard.KEY_RIGHT && isAltKeyDown()) {
			forward();
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		int dWheel = Mouse.getDWheel();
		scroll += -(dWheel/4f);
		super.handleMouseInput();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (!OpenGlHelper.isFramebufferEnabled()) {
			if (mouseY >= (height/2)+24 && mouseY <= (height/2)+36) {
				try {
					Desktop.getDesktop().browse(new URI("https://unascribed.com/correlated/"));
				} catch (Exception e) {}
			}
		} else if (mouseButton == 0) {
			int xSize = 236;
			int ySize = 144;
			
			int x = (width - xSize) / 2;
			int y = (height - ySize) / 2;
			
			mouseX -= x;
			mouseY -= y;
			
			if (mouseY >= 0 && mouseY <= 10) {
				if (mouseX >= 214 && mouseX <= 226) {
					highContrast = !highContrast;
					mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1)); 
				} else if (mouseX >= 11 && mouseX <= 24) {
					back();
					mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
				} else if (mouseX >= 24 && mouseX <= 37) {
					forward();
					mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
				}
			}
			
			mouseX -= 12;
			mouseY -= 12;
			
			if (mouseX < xSize-24 && mouseY < ySize-24) {
				mouseY += (int)lagScroll;
				Future<DocumentationPage> future = ClientProxy.documentationManager.getPage(domain, topic);
				if (future.isDone() && !future.isCancelled()) {
					try {
						ClickRegion cr = future.get().getRegionClicked(mouseX, mouseY);
						if (cr != null) {
							if (cr.action instanceof NavigateAction) {
								navigate(((NavigateAction)cr.action).target.replace('/', '.').substring(1));
							}
						}
					} catch (Exception e) {
					}
				}
			}
		} else if (mouseButton == 3) {
			back();
		} else if (mouseButton == 4) {
			forward();
		}
	}
	
	public void navigate(String topic) {
		historyBackward.addLast(new HistoryEntry(domain, this.topic, scroll));
		historyForward.clear();
		this.topic = topic;
		scroll = 0;
		lagScroll = 0;
	}
	
	public void back() {
		HistoryEntry e = historyBackward.pollLast();
		if (e != null) {
			historyForward.addFirst(new HistoryEntry(domain, this.topic, scroll));
			this.domain = e.domain;
			this.topic = e.topic;
			this.lagScroll = e.scroll;
			this.scroll = e.scroll;
		}
	}
	
	public void forward() {
		HistoryEntry e = historyForward.pollFirst();
		if (e != null) {
			historyBackward.addLast(new HistoryEntry(domain, this.topic, scroll));
			this.domain = e.domain;
			this.topic = e.topic;
			this.lagScroll = e.scroll;
			this.scroll = e.scroll;
		}
	}
	
	@Override
	public void updateScreen() {
		animationTicks++;
		delayTicks--;
		if (scroll < 0) {
			scroll /= 4;
		}
		Future<DocumentationPage> future = ClientProxy.documentationManager.getPage(domain, topic);
		if (future.isDone() && !future.isCancelled()) {
			try {
				DocumentationPage page = future.get();
				if (page.getHeight() > 136) {
					if (scroll > (page.getHeight()-128)) {
						scroll = ((scroll-(page.getHeight()-128))/4f)+(page.getHeight()-128);
					}
				} else {
					scroll = 0;
				}
			} catch (Exception e) {
			}
		}
		float diff = scroll-lagScroll;
		lagScroll += diff/4f;
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
