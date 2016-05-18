package com.unascribed.correlatedpotentialistics.inventory;

import java.util.List;

import com.unascribed.correlatedpotentialistics.item.ItemDrive;
import com.unascribed.correlatedpotentialistics.item.ItemDrive.PartitioningMode;
import com.unascribed.correlatedpotentialistics.item.ItemDrive.Priority;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerDrive extends Container {
	public class SlotStatic extends SlotFake {
		public SlotStatic(int index, int xPosition, int yPosition, ItemStack stack) {
			super(index, xPosition, yPosition);
			super.putStack(stack);
		}

		@Override
		public void putStack(ItemStack stack) {
			if (player.worldObj.isRemote) {
				super.putStack(stack);
			}
		}

		@Override
		public ItemStack decrStackSize(int amount) {
			return null;
		}

		@Override
		public void onSlotChanged() {}
	}
	public class SlotFake extends Slot {
		public SlotFake(int index, int xPosition, int yPosition) {
			super(null, index, xPosition, yPosition);
		}

		private ItemStack stack;

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
			if (stack == null) return null;
			ItemStack split = stack.splitStack(amount);
			if (stack.stackSize <= 0) {
				stack = null;
			}
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
			for (ItemStack s : prototypes) {
				if (ItemStack.areItemsEqual(s, stack) && ItemStack.areItemStackTagsEqual(s, stack)) {
					return false;
				}
			}
			return getItemDrive().getBitsFree(getDrive()) >= getItemDrive().getTypeAllocationBits(getDrive());
		}

		@Override
		public void onSlotChanged() {
			getItemDrive().markDirty(getDrive());
			player.inventory.setInventorySlotContents(driveSlotId, getDrive());
		}
	}

	private final int driveSlotId;
	public final Slot driveSlot;
	private final EntityPlayer player;
	private List<ItemStack> prototypes;
	public ContainerDrive(IInventory playerInventory, int driveSlotId, EntityPlayer player) {
		this.driveSlotId = driveSlotId;
		ItemStack drive = playerInventory.getStackInSlot(driveSlotId);
		this.player = player;
		driveSlot = new SlotStatic(-2000, -8000, -8000, drive);
		addSlotToContainer(driveSlot);

		for (int i = 0; i < 64; i++) {
			SlotFake slot = new SlotFake(i, (((i % 11)*18)+8)+(i > 54 ? 18 : 0), ((i/11)*18)+18);
			addSlotToContainer(slot);
		}
		updateSlots();

		int x = 26;
		int y = 37;
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(playerInventory, j + i * 9 + 8, x + j * 18, 103 + i * 18 + y));
			}
		}

		for (int i = 0; i < 9; ++i) {
			Slot slot;
			if (i == driveSlotId) {
				slot = new Slot(playerInventory, i, x + i * 18, 161 + y) {
					@Override
					public boolean canTakeStack(EntityPlayer playerIn) { return false; }
				};
			} else {
				slot = new Slot(playerInventory, i, x + i * 18, 161 + y);
			}
			addSlotToContainer(slot);
		}
	}

	public void updateSlots() {
		prototypes = getItemDrive().getPrototypes(getDrive());
		for (int i = 1; i < 65; i++) {
			Slot slot = inventorySlots.get(i);
			if (slot instanceof SlotFake) {
				if (slot.getSlotIndex() < prototypes.size()) {
					ItemStack stack = prototypes.get(slot.getSlotIndex());
					stack.stackSize = 1;
					slot.putStack(stack);
				} else {
					slot.putStack(null);
				}
			}
		}
	}

	@Override
	public boolean enchantItem(EntityPlayer playerIn, int id) {
		if (id == 0 || id == 1) {
			Priority[] values = Priority.values();
			Priority cur = getItemDrive().getPriority(getDrive());
			int idx = (cur.ordinal()+(id == 0 ? -1 : 1))%values.length;
			if (idx < 0) {
				idx = values.length+idx;
			}
			Priority nxt = values[idx];
			getItemDrive().setPriority(getDrive(), nxt);
		} else if (id == 2) {
			PartitioningMode[] values = PartitioningMode.values();
			PartitioningMode cur = getItemDrive().getPartitioningMode(getDrive());
			int idx = (cur.ordinal()+1)%values.length;
			PartitioningMode nxt = values[idx];
			getItemDrive().setPartitioningMode(getDrive(), nxt);
		}
		player.inventory.setInventorySlotContents(driveSlotId, getDrive());
		return true;
	}

	@Override
	public void updateProgressBar(int id, int data) {

	}

	@Override
	public void addListener(ICrafting listener) {
		super.addListener(listener);
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if (slotId >= 1) {
			Slot slot = getSlot(slotId);
			if (slot instanceof SlotFake) {
				if (clickTypeIn == ClickType.PICKUP) {
					if (slot.getHasStack()) {
						int stored = getItemDrive().getAmountStored(getDrive(), slot.getStack());
						if (stored <= 0) {
							getItemDrive().deallocateType(getDrive(), slot.getStack());
							updateSlots();
							player.inventory.setInventorySlotContents(driveSlotId, getDrive());
						}
						return null;
					} else {
						ItemStack cursor = player.inventory.getItemStack();
						if (getItemDrive().getPartitioningMode(getDrive()) == PartitioningMode.NONE) return cursor;
						if (cursor != null) {
							if (slot.isItemValid(cursor)) {
								getItemDrive().allocateType(getDrive(), cursor, 0);
								updateSlots();
								player.inventory.setInventorySlotContents(driveSlotId, getDrive());
							}
						}
						return cursor;
					}
				}
			} else if (clickTypeIn == ClickType.QUICK_MOVE) {
				ItemStack stack = slot.getStack();
				if (getItemDrive().getPartitioningMode(getDrive()) == PartitioningMode.NONE) return stack;
				if (getSlot(1).isItemValid(stack)) {
					getItemDrive().allocateType(getDrive(), stack, 0);
					updateSlots();
					player.inventory.setInventorySlotContents(driveSlotId, getDrive());
				}
				return stack;
			}
		}
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	public ItemStack getDrive() {
		return driveSlot.getStack();
	}

	public ItemDrive getItemDrive() {
		return (ItemDrive)getDrive().getItem();
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn == player;
	}

}
