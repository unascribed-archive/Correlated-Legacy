package io.github.elytra.correlated.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.client.gui.shell.AutomatonProgrammer;
import io.github.elytra.correlated.client.gui.shell.GuiTerminalShell;
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
		super(Correlated.inst.network);
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
