package io.github.elytra.copo.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SlotStatic extends Slot {
	private ItemStack stack;

	public SlotStatic(int index, int xPosition, int yPosition, ItemStack stack) {
		super(null, index, xPosition, yPosition);
		this.stack = stack;
	}
	
	@Override
	public ItemStack getStack() {
		return stack;
	}

	@Override
	public void putStack(ItemStack stack) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			this.stack = stack;
		}
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		return null;
	}
	
	@Override
	public boolean isHere(IInventory inv, int slotIn) {
		return false;
	}
	
	@Override
	public boolean canBeHovered() {
		return false;
	}
	
	@Override
	public boolean canTakeStack(EntityPlayer playerIn) {
		return false;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public void onSlotChanged() {}
}