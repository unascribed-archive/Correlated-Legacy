package io.github.elytra.copo.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.gui.shell.AutomatonProgrammer;
import io.github.elytra.copo.client.gui.shell.GuiVTShell;
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
		super(CoPo.inst.network);
		this.windowId = windowId;
		this.line = line;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		GuiScreen open = Minecraft.getMinecraft().currentScreen;
		if (open instanceof GuiVTShell) {
			GuiVTShell vt = ((GuiVTShell)open);
			if (vt.container.windowId == windowId && vt.program instanceof AutomatonProgrammer) {
				((AutomatonProgrammer)vt.program).setStatus(line);
			}
			}
	}
}
