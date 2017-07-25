package com.elytradev.correlated.network;

import com.elytradev.correlated.init.CNetwork;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class SetSlotSizeMessage extends Message {
	@MarshalledAs("i32")
	public int windowId;
	@MarshalledAs("i32")
	public int slot;
	@MarshalledAs("i32")
	public int slotSize;

	public SetSlotSizeMessage(NetworkContext ctx) {
		super(ctx);
	}
	public SetSlotSizeMessage(int windowId, int slot, int slotSize) {
		super(CNetwork.CONTEXT);
		this.windowId = windowId;
		this.slot = slot;
		this.slotSize = slotSize;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Container c = Minecraft.getMinecraft().player.openContainer;
		if (c.windowId == windowId) {
			Slot s = c.getSlot(slot);
			ItemStack stack = s.getStack();
			stack.setCount(slotSize);
			s.putStack(stack);
		}
	}

}
