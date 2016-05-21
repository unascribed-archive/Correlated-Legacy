package io.github.elytra.copo.network;

import org.apache.commons.lang3.mutable.MutableInt;

import io.github.elytra.copo.CoPo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StartWeldthrowingMessage implements IMessage, IMessageHandler<StartWeldthrowingMessage, IMessage> {
	public int entityId;
	
	@Override
	public void fromBytes(ByteBuf buf) {
		entityId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityId);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(StartWeldthrowingMessage message, MessageContext ctx) {
		Entity e = Minecraft.getMinecraft().theWorld.getEntityByID(message.entityId);
		if (e instanceof EntityPlayer) {
			CoPo.weldthrower.weldthrowing.put((EntityPlayer)e, new MutableInt());
		}
		return null;
	}

}
