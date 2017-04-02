package com.elytradev.correlated.network;

import com.elytradev.correlated.init.CNetwork;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class InsertAllMessage extends Message {

	@MarshalledAs("i32")
	public int windowId;
	public ItemStack template;
	
	public InsertAllMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public InsertAllMessage(int windowId, ItemStack template) {
		super(CNetwork.CONTEXT);
		this.windowId = windowId;
		this.template = template;
	}

	@Override
	protected void handle(EntityPlayer sender) {
		if (sender.openContainer.windowId == windowId) {
			for (Slot s : sender.openContainer.inventorySlots) {
				if (s.inventory instanceof InventoryPlayer && s.getHasStack()
						&& ItemStack.areItemsEqual(s.getStack(), template)
						&& ItemStack.areItemStackTagsEqual(s.getStack(), template)) {
					sender.openContainer.slotClick(s.slotNumber, 0, ClickType.QUICK_MOVE, sender);
				}
			}
		}
	}

}
