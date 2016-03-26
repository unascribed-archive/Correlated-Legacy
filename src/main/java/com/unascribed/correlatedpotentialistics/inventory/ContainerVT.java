package com.unascribed.correlatedpotentialistics.inventory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.helper.ItemStacks;
import com.unascribed.correlatedpotentialistics.tile.TileEntityVT;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerVT extends Container {
	private TileEntityVT vt;
	private EntityPlayer player;
	private int scrollOffset;
	private String searchQuery = "";
	public int rows;
	
	public class SlotVirtual extends Slot {
		private ItemStack stack;
		private int count;
		public SlotVirtual(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}
		
		@Override
		public ItemStack getStack() {
			ItemStack stack = this.stack;
			sanitizeNBT(stack);
			return stack;
		}

		@Override
		public void putStack(ItemStack stack) {
			if (vt.hasWorldObj() && vt.getWorld().isRemote) {
				this.stack = stack;
				if (stack != null) {
					count = ItemStacks.getInteger(stack, "CorrelatedPotentialisticsExtendedStackSize").or(stack.stackSize);
					sanitizeNBT(stack);
					stack.stackSize = 1;
				} else {
					count = 0;
				}
			} else {
				if (stack != null) {
					CoPo.log.warn("putStack was called on a virtual slot", new RuntimeException()
						.fillInStackTrace());
					addItemToNetwork(stack);
				}
			}
		}

		@Override
		public void onSlotChanged() {
		}
		
		@Override
		public int getSlotStackLimit() {
			return Integer.MAX_VALUE;
		}

		@Override
		public ItemStack decrStackSize(int amount) {
			return null;
		}

		@Override
		public boolean isHere(IInventory inv, int slotIn) {
			return false;
		}

		public int getCount() {
			return count;
		}
		
		public void setStack(ItemStack stack) {
			this.stack = stack;
		}
		
		private void sanitizeNBT(ItemStack stack) {
			if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("CorrelatedPotentialisticsHadTag")) {
				boolean hadTag = ItemStacks.getBoolean(stack, "CorrelatedPotentialisticsHadTag").or(false);
				if (stack.hasTagCompound()) {
					stack.getTagCompound().removeTag("CorrelatedPotentialisticsExtendedStackSize");
					stack.getTagCompound().removeTag("CorrelatedPotentialisticsHadTag");
					if (!hadTag) {
						stack.setTagCompound(null);
					}
				}
			}
		}

	}

	public ContainerVT(IInventory playerInventory, EntityPlayer player, TileEntityVT vt) {
		this.player = player;
		this.vt = vt;
		int y = 37;
		
		for (int i = 0; i < 6; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new SlotVirtual(null, j + i * 9, 8 + j * 18, 18 + i * 18));
			}
		}
		updateSlots();
		
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 103 + i * 18 + y));
			}
		}

		for (int i = 0; i < 9; ++i) {
			addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 161 + y));
		}
	}
	
	public void updateSlots() {
		boolean remote = vt.hasWorldObj() && vt.getWorld().isRemote;
		List<ItemStack> typesAll = vt.getController().getTypes();
		if (!searchQuery.isEmpty()) {
			Iterator<ItemStack> itr = typesAll.iterator();
			while (itr.hasNext()) {
				ItemStack is = itr.next();
				if (!is.getDisplayName().toLowerCase().contains(searchQuery)) {
					itr.remove();
				}
			}
		}
		List<ItemStack> types = Lists.newArrayList();
		outer: for (ItemStack is : typesAll) {
			for (ItemStack existing : types) {
				if (ItemStack.areItemsEqual(is, existing) && ItemStack.areItemStackTagsEqual(is, existing)) {
					existing.stackSize += is.stackSize;
					continue outer;
				}
			}
			types.add(is);
		}
		Collections.sort(types, (a, b) -> Ints.compare(b.stackSize, a.stackSize));
		int idx = scrollOffset*9;
		for (Slot slot : inventorySlots) {
			if (slot instanceof SlotVirtual) {
				SlotVirtual sv = (SlotVirtual)slot;
				if (!remote) {
					if (idx < types.size()) {
						ItemStack stack = types.get(idx);
						boolean hadTag = stack.hasTagCompound();
						ItemStacks.ensureHasTag(stack).getTagCompound().setInteger("CorrelatedPotentialisticsExtendedStackSize", stack.stackSize);
						ItemStacks.ensureHasTag(stack).getTagCompound().setBoolean("CorrelatedPotentialisticsHadTag", hadTag);
						sv.setStack(stack);
					} else {
						sv.setStack(null);
					}
				}
			}
			idx++;
		}
		rows = (int)Math.ceil(types.size()/9f);
		for (ICrafting crafter : crafters) {
			crafter.sendProgressBarUpdate(this, 0, rows);
		}
	}
	
	@Override
	public void updateProgressBar(int id, int data) {
		if (id == 0) {
			rows = data;
		}
	}
	
	@Override
	public boolean enchantItem(EntityPlayer playerIn, int id) {
		scrollOffset = id;
		updateSlots();
		return true;
	}
	
	public ItemStack addItemToNetwork(ItemStack stack) {
		if (player.worldObj.isRemote) return null;
		ItemStack is = vt.getController().addItemToNetwork(stack);
		updateSlots();
		return is;
	}
	
	public ItemStack removeItemsFromNetwork(ItemStack prototype, int amount) {
		if (player.worldObj.isRemote) return null;
		ItemStack is = vt.getController().removeItemsFromNetwork(prototype, amount);
		updateSlots();
		return is;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn == player && vt.hasController() && vt.getController().getEnergyStored(null) > 0;
	}
	
	@Override
	public void onCraftGuiOpened(ICrafting listener) {
		super.onCraftGuiOpened(listener);
		listener.sendProgressBarUpdate(this, 0, rows);
	}
	
	@Override
	public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
		Slot slot = slotId >= 0 ? getSlot(slotId) : null;
		if (slot instanceof SlotVirtual) {
			if (!player.worldObj.isRemote) {
				if (player.inventory.getItemStack() != null) {
					if (mode == 0) {
						if (clickedButton == 0) {
							addItemToNetwork(player.inventory.getItemStack());
						} else if (clickedButton == 1) {
							addItemToNetwork(player.inventory.getItemStack().splitStack(1));
						}
					}
				} else if (slot.getHasStack()) {
					if (mode == 0) {
						if (clickedButton == 0) {
							player.inventory.setItemStack(removeItemsFromNetwork(slot.getStack(), Math.min(64, slot.getStack().getMaxStackSize())));
						} else if (clickedButton == 1) {
							player.inventory.setItemStack(removeItemsFromNetwork(slot.getStack(), 1));
						}
					} else if (mode == 1) {
						ItemStack is = null;
						if (clickedButton == 0) {
							is = removeItemsFromNetwork(slot.getStack(), Math.min(64, slot.getStack().getMaxStackSize()));
						} else if (clickedButton == 1) {
							is = removeItemsFromNetwork(slot.getStack(), 1);
						}
						if (is != null) {
							if (!player.inventory.addItemStackToInventory(is)) {
								addItemToNetwork(is);
							}
							detectAndSendChanges();
						}
					} else if (mode == 4) {
						ItemStack is = null;
						if (clickedButton == 0) {
							is = removeItemsFromNetwork(slot.getStack(), 1);
						} else if (clickedButton == 1) {
							is = removeItemsFromNetwork(slot.getStack(), Math.min(64, slot.getStack().getMaxStackSize()));
						}
						detectAndSendChanges();
						player.dropPlayerItemWithRandomChoice(is, false);
					}
				}
			}
			if (player.inventory.getItemStack() != null && player.inventory.getItemStack().stackSize <= 0) {
				player.inventory.setItemStack(null);
			}
			return player.inventory.getItemStack();
		} else {
			if (mode == 1) {
				// shift click
				if (!player.worldObj.isRemote) {
					ItemStack stack = getSlot(slotId).getStack();
					if (stack != null) {
						ItemStack is = addItemToNetwork(stack);
						getSlot(slotId).putStack(is);
						return is;
					}
				}
				return getSlot(slotId).getStack();
			}
			return super.slotClick(slotId, clickedButton, mode, player);
		}
	}

	public void updateSearchQuery(String query) {
		searchQuery = query.toLowerCase();
		updateSlots();
	}

}
