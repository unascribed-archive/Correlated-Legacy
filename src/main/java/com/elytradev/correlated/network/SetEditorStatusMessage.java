package com.elytradev.correlated.network;

import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.client.gui.shell.AutomatonProgrammer;
import com.elytradev.correlated.client.gui.shell.GuiTerminalShell;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class SetEditorStatusMessage extends Message {
	@MarshalledAs("i32")
	public int windowId;
	public String line;

	public SetEditorStatusMessage(NetworkContext ctx) {
		super(ctx);
	}
	public SetEditorStatusMessage(int windowId, String line) {
		super(CNetwork.CONTEXT);
		this.windowId = windowId;
		this.line = line;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		GuiScreen open = Minecraft.getMinecraft().currentScreen;
		if (open instanceof GuiTerminalShell) {
			GuiTerminalShell shell = ((GuiTerminalShell)open);
			if (shell.container.windowId == windowId && shell.program instanceof AutomatonProgrammer) {
				((AutomatonProgrammer)shell.program).setStatus(line);
			}
			}
	}
}
