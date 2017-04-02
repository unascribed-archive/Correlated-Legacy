package com.elytradev.correlated.network;

import com.elytradev.correlated.client.gui.GuiTerminal;
import com.elytradev.correlated.client.gui.shell.GuiTerminalShell;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class AddStatusLineMessage extends Message {
	@MarshalledAs("i32")
	public int windowId;
	@MarshalledAs(TextComponentMarshaller.NAME)
	public ITextComponent line;

	public AddStatusLineMessage(NetworkContext ctx) {
		super(ctx);
	}
	public AddStatusLineMessage(int windowId, ITextComponent line) {
		super(CNetwork.CONTEXT);
		this.windowId = windowId;
		this.line = line;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		GuiScreen open = Minecraft.getMinecraft().currentScreen;
		if (open instanceof GuiTerminal) {
			GuiTerminal terminal = ((GuiTerminal)open);
			if (terminal.inventorySlots.windowId == windowId) {
				terminal.addLine(line);
			}
		} else if (open instanceof GuiTerminalShell) {
			GuiTerminalShell shell = ((GuiTerminalShell)open);
			if (shell.container.windowId == windowId) {
				shell.addLine(line);
			}
		}
	}

}
