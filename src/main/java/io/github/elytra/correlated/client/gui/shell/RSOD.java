package io.github.elytra.correlated.client.gui.shell;

import org.lwjgl.input.Keyboard;

import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.client.IBMFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.translation.I18n;

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
		String errorLocal = I18n.translateToLocal(key);
		if (IBMFontRenderer.canRender(errorLocal)) {
			int errLen = (int)(errorLocal.length()*4.5f);
			drawString((w/2)-(errLen/2), (h/2), errorLocal);
		} else {
			FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
			boolean oldUnicode = fr.getUnicodeFlag();
			fr.setUnicodeFlag(true);
			int errLen = fr.getStringWidth(errorLocal);
			fr.drawString(errorLocal, (w/2)-(errLen/2), (h/2), Correlated.proxy.getColor("terminal", (parent.palette*4)+1));
			fr.setUnicodeFlag(oldUnicode);
		}
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
