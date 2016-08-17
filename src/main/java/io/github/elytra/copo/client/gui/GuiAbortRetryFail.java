package io.github.elytra.copo.client.gui;

import java.io.IOException;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.IBMFontRenderer;
import io.github.elytra.copo.network.LeaveDungeonMessage;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;

public class GuiAbortRetryFail extends GuiGameOver {
	
	private final ITextComponent causeOfDeath;
	private int ticks;
	
	public GuiAbortRetryFail(ITextComponent p_i46598_1_) {
		super(p_i46598_1_);
		this.causeOfDeath = p_i46598_1_;
	}
	
	@Override
	public void initGui() {
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawRect(0, 0, width, height, 0xFF000000);
		String txt = causeOfDeath == null ? "You died" : causeOfDeath.getUnformattedText();
		drawIBMString(10, 40, txt.substring(0, Math.min(ticks, txt.length())));
		int base = txt.length()+10;
		if (ticks >= base) {
			drawIBMString(10, 48, "Abort/Retry?"+(ticks % 10 < 5 ? "_" : ""));
		}
		if (ticks > base+20) drawIBMString(10, 80, "[A]bort: Return to the overworld");
		if (ticks > base+22) drawIBMString(10, 88, "         You will lose the items you found, but");
		if (ticks > base+24) drawIBMString(10, 96, "         your Unstable Pearl can be used again");
		
		if (ticks > base+30) drawIBMString(10, 112, "[R]etry: Respawn within the dungeon");
		if (ticks > base+32) drawIBMString(10, 120, "         You will have to find your items, but");
		if (ticks > base+34) drawIBMString(10, 128, "         they will not despawn");

		if (ticks > base+100) drawIBMString(10, 200, "Press A or R to make a decision");
	}
	
	private void drawIBMString(int x, int y, String str) {
		IBMFontRenderer.drawString(x, y, str, IBMFontRenderer.DIM_WHITE);
	}
	
	@Override
	public void updateScreen() {
		ticks++;
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (ticks >= (causeOfDeath == null ? "You died" : causeOfDeath.getUnformattedText()).length()+10) {
			if (typedChar == 'r' || typedChar == 'R') {
				this.mc.thePlayer.respawnPlayer();
				this.mc.displayGuiScreen((GuiScreen) null);
			} else if (typedChar == 'a' || typedChar == 'A') {
				CoPo.inst.network.sendToServer(new LeaveDungeonMessage());
				this.mc.thePlayer.respawnPlayer();
				this.mc.displayGuiScreen((GuiScreen) null);
			}
		}
	}

}
