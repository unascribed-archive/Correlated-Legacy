package com.elytradev.correlated.client.gui.shell;

import org.lwjgl.input.Keyboard;

import com.elytradev.correlated.client.IBMFontRenderer;

import net.minecraft.client.Minecraft;
import com.elytradev.correlated.C28n;

public class RSOD extends Program {
	private String error;
	
	public RSOD(GuiTerminalShell parent, String error) {
		super(parent);
		this.error = error;
	}

	@Override
	public void render(int rows, int cols) {
		int w = (int)(cols * 4.5f);
		int h = (int)(rows * 8f);
		String header = " FATAL ERROR ";
		int len = (int)(header.length() * 4.5f);
		drawStringInverseVideo((w/2)-(len/2), (h/2)-16, header);
		String key = "tooltip.correlated.controller_error."+error;
		String errorLocal = C28n.format(key);
		float errLen = IBMFontRenderer.measure(errorLocal);
		drawString((w/2)-(errLen/2), (h/2), errorLocal);
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			Minecraft.getMinecraft().player.closeScreen();
		}
	}

	@Override
	public String getName() {
		return "RSOD.DLL";
	}

	@Override
	public void update() {

	}

}
