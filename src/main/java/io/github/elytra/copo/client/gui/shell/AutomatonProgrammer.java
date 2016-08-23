package io.github.elytra.copo.client.gui.shell;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Strings;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.ClientProxy;
import io.github.elytra.copo.client.IBMFontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class AutomatonProgrammer extends Program {

	public AutomatonProgrammer(GuiVTShell parent) {
		super(parent);
	}

	@Override
	public void render(int rows, int cols) {
		drawStringInverseVideo(0, 0, Strings.padEnd("  Automaton IDE 1.0.0 - New Buffer", cols, ' '));
		
		if ((ClientProxy.ticks % 10) < 5) {
			drawString(0, 1, "_");
		}
		
		drawStringInverseVideo(0, rows-1, "^O");
		drawString(3, rows-1, "Save");
		
		drawStringInverseVideo(8, rows-1, "^X");
		drawString(11, rows-1, "Exit");
		
		drawStringInverseVideo(16, rows-1, "^Y");
		drawString(19, rows-1, "Prev");
		
		drawStringInverseVideo(24, rows-1, "^V");
		drawString(27, rows-1, "Next");
		
		drawStringInverseVideo(32, rows-1, "^K");
		drawString(35, rows-1, "Kill");
		
		drawStringInverseVideo(40, rows-1, "^U");
		drawString(43, rows-1, "UnKill");
	}

	private void drawStringInverseVideo(int x, int y, String str) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x*4.5f, y*8f, 0);
		IBMFontRenderer.drawStringInverseVideo(0, 0, str, CoPo.proxy.getColor("other", 64));
		GlStateManager.popMatrix();
	}
	
	private void drawString(int x, int y, String str) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x*4.5f, y*8f, 0);
		IBMFontRenderer.drawString(0, 0, str, CoPo.proxy.getColor("other", 64));
		GlStateManager.popMatrix();
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			parent.program = new CommandInterpreter(parent);
		}
	}

	@Override
	public String getName() {
		return "IDE.EXE";
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

}
