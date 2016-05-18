package com.unascribed.correlatedpotentialistics.network;

import com.unascribed.correlatedpotentialistics.client.gui.GuiVT;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SetSearchQueryMessage implements IMessage, IMessageHandler<SetSearchQueryMessage, IMessage> {
	public int windowId;
	public String query;

	public SetSearchQueryMessage() {}
	public SetSearchQueryMessage(int windowId, String query) {
		this.windowId = windowId;
		this.query = query;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		windowId = buf.readInt();
		query = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(windowId);
		ByteBufUtils.writeUTF8String(buf, query);
	}

	@Override
	public IMessage onMessage(SetSearchQueryMessage message, MessageContext ctx) {
		if (ctx.side == Side.SERVER) {
			handleServer(message, ctx);
		} else if (ctx.side == Side.CLIENT) {
			handleClient(message, ctx);
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	private void handleClient(SetSearchQueryMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			GuiScreen open = Minecraft.getMinecraft().currentScreen;
			if (open instanceof GuiVT) {
				GuiVT vt = ((GuiVT)open);
				if (vt.inventorySlots.windowId == message.windowId) {
					vt.updateSearchQuery(message.query);
				}
			}
		});
	}
	private void handleServer(SetSearchQueryMessage message, MessageContext ctx) {
		((WorldServer)ctx.getServerHandler().playerEntity.worldObj).addScheduledTask(() -> {
			Container c = ctx.getServerHandler().playerEntity.openContainer;
			if (c instanceof ContainerVT && c.windowId == message.windowId) {
				((ContainerVT)c).updateSearchQuery(message.query);
			}
		});
	}

}
