package io.github.elytra.copo.client.gui.shell;

import java.util.Locale;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Strings;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.IBMFontRenderer;
import io.github.elytra.copo.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class CommandInterpreter extends Program {
	private StringBuilder command = new StringBuilder();
	public CommandInterpreter(GuiTerminalShell parent) {
		super(parent);
	}
	
	@Override
	public String getName() {
		return "COMMAND.COM";
	}
	
	@Override
	public void render(int rows, int cols) {
		int y = 0;
		for (int i = Math.max(0, parent.container.status.size()-(rows-1)); i < parent.container.status.size(); i++) {
			String line = Strings.nullToEmpty(parent.container.status.get(i));
			if (line.length() > cols) {
				line = line.substring(0, cols-3)+"...";
			}
			IBMFontRenderer.drawString(0, y, line, CoPo.proxy.getColor("other", 64));
			y += 8;
		}
		if (command.length() > (cols-4)) {
			command.setLength(cols-4);
		}
		IBMFontRenderer.drawString(0, y, "J:\\>"+command+(ClientProxy.ticks % 20 < 10 ? "_" : ""), CoPo.proxy.getColor("other", 64));
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			Minecraft.getMinecraft().displayGuiScreen(parent.guiTerminal);
		} else if (keyCode == Keyboard.KEY_RETURN) {
			parent.container.status.add("J:\\>"+command);
			String cmd = command.toString();
			if (cmd.length() == 2 && cmd.endsWith(":")) {
				if (!cmd.equalsIgnoreCase("J:")) {
					parent.container.status.add("Not ready reading drive "+cmd.toUpperCase(Locale.ROOT).charAt(0));
				}
			} else if (!cmd.isEmpty()) {
				String[] split = cmd.split("[ /]", 2);
				switch (split[0].toLowerCase(Locale.ROOT)) {
					case "help":
						parent.container.status.add("help - print this help");
						parent.container.status.add("part - report disk space usage");
						parent.container.status.add("free - display memory usage statistics");
						parent.container.status.add("echo - display a line of text");
						parent.container.status.add("exit - exit the shell");
						parent.container.status.add("ide - write programs for automatons");
						break;
					case "exit":
						Minecraft.getMinecraft().displayGuiScreen(parent.guiTerminal);
						break;
					case "echo":
						parent.container.status.add(split.length >= 2 ? split[1] : "");
						break;
					case "part":
						Minecraft.getMinecraft().playerController.sendEnchantPacket(parent.container.windowId, -22);
						break;
					case "free":
						Minecraft.getMinecraft().playerController.sendEnchantPacket(parent.container.windowId, -23);
						break;
					case "download":
						if (split.length == 1 || Strings.isNullOrEmpty(split[1])) {
							parent.container.status.add("Download what?");
						} else {
							switch (split[1]) {
								case "ram":
								case "more ram":
									parent.container.status.add("fatal: cannot connect to downloadmoreram.com");
									parent.container.status.add("do you have an internet card?");
									break;
								default:
									parent.container.status.add("fatal: don't know how to download '"+split[1]+"'");
									break;
							}
						}
						break;
					case "ide":
						parent.program = new AutomatonProgrammer(parent);
						break;
					case "437":
						parent.container.status.add("\0☺☻♥♦♣♠•◘○◙♂♀♪♫☼►◄↕‼¶§▬↨↑↓→←∟↔▲▼");
						parent.container.status.add(" !\"#$%&'()*+,-./0123456789:;<=>?");
						parent.container.status.add("@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_");
						parent.container.status.add("`abcdefghijklmnopqrstuvwxyz{|}~⌂");
						parent.container.status.add("ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜ¢£¥₧ƒ");
						parent.container.status.add("áíóúñÑªº¿⌐¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐");
						parent.container.status.add("└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀");
						parent.container.status.add("αβΓπΣσμτΦΘΩδ∞φε∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u00a0");
						break;
					default:
						parent.container.status.add("Bad command or file name");
						break;
				}
			}
			command.setLength(0);
		} else if (keyCode == Keyboard.KEY_BACK) {
			if (command.length() >= 1) {
				command.setLength(command.length()-1);
			}
		} else if (GuiScreen.isCtrlKeyDown()) {
			switch (keyCode) {
				case Keyboard.KEY_C:
					parent.container.status.add("J:\\>"+command+"^C");
					command.setLength(0);
					break;
				case Keyboard.KEY_D:
					Minecraft.getMinecraft().displayGuiScreen(parent.guiTerminal);
					break;
			}
		} else if (typedChar != 0 && IBMFontRenderer.canRender(typedChar)) {
			command.append(typedChar);
		}
	}
	
	private Random rand = new Random(System.nanoTime());
	
	@Override
	public void update() {
		if (rand.nextInt() == 5) {
			parent.container.status.add("Your free upgrade to Windows 10 is available!");
		}
	}

}
