package io.github.elytra.correlated.client.gui.shell;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.client.gui.GuiTerminal;
import io.github.elytra.correlated.inventory.ContainerTerminal;
import io.github.elytra.correlated.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;

public class GuiTerminalShell extends GuiScreen {
	public GuiTerminal guiTerminal;
	public ContainerTerminal container;
	private static final ResourceLocation terminal = new ResourceLocation("correlated", "textures/gui/shell.png");
	private static final ResourceLocation terminal_overlay = new ResourceLocation("correlated", "textures/gui/shell_overlay.png");
	public Program program = new CommandInterpreter(this);
	
	public int palette = 0;
	
	public GuiTerminalShell(GuiTerminal guiTerminal, ContainerTerminal container) {
		this.guiTerminal = guiTerminal;
		this.container = container;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		int xSize = 256;
		int ySize = 144;
		GlStateManager.pushMatrix();
			GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
			int bg = Correlated.proxy.getColor("terminal", palette*4);
			GlStateManager.color(((bg >> 16)&0xFF)/255f, ((bg >> 8)&0xFF)/255f, (bg&0xFF)/255f);
			Minecraft.getMinecraft().getTextureManager().bindTexture(terminal);
			drawTexturedModalRect(0, 0, 0, 0, 256, 144);
			
			GlStateManager.color(1, 1, 1);
			int rows = 15;
			int cols = 51;
			GlStateManager.pushMatrix();
				GlStateManager.translate(13, 12, 0);
				program.render(rows, cols);
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
				ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
				GL11.glScissor((int)((((width - xSize)/2f)+10)*res.getScaleFactor()), (int)((((height - ySize)/2f)+10.375f)*res.getScaleFactor()), (int)(((cols*4.5f)+5f)*res.getScaleFactor()), (int)((rows*8)+3.5f)*res.getScaleFactor());
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				GlStateManager.translate(0, (ClientProxy.ticks%244)-100, 16);
				int c = Correlated.proxy.getColor("terminal", (palette*4)+1) & 0x00FFFFFF;
				drawGradientRect(11, 0, 244, 100, c, c | 0x44000000);
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();
			
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			
			GlStateManager.pushMatrix();
				GlStateManager.color(1, 1, 1);
				GlStateManager.translate(0, 0, 32);
				Minecraft.getMinecraft().getTextureManager().bindTexture(terminal_overlay);
				drawTexturedModalRect(0, 0, 0, 0, 256, 144);
			GlStateManager.popMatrix();
			
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		program.keyTyped(typedChar, keyCode);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		program.update();
	}
	
	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}
	
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	public void addLine(String line) {
		container.status.add(line);
	}

}

