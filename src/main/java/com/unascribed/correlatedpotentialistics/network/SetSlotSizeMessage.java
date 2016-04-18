package com.unascribed.correlatedpotentialistics.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SetSlotSizeMessage implements IMessage, IMessageHandler<SetSlotSizeMessage, IMessage> {
	public int windowId;
	public int slot;
	public int slotSize;
	
	public SetSlotSizeMessage() {}
	public SetSlotSizeMessage(int windowId, int slot, int slotSize) {
		this.windowId = windowId;
		this.slot = slot;
		this.slotSize = slotSize;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		windowId = buf.readInt();
		slot = buf.readInt();
		slotSize = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(windowId);
		buf.writeInt(slot);
		buf.writeInt(slotSize);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(SetSlotSizeMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			Container c = Minecraft.getMinecraft().thePlayer.openContainer;
			if (c.windowId == message.windowId) {
				Slot slot = c.getSlot(message.slot);
				ItemStack stack = slot.getStack();
				if (stack != null) {
					stack.stackSize = message.slotSize;
				}
				slot.putStack(stack);
			}
		});
		return null;
	}

}
