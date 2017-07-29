package com.elytradev.correlated.client.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.elytradev.correlated.network.wireless.APNRequestMessage;
import com.elytradev.correlated.wifi.IWirelessClient;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiSelectAPN extends GuiScreen {

	private static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");
	private static final ResourceLocation BG = new ResourceLocation("correlated", "textures/gui/select_apn.png");
	
	private boolean hasRequestedAPNs = false;
	
	private GuiScreen parent;
	private boolean client;
	private boolean multiple;
	private IWirelessClient iwc;
	
	private List<Pair<String, Integer>> apns = Lists.newArrayList();
	private Set<String> selected = Sets.newHashSet();
	
	private GuiTextField creatingTextField;
	
	private float scrollY = 0;
	private boolean draggingScrollKnob = false;

	public GuiSelectAPN(GuiScreen parent, boolean client, boolean multiple, IWirelessClient iwc) {
		this.parent = parent;
		this.client = client;
		this.multiple = multiple;
		this.iwc = iwc;
	}
	
	@Override
	public void onGuiClosed() {
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (!hasRequestedAPNs) {
			hasRequestedAPNs = true;
			new APNRequestMessage(iwc.getX(), iwc.getY(), iwc.getZ(), iwc.getPosition()).sendToServer();
		}
		drawDefaultBackground();
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1);
		int transX = (width-176)/2;
		int transY = (height-147)/2;
		GlStateManager.translate(transX, transY, 0);
		mouseX -= transX;
		mouseY -= transY;
		mc.getTextureManager().bindTexture(BG);
		drawTexturedModalRect(7, 18, 0, 147, 144, 109);
		
		int x = 7;
		int y = 18;
		int start = y;
		int end = y+109;
		
		if (creatingTextField != null) {
			if (!creatingTextField.isFocused()) {
				creatingTextField = null;
			} else {
				scrollY = 0;
				mc.getTextureManager().bindTexture(WIDGETS);
				drawTexturedModalRect(x, y, 0, 66, 72, 20);
				drawTexturedModalRect(x+72, y, 128, 66, 72, 20);
				creatingTextField.setTextColor(-1);
				creatingTextField.setEnableBackgroundDrawing(false);
				creatingTextField.xPosition = x+5;
				creatingTextField.yPosition = y+6;
				creatingTextField.width = 135;
				creatingTextField.height = 8;
				creatingTextField.drawTextBox();
				GlStateManager.color(1, 1, 1);
				y += 19;
			}
		}
		
		if (scrollY > apns.size()-1) scrollY = apns.size()-1;
		
		y -= scrollY*19;
		
		for (Pair<String, Integer> pair : apns) {
			if (y+19 < start) {
				y += 19;
				continue;
			}
			if (y > end) break;
			mc.getTextureManager().bindTexture(WIDGETS);
			boolean hover = false;
			int rawColor = -1;
			if (mouseY < end &&
					mouseX > x && mouseX < x+144 &&
					mouseY > y && mouseY <= y+19) {
				hover = true;
				rawColor = 0xFFFFFFA0;
			}
			if (pair.getLeft().equals("§r<None>") ? selected.isEmpty() : selected.contains(pair.getLeft())) {
				rawColor = hover ? 0xFFA0DBAD : 0xFF00DBAD;
			}
			int shadow = (rawColor & 16579836) >> 2 | rawColor & -16777216;
			drawTexturedModalRect(x, y, 0, hover ? 86 : 66, 72, 20);
			drawTexturedModalRect(x+72, y, 128, hover ? 86 : 66, 72, 20);
			// this code sucks
			int color = shadow;
			x += 1;
			y += 1;
			for (int i = 0; i < 2; i++) {
				fontRenderer.drawString(pair.getLeft(), x+5, y+6, color);
				if (client && pair.getRight() != -1) {
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
					mc.getTextureManager().bindTexture(BG);
					drawTexturedModalRect((x+144)-20, y+6, 240, 0, 16, 8);
					drawTexturedModalRect((x+144)-20, y+6, 240, 8, 5+(pair.getRight()*2), 8);
					GlStateManager.disableBlend();
				}
				if (i == 0) {
					color = rawColor;
					x -= 1;
					y -= 1;
				}
			}
			GlStateManager.color(1, 1, 1);
			y += 19;
		}
		mc.getTextureManager().bindTexture(BG);
		
		drawTexturedModalRect(0, 0, 0, 0, 176, 147);
		if (apns.size() < 6) {
			drawTexturedModalRect(156, 19, 244, 241, 12, 15);
		} else {
			int scrollHandleY = 19;
			int height = 107-15;
			scrollHandleY += height*(scrollY/(apns.size()-1));
			drawTexturedModalRect(156, scrollHandleY, 232, 241, 12, 15);
		}
		fontRenderer.drawString(I18n.format("gui.correlated.select_apn"+(multiple ? "_multiple" : "")), 7, 6, 0x404040);
		GlStateManager.popMatrix();
		super.drawScreen(mouseX+transX, mouseY+transY, partialTicks);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			keyTyped('\0', 1);
		} else if (button.id == 1) {
			creatingTextField = new GuiTextField(2, fontRenderer, 0, 0, 144, 12);
			creatingTextField.setFocused(true);
		}
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		int dWheel = Mouse.getDWheel();
		scrollY -= dWheel/240f;
		if (scrollY < 0) {
			scrollY = 0;
		}
		super.handleMouseInput();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (creatingTextField != null) {
			creatingTextField.textboxKeyTyped(typedChar, keyCode);
		}
		if (keyCode == 1) {
			if (creatingTextField != null) {
				creatingTextField = null;
			} else {
				mc.displayGuiScreen(parent);
	
				if (mc.currentScreen == null) {
					mc.setIngameFocus();
				}
				iwc.setAPNs(selected);
			}
		} else if (keyCode == Keyboard.KEY_RETURN && creatingTextField != null) {
			String str = creatingTextField.getText().trim();
			if (str.isEmpty()) {
				creatingTextField = null;
				return;
			} else {
				if (!multiple) {
					selected.clear();
				}
				boolean add = true;
				for (Pair<String, Integer> pair : apns) {
					if (str.equals(pair.getLeft())) {
						add = false;
						break;
					}
				}
				if (add) {
					apns.add(0, Pair.of(str, 5));
				}
				selected.add(str);
				creatingTextField = null;
			}
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (creatingTextField != null) {
			creatingTextField.mouseClicked(mouseX, mouseY, mouseButton);
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseButton == 0) {
			int transX = (width-176)/2;
			int transY = (height-147)/2;
			mouseX -= transX;
			mouseY -= transY;
			if (mouseX > 156 && mouseX < 156+19 &&
					mouseY > 19 && mouseY < 19+107) {
				draggingScrollKnob = true;
				float pos = Math.max(0, Math.min(1, ((mouseY-22)/107f)));
				scrollY = apns.size()*pos;
			} else if (creatingTextField == null) {
				int x = 7;
				int y = 18;
				int start = y;
				int end = y+109;
				y -= scrollY*19;
				
				for (Pair<String, Integer> pair : apns) {
					if (y+19 < start) {
						y += 19;
						continue;
					}
					if (y > end) break;
					if (mouseX > x && mouseX < x+144 &&
							mouseY > y && mouseY <= y+19) {
						mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
						if (!multiple) {
							selected.clear();
						}
						if (!pair.getLeft().equals("§r<None>")) {
							if (selected.contains(pair.getLeft())) {
								selected.remove(pair.getLeft());
							} else {
								selected.add(pair.getLeft());
							}
						}
						break;
					}
					y += 19;
				}
			}
		}
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		if (draggingScrollKnob) {
			int transY = (height-147)/2;
			mouseY -= transY;
			float pos = Math.max(0, Math.min(1, ((mouseY-22)/107f)));
			scrollY = apns.size()*pos;
		}
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if (draggingScrollKnob && state == 0) {
			draggingScrollKnob = false;
		}
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		if (creatingTextField != null) {
			creatingTextField.updateCursorCounter();
		}
	}
	
	@Override
	public void initGui() {
		Mouse.getDWheel();
		super.initGui();
		int transX = (width-176)/2;
		int transY = (height-147)/2;
		addButton(new GuiButtonExt(0, transX+128, transY+128, 40, 15, I18n.format("gui.done")));
		if (!client) {
			addButton(new GuiButtonExt(1, transX+7, transY+128, 40, 15, I18n.format("gui.correlated.new")));
		}
	}

	public void updateAPNList(List<Pair<String, Integer>> li, List<String> selected) {
		this.apns = Lists.newArrayList(li);
		if (client) {
			Collections.sort(this.apns, (a, b) -> {
				return ComparisonChain.start()
					.compare(b.getRight(), a.getRight())
					.compare(a.getLeft(), b.getLeft())
					.result();
			});
		} else {
			Collections.sort(this.apns, (a, b) -> a.getLeft().compareTo(b.getLeft()));
		}
		this.apns.add(0, Pair.of("§r<None>", -1));
		this.selected = Sets.newHashSet(selected);
	}

}
