package io.github.elytra.copo.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.copo.CoPo;
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
		super(CoPo.inst.network);
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
