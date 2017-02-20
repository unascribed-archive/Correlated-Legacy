package com.elytradev.correlated.inventory;

import com.elytradev.correlated.tile.TileEntityInterface;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerInterface extends Container {
	public class SlotFake extends Slot {
		public SlotFake(int index, int xPosition, int yPosition) {
			super(null, index, xPosition, yPosition);
		}

		private ItemStack stack = ItemStack.EMPTY;

		@Override
		public ItemStack getStack() {
			return stack;
		}

		@Override
		public void putStack(ItemStack stack) {
			this.stack = stack;
			onSlotChanged();
		}

		@Override
		public ItemStack decrStackSize(int amount) {
			ItemStack split = stack.splitStack(amount);
			onSlotChanged();
			return split;
		}

		@Override
		public int getItemStackLimit(ItemStack stack) {
			return 1;
		}

		@Override
		public boolean isHere(IInventory inv, int slotIn) {
			return false;
		}

		@Override
		public boolean canBeHovered() {
			return true;
		}

		@Override
		public boolean canTakeStack(EntityPlayer playerIn) {
			return false;
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			return true;
		}

		@Override
		public void onSlotChanged() {
		}
	}

	private TileEntityInterface te;
	private EntityPlayer player;

	public ContainerInterface(InventoryPlayer playerInventory, EntityPlayer player, TileEntityInterface te) {
		this.te = te;
		this.player = player;

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(te, i, 6+((i%3)*18), 18+((i/3)*18)));
		}

		for (int i = 0; i < 9; i++) {
			SlotFake slot = new SlotFake(i, 62+((i%3)*18), 18+((i/3)*18));
			slot.putStack(te.getOutputPrototype(i));
			addSlotToContainer(slot);
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(te, i+9, 118+((i%3)*18), 18+((i/3)*18)));
		}

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 142));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			result = stack.copy();

			if (index < 27) {
				if (!mergeItemStack(stack, 27, inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(stack, 0, 9, false)) {
				return ItemStack.EMPTY;
			}

			if (stack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return result;
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if (clickTypeIn == ClickType.QUICK_CRAFT) return player.inventory.getItemStack();
		if (slotId >= 1) {
			Slot slot = getSlot(slotId);
			if (slot instanceof SlotFake) {
				if (clickTypeIn == ClickType.PICKUP) {
					if (slot.getHasStack()) {
						slot.putStack(ItemStack.EMPTY);
						te.setOutputPrototype(slot.getSlotIndex(), ItemStack.EMPTY);
						return ItemStack.EMPTY;
					} else {
						ItemStack cursor = player.inventory.getItemStack();
						if (!cursor.isEmpty()) {
							if (slot.isItemValid(cursor)) {
								ItemStack copy = cursor.copy();
								copy.setCount(1);
								slot.putStack(copy);
								te.setOutputPrototype(slot.getSlotIndex(), copy);
							}
						}
						return cursor;
					}
				}
			}
		}
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return player == this.player;
	}

}
