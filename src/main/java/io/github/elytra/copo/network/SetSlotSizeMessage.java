package io.github.elytra.copo.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.copo.CoPo;
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
		super(CoPo.inst.network);
		this.windowId = windowId;
		this.slot = slot;
		this.slotSize = slotSize;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Container c = Minecraft.getMinecraft().thePlayer.openContainer;
		if (c.windowId == windowId) {
			Slot s = c.getSlot(slot);
			ItemStack stack = s.getStack();
			if (stack != null) {
				stack.stackSize = slotSize;
			}
			s.putStack(stack);
		}
	}

}
