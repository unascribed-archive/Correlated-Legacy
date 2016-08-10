package io.github.elytra.copo.network;

import io.github.elytra.copo.inventory.ContainerAutomaton;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetAutomatonNameMessage implements IMessage, IMessageHandler<SetAutomatonNameMessage, IMessage> {
	public int windowId;
	public String name;

	public SetAutomatonNameMessage() {}
	public SetAutomatonNameMessage(int windowId, String name) {
		this.windowId = windowId;
		this.name = name;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		windowId = buf.readInt();
		name = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(windowId);
		ByteBufUtils.writeUTF8String(buf, name);
	}

	@Override
	public IMessage onMessage(SetAutomatonNameMessage message, MessageContext ctx) {
		((WorldServer)ctx.getServerHandler().playerEntity.worldObj).addScheduledTask(() -> {
			Container c = ctx.getServerHandler().playerEntity.openContainer;
			if (c instanceof ContainerAutomaton && c.windowId == message.windowId) {
				((ContainerAutomaton)c).automaton.setCustomNameTag(message.name);
			}
		});
		return null;
	}

}
