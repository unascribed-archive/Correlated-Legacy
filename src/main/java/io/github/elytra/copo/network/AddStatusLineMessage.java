package io.github.elytra.copo.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.gui.GuiVT;
import io.github.elytra.copo.client.gui.shell.GuiVTShell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class AddStatusLineMessage extends Message {
	@MarshalledAs("i32")
	public int windowId;
	public String line;

	public AddStatusLineMessage(NetworkContext ctx) {
		super(ctx);
	}
	public AddStatusLineMessage(int windowId, String line) {
		super(CoPo.inst.network);
		this.windowId = windowId;
		this.line = line;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		GuiScreen open = Minecraft.getMinecraft().currentScreen;
		if (open instanceof GuiVT) {
			GuiVT vt = ((GuiVT)open);
			if (vt.inventorySlots.windowId == windowId) {
				vt.addLine(line);
			}
		} else if (open instanceof GuiVTShell) {
			GuiVTShell vt = ((GuiVTShell)open);
			if (vt.container.windowId == windowId) {
				vt.addLine(line);
			}
		}
	}

}
