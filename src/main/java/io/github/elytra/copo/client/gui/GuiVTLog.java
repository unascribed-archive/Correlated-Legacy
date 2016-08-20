package io.github.elytra.copo.client.gui;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import com.google.common.base.Strings;

import io.github.elytra.copo.client.ClientProxy;
import io.github.elytra.copo.client.IBMFontRenderer;
import io.github.elytra.copo.inventory.ContainerVT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class GuiVTLog extends GuiScreen {
	private GuiVT guiVt;
	public ContainerVT container;
	private StringBuilder command = new StringBuilder();
	public GuiVTLog(GuiVT guiVt, ContainerVT container) {
		this.guiVt = guiVt;
		this.container = container;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawRect(4, 4, width-4, height-4, 0xFF28393f);
		drawGradientRect(16, 16, width-16, height-16, 0xFF01926B, 0xFF006D4B);
		drawRect(width-32, height-12, width-16, height-8, 0xFF00DBAD);
		int lines = ((height-40)/8)-1;
		int cols = (int)((width-40)/4.5);
		int y = 20;
		for (int i = Math.max(0, container.status.size()-lines); i < container.status.size(); i++) {
			String line = Strings.nullToEmpty(container.status.get(i));
			if (line.length() > cols) {
				line = line.substring(0, cols-3)+"...";
			}
			IBMFontRenderer.drawString(20, y, line, 0x00DBAD);
			y += 8;
		}
		if (command.length() > (cols-4)) {
			command.setLength(cols-4);
		}
		IBMFontRenderer.drawString(20, y, "J:\\>"+command+(ClientProxy.ticks % 20 < 10 ? "_" : ""), 0x00DBAD);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			Minecraft.getMinecraft().displayGuiScreen(guiVt);
		} else if (keyCode == Keyboard.KEY_RETURN) {
			container.status.add("J:\\>"+command);
			String cmd = command.toString();
			if (cmd.length() == 2 && cmd.endsWith(":")) {
				if (!cmd.equalsIgnoreCase("J:")) {
					container.status.add("Not ready reading drive "+cmd.toUpperCase(Locale.ROOT).charAt(0));
				}
			} else if (!cmd.isEmpty()) {
				String[] split = cmd.split("[ /]", 2);
				switch (split[0].toLowerCase(Locale.ROOT)) {
					case "help":
						container.status.add("help - print this help");
						container.status.add("part - report disk space usage");
						container.status.add("free - display memory usage statistics");
						container.status.add("echo - display a line of text");
						container.status.add("exit - exit the shell");
						break;
					case "exit":
						Minecraft.getMinecraft().displayGuiScreen(guiVt);
						break;
					case "echo":
						container.status.add(split.length >= 2 ? split[1] : "");
						break;
					case "part":
						Minecraft.getMinecraft().playerController.sendEnchantPacket(container.windowId, -22);
						break;
					case "free":
						Minecraft.getMinecraft().playerController.sendEnchantPacket(container.windowId, -23);
						break;
					case "download":
						if (split.length == 1 || Strings.isNullOrEmpty(split[1])) {
							container.status.add("Download what?");
						} else {
							switch (split[1]) {
								case "ram":
								case "more ram":
									container.status.add("fatal: cannot connect to downloadmoreram.com");
									container.status.add("do you have an internet card?");
									break;
								default:
									container.status.add("fatal: don't know how to download '"+split[1]+"'");
									break;
							}
						}
						break;
					default:
						container.status.add("Bad command or file name");
						break;
				}
			}
			command.setLength(0);
		} else if (keyCode == Keyboard.KEY_BACK) {
			if (command.length() >= 1) {
				command.setLength(command.length()-1);
			}
		} else if (isCtrlKeyDown()) {
			switch (keyCode) {
				case Keyboard.KEY_C:
					container.status.add("J:\\>"+command+"^C");
					command.setLength(0);
					break;
				case Keyboard.KEY_D:
					Minecraft.getMinecraft().displayGuiScreen(guiVt);
					break;
			}
		} else if (typedChar != 0 && IBMFontRenderer.canRender(typedChar)) {
			command.append(typedChar);
		}
	}
	
	private Random rand = new Random(System.nanoTime());
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		if (rand.nextInt() == 5) {
			container.status.add("Your free upgrade to Windows 10 is available!");
		}
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

