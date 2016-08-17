package io.github.elytra.copo.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AutomatonSpeakMessage implements IMessage, IMessageHandler<AutomatonSpeakMessage, IMessage> {
	public int entityId;
	public String line;
	
	@Override
	public void fromBytes(ByteBuf buf) {
		entityId = buf.readInt();
		line = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityId);
		ByteBufUtils.writeUTF8String(buf, line);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(AutomatonSpeakMessage message, MessageContext ctx) {
		
		return null;
	}

}
