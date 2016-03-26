package com.unascribed.correlatedpotentialistics.network;

import com.unascribed.correlatedpotentialistics.inventory.ContainerVT;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
		MinecraftServer.getServer().addScheduledTask(() -> {
			Container c = ctx.getServerHandler().playerEntity.openContainer;
			if (c instanceof ContainerVT && c.windowId == message.windowId) {
				((ContainerVT)c).updateSearchQuery(message.query);
			}
		});
		return null;
	}

}
