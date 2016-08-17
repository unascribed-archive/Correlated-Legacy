package io.github.elytra.copo.network;

import io.github.elytra.copo.client.gui.GuiVT;
import io.github.elytra.copo.client.gui.GuiVTLog;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AddStatusLineMessage implements IMessage, IMessageHandler<AddStatusLineMessage, IMessage> {
	public int windowId;
	public String line;

	public AddStatusLineMessage() {}
	public AddStatusLineMessage(int windowId, String line) {
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
	public IMessage onMessage(AddStatusLineMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			GuiScreen open = Minecraft.getMinecraft().currentScreen;
			if (open instanceof GuiVT) {
				GuiVT vt = ((GuiVT)open);
				if (vt.inventorySlots.windowId == message.windowId) {
					vt.addLine(message.line);
				}
			} else if (open instanceof GuiVTLog) {
				GuiVTLog vt = ((GuiVTLog)open);
				if (vt.container.windowId == message.windowId) {
					vt.addLine(message.line);
				}
			}
		});
		return null;
	}

}
