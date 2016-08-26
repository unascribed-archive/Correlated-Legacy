package io.github.elytra.copo.network;

import io.github.elytra.copo.client.gui.shell.AutomatonProgrammer;
import io.github.elytra.copo.client.gui.shell.GuiVTShell;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SetEditorStatusMessage implements IMessage, IMessageHandler<SetEditorStatusMessage, IMessage> {
	public int windowId;
	public String line;

	public SetEditorStatusMessage() {}
	public SetEditorStatusMessage(int windowId, String line) {
		this.windowId = windowId;
		this.line = line;
	}
	@Override
	public void fromBytes(ByteBuf buf) {
		windowId = buf.readInt();
		line = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(windowId);
		ByteBufUtils.writeUTF8String(buf, line);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(SetEditorStatusMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			GuiScreen open = Minecraft.getMinecraft().currentScreen;
			if (open instanceof GuiVTShell) {
				GuiVTShell vt = ((GuiVTShell)open);
				if (vt.container.windowId == message.windowId && vt.program instanceof AutomatonProgrammer) {
					((AutomatonProgrammer)vt.program).setStatus(message.line);
				}
			}
		});
		return null;
	}
}
