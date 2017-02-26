package com.elytradev.correlated.client.gui.shell;

import java.util.Locale;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.elytradev.correlated.client.IBMFontRenderer;
import com.elytradev.correlated.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

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
			String line = parent.container.status.get(i).getFormattedText();
			if (line.length() > cols) {
				line = line.substring(0, cols-3)+"...";
			}
			drawString(0, y, line);
			y += 8;
		}
		if (command.length() > (cols-4)) {
			command.setLength(cols-4);
		}
		drawString(0, y, "J:\\>"+command+(ClientProxy.ticks % 20 < 10 ? "_" : ""));
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			Minecraft.getMinecraft().displayGuiScreen(parent.guiTerminal);
		} else if (keyCode == Keyboard.KEY_RETURN) {
			parent.container.status.add(new TextComponentString("J:\\>"+command));
			String cmd = command.toString();
			if (cmd.length() == 2 && cmd.endsWith(":")) {
				if (!cmd.equalsIgnoreCase("J:")) {
					parent.container.status.add(new TextComponentTranslation("correlated.shell.notReady", cmd.toUpperCase(Locale.ROOT).charAt(0)));
				}
			} else if (!cmd.isEmpty()) {
				String[] split = cmd.split("[ /]", 2);
				switch (split[0].toLowerCase(Locale.ROOT)) {
					case "help":
						parent.container.status.add(new TextComponentTranslation("correlated.shell.help.help"));
						parent.container.status.add(new TextComponentTranslation("correlated.shell.help.part"));
						parent.container.status.add(new TextComponentTranslation("correlated.shell.help.free"));
						parent.container.status.add(new TextComponentTranslation("correlated.shell.help.echo"));
						parent.container.status.add(new TextComponentTranslation("correlated.shell.help.exit"));
						parent.container.status.add(new TextComponentTranslation("correlated.shell.help.ide"));
						break;
					case "exit":
						Minecraft.getMinecraft().displayGuiScreen(parent.guiTerminal);
						break;
					case "echo":
						parent.container.status.add(new TextComponentString(split.length >= 2 ? split[1] : ""));
						break;
					case "part":
						Minecraft.getMinecraft().playerController.sendEnchantPacket(parent.container.windowId, -22);
						break;
					case "free":
						Minecraft.getMinecraft().playerController.sendEnchantPacket(parent.container.windowId, -23);
						break;
					case "ide":
						parent.program = new AutomatonProgrammer(parent);
						break;
					case "437":
						parent.container.status.add(new TextComponentString("\0☺☻♥♦♣♠•◘○◙♂♀♪♫☼►◄↕‼¶§▬↨↑↓→←∟↔▲▼"));
						parent.container.status.add(new TextComponentString(" !\"#$%&'()*+,-./0123456789:;<=>?"));
						parent.container.status.add(new TextComponentString("@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_"));
						parent.container.status.add(new TextComponentString("`abcdefghijklmnopqrstuvwxyz{|}~⌂"));
						parent.container.status.add(new TextComponentString("ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜ¢£¥₧ƒ"));
						parent.container.status.add(new TextComponentString("áíóúñÑªº¿⌐¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐"));
						parent.container.status.add(new TextComponentString("└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀"));
						parent.container.status.add(new TextComponentString("αβΓπΣσμτΦΘΩδ∞φε∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u00a0"));
						break;
					default:
						parent.container.status.add(new TextComponentTranslation("correlated.shell.badCommand"));
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
					parent.container.status.add(new TextComponentString("J:\\>"+command+"^C"));
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
			parent.container.status.add(new TextComponentTranslation("correlated.shell.egg"));
		}
	}

}
