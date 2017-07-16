package com.elytradev.correlated.inventory;

import java.util.List;

import com.elytradev.correlated.item.ItemDrive;
import com.elytradev.correlated.item.ItemDrive.PartitioningMode;
import com.elytradev.correlated.item.ItemDrive.Priority;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerDrive extends Container {
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
		public boolean isEnabled() {
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
			return getItemDrive().getKilobitsFree(getDrive()) >= getItemDrive().getTypeAllocationKilobits(getDrive(), stack.serializeNBT());
		}

		@Override
		public void onSlotChanged() {
			getItemDrive().markDirty(getDrive());
		}
	}

	private final EntityPlayer player;
	private List<ItemStack> prototypes;
	private ContainerTerminal ct;
	
	private int oldX;
	private int oldY;
	
	public ContainerDrive(ContainerTerminal ct, IInventory playerInventory, EntityPlayer player) {
		this.ct = ct;
		this.player = player;

		oldX = ct.maintenanceSlot.xPos;
		oldY = ct.maintenanceSlot.yPos;
		
		ct.maintenanceSlot.xPos = -2000;
		ct.maintenanceSlot.yPos = -2000;
		
		addSlotToContainer(ct.maintenanceSlot);
		
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
			addSlotToContainer(new Slot(playerInventory, i, x + i * 18, 161 + y));
		}
	}

	public void updateSlots() {
		prototypes = getItemDrive().getPartitioningMode(getDrive()) == PartitioningMode.BLACKLIST ? getItemDrive().getBlacklistedTypes(getDrive()) : getItemDrive().getPrototypes(getDrive());
		for (int i = 1; i < 65; i++) {
			Slot slot = inventorySlots.get(i);
			if (slot instanceof SlotFake) {
				if (slot.getSlotIndex() < prototypes.size()) {
					ItemStack stack = prototypes.get(slot.getSlotIndex());
					stack.setCount(1);
					slot.putStack(stack);
				} else {
					slot.putStack(ItemStack.EMPTY);
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
			updateSlots();
		} else if (id == 3) {
			ct.maintenanceSlot.xPos = oldX;
			ct.maintenanceSlot.yPos = oldY;
			player.openContainer = ct;
		}
		return true;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		ct.maintenanceSlot.xPos = oldX;
		ct.maintenanceSlot.yPos = oldY;
	}
	
	@Override
	public void updateProgressBar(int id, int data) {

	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if (clickTypeIn == ClickType.QUICK_CRAFT) return player.inventory.getItemStack();
		if (slotId >= 1) {
			Slot slot = getSlot(slotId);
			if (slot instanceof SlotFake) {
				if (clickTypeIn == ClickType.PICKUP) {
					if (slot.getHasStack()) {
						int stored = getItemDrive().getAmountStored(getDrive(), slot.getStack());
						if (stored <= 0) {
							getItemDrive().deallocateType(getDrive(), slot.getStack());
							updateSlots();
						}
						return ItemStack.EMPTY;
					} else {
						ItemStack cursor = player.inventory.getItemStack();
						if (getItemDrive().getPartitioningMode(getDrive()) == PartitioningMode.NONE) return cursor;
						if (cursor != null) {
							if (slot.isItemValid(cursor)) {
								if (cursor != getDrive()) {
									if (getItemDrive().getPartitioningMode(getDrive()) == PartitioningMode.BLACKLIST) {
										int stored = getItemDrive().getAmountStored(getDrive(), cursor);
										if (stored <= 0) {
											getItemDrive().blacklistType(getDrive(), cursor);
											updateSlots();
										}
									} else {
										getItemDrive().allocateType(getDrive(), cursor, 0);
										updateSlots();
									}
								}
							}
						}
						return cursor;
					}
				}
			} else if (clickTypeIn == ClickType.QUICK_MOVE) {
				ItemStack stack = slot.getStack();
				if (getItemDrive().getPartitioningMode(getDrive()) == PartitioningMode.NONE) return stack;
				if (getSlot(1).isItemValid(stack)) {
					if (stack != getDrive()) {
						if (getItemDrive().getPartitioningMode(getDrive()) == PartitioningMode.BLACKLIST) {
							int stored = getItemDrive().getAmountStored(getDrive(), stack);
							if (stored <= 0) {
								getItemDrive().blacklistType(getDrive(), stack);
								updateSlots();
							}
						} else {
							getItemDrive().allocateType(getDrive(), stack, 0);
							updateSlots();
						}
					}
				}
				return stack;
			}
		}
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	public ItemStack getDrive() {
		return ct.maintenanceSlot.getStack();
	}

	public ItemDrive getItemDrive() {
		return (ItemDrive)getDrive().getItem();
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn == player;
	}

}
