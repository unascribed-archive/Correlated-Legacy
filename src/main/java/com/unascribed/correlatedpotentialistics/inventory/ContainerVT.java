package com.unascribed.correlatedpotentialistics.inventory;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.common.primitives.Ints;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.primitives.Booleans;
import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.network.SetSearchQueryMessage;
import com.unascribed.correlatedpotentialistics.network.SetSlotSizeMessage;
import com.unascribed.correlatedpotentialistics.tile.TileEntityVT;
import com.unascribed.correlatedpotentialistics.tile.TileEntityVT.UserPreferences;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

// Yes, this class is a huge hardcoded mess and I'm sorry.
public class ContainerVT extends Container {
	public enum SortMode {
		QUANTITY((a, b) -> {
			int quantityComp = Ints.compare(a.stackSize, b.stackSize);
			if (quantityComp != 0) return quantityComp;
			return Collator.getInstance().compare(a.getDisplayName(), b.getDisplayName());
		}),
		MOD_MINECRAFT_FIRST((a, b) -> {
			String modA = getModId(a);
			String modB = getModId(b);
			boolean aMinecraft = "minecraft".equals(modA);
			boolean bMinecraft = "minecraft".equals(modB);
			if (aMinecraft || bMinecraft && aMinecraft != bMinecraft) return Booleans.compare(aMinecraft, bMinecraft);
			int modComp = Collator.getInstance().compare(modA, modB);
			if (modComp != 0) return modComp;
			return Collator.getInstance().compare(a.getDisplayName(), b.getDisplayName());
		}),
		MOD((a, b) -> {
			int modComp = Collator.getInstance().compare(getModId(a), getModId(b));
			if (modComp != 0) return modComp;
			return Collator.getInstance().compare(a.getDisplayName(), b.getDisplayName());
		}),
		NAME((a, b) -> Collator.getInstance().compare(a.getDisplayName(), b.getDisplayName()));
		public final Comparator<ItemStack> comparator;
		public final String lowerName = name().toLowerCase(Locale.ROOT);
		private SortMode(Comparator<ItemStack> comparator) {
			this.comparator = comparator;
		}

		private static String getModId(ItemStack is) {
			return Item.itemRegistry.getNameForObject(is.getItem()).getResourceDomain();
		}
	}
	public enum CraftingTarget {
		NETWORK,
		INVENTORY;
		public final String lowerName = name().toLowerCase(Locale.ROOT);
	}
	public enum CraftingAmount {
		ONE(s -> 1),
		STACK(s -> s.getMaxStackSize()/Math.max(1, s.stackSize)),
		MAX(s -> 6400);
		/*
		 * The above is 6400 instead of MAX_VALUE, as some mods add infinite
		 * loop recipes like two ingots of the same type -> two ingots of the
		 * same type. If you were to set the target to the network and the
		 * amount to max, and attempt to craft such an infinite loop operation,
		 * it would run 2,147,483,647 crafting operations and freeze the server.
		 * 6,400 is much more reasonable, covers the majority of legitimate
		 * use cases, and is unlikely to cause crashes in such an infinite loop
		 * case.
		 */
		public final String lowerName = name().toLowerCase(Locale.ROOT);
		public final Function<ItemStack, Integer> amountToCraft;
		private CraftingAmount(Function<ItemStack, Integer> amountToCraft) {
			this.amountToCraft = amountToCraft;
		}
	}

	private TileEntityVT vt;
	private EntityPlayer player;
	private int scrollOffset;
	private String searchQuery = "";
	public int rows;
	public SortMode sortMode = SortMode.QUANTITY;
	public boolean sortAscending = false;
	public CraftingAmount craftingAmount = CraftingAmount.ONE;
	public CraftingTarget craftingTarget = CraftingTarget.INVENTORY;
	private int lastChangeId;
	public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
	public InventoryCraftResult craftResult = new InventoryCraftResult();

	public class SlotVirtual extends Slot {
		private ItemStack stack;
		private int count;
		public SlotVirtual(int index, int xPosition, int yPosition) {
			super(null, index, xPosition, yPosition);
		}

		@Override
		public ItemStack getStack() {
			ItemStack stack = this.stack;
			return stack;
		}

		@Override
		public void putStack(ItemStack stack) {
			if (vt.hasWorldObj() && vt.getWorld().isRemote) {
				if (stack != null) {
					// prevent vanilla from corrupting our stack size
					if (ItemStack.areItemsEqual(stack, this.stack) && ItemStack.areItemStackTagsEqual(stack, this.stack)) {
						if ((stack.stackSize <= 127 && stack.stackSize >= -128)
								&& (count < -128 || count > 127)) {
							int diff = Math.abs(stack.stackSize-count);
							if (diff > stack.getMaxStackSize()) {
								return;
							}
						}
					}
					this.stack = stack.copy();
					count = stack.stackSize;
					// prevent vanilla from drawing the stack size
					this.stack.stackSize = 1;
				} else {
					this.stack = null;
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
			ItemStack nw = stack.copy();
			nw.stackSize = 0;
			return nw;
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

	}

	public ContainerVT(IInventory playerInventory, EntityPlayer player, TileEntityVT vt) {
		this.player = player;
		this.vt = vt;
		int x = 69;
		int y = 37;

		if (!player.worldObj.isRemote) {
			UserPreferences prefs = vt.getPreferences(player);
			sortMode = prefs.sortMode;
			sortAscending = prefs.sortAscending;
			searchQuery = prefs.lastSearchQuery;
			craftingTarget = prefs.craftingTarget;
		}

		addSlotToContainer(new Slot(vt, 0, 25, 161));
		
		for (int i = 0; i < 6; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new SlotVirtual(j + i * 9, x + j * 18, 18 + i * 18));
			}
		}
		updateSlots();

		addSlotToContainer(new SlotCrafting(player, craftMatrix, craftResult, 0, 26, 104));

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				this.addSlotToContainer(new Slot(craftMatrix, j + i * 3, 7 + j * 18, 18 + i * 18));
			}
		}

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, x + j * 18, 103 + i * 18 + y));
			}
		}

		for (int i = 0; i < 9; ++i) {
			addSlotToContainer(new Slot(playerInventory, i, x + i * 18, 161 + y));
		}

	}

	public void updateSlots() {
		if (vt.hasWorldObj() && vt.getWorld().isRemote) return;
		lastChangeId = vt.getController().changeId;
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
		if (sortAscending) {
			Collections.sort(types, sortMode.comparator);
		} else {
			Collections.sort(types, (a, b) -> sortMode.comparator.compare(b, a));
		}
		int idx = scrollOffset*9;
		for (Slot slot : inventorySlots) {
			if (slot instanceof SlotVirtual) {
				SlotVirtual sv = (SlotVirtual)slot;
				if (idx < types.size()) {
					sv.setStack(types.get(idx));
				} else {
					sv.setStack(null);
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
	public boolean enchantItem(EntityPlayer playerIn, int id) {
		switch (id) {
			case -1:
				sortAscending = true;
				break;
			case -2:
				sortAscending = false;
				break;

			case -3:
				sortMode = SortMode.QUANTITY;
				break;
			case -4:
				sortMode = SortMode.MOD_MINECRAFT_FIRST;
				break;
			case -5:
				sortMode = SortMode.MOD;
				break;
			case -6:
				sortMode = SortMode.NAME;
				break;

			case -10:
				craftingAmount = CraftingAmount.ONE;
				break;
			case -11:
				craftingAmount = CraftingAmount.STACK;
				break;
			case -12:
				craftingAmount = CraftingAmount.MAX;
				break;

			case -20:
				craftingTarget = CraftingTarget.INVENTORY;
				break;
			case -21:
				craftingTarget = CraftingTarget.NETWORK;
				break;

			case -128:
				for (int i = 0; i < 9; i++) {
					ItemStack is = craftMatrix.getStackInSlot(i);
					if (is == null) continue;
					craftMatrix.setInventorySlotContents(i, addItemToNetwork(is));
				}
				detectAndSendChanges();
				break;

			default:
				scrollOffset = id;
				break;
		}
		if (id > -10) {
			updateSlots();
		}
		return true;
	}

	public ItemStack addItemToNetwork(ItemStack stack) {
		if (player.worldObj.isRemote) return null;
		ItemStack is = vt.getController().addItemToNetwork(stack);
		return is;
	}

	public ItemStack removeItemsFromNetwork(ItemStack prototype, int amount) {
		if (player.worldObj.isRemote) return null;
		ItemStack is = vt.getController().removeItemsFromNetwork(prototype, amount, true);
		return is;
	}

	private List<Integer> oldStackSizes = Lists.newArrayList();

	@Override
	protected Slot addSlotToContainer(Slot slotIn) {
		oldStackSizes.add(0);
		return super.addSlotToContainer(slotIn);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (int i = 0; i < this.inventorySlots.size(); i++) {
			ItemStack stack = inventorySlots.get(i).getStack();
			int cur = stack == null ? 0 : stack.stackSize;
			int old = oldStackSizes.get(i);

			if (cur != old) {
				oldStackSizes.set(i, cur);

				// if it's out of range for the vanilla packet, we need to send our own
				if (cur > 127 || cur < -128) {
					for (ICrafting ic : crafters) {
						if (ic instanceof EntityPlayerMP) {
							EntityPlayerMP p = (EntityPlayerMP)ic;
							if (cur > 127) {
								CoPo.inst.network.sendTo(new SetSlotSizeMessage(windowId, i, cur), p);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			if (vt.hasController() && vt.getController().changeId != lastChangeId) {
				updateSlots();
			}
		}
		return player == this.player && vt.hasController() && vt.getController().getEnergyStored(null) > 0;
	}

	@Override
	public void onCraftGuiOpened(ICrafting listener) {
		super.onCraftGuiOpened(listener);
		listener.sendProgressBarUpdate(this, 0, rows);
		listener.sendProgressBarUpdate(this, 1, sortMode.ordinal());
		listener.sendProgressBarUpdate(this, 2, sortAscending ? 1 : 0);
		listener.sendProgressBarUpdate(this, 3, craftingTarget.ordinal());
		listener.sendProgressBarUpdate(this, 4, craftingAmount.ordinal());
		if (listener instanceof EntityPlayerMP) {
			CoPo.inst.network.sendTo(new SetSearchQueryMessage(windowId, searchQuery), (EntityPlayerMP)listener);
		}
	}

	@Override
	public void updateProgressBar(int id, int data) {
		if (id == 0) {
			rows = data;
		} else if (id == 1) {
			SortMode[] values = SortMode.values();
			sortMode = values[data%values.length];
		} else if (id == 2) {
			sortAscending = data != 0;
		} else if (id == 3) {
			CraftingTarget[] values = CraftingTarget.values();
			craftingTarget = values[data%values.length];
		} else if (id == 4) {
			CraftingAmount[] values = CraftingAmount.values();
			craftingAmount = values[data%values.length];
		}
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
				if (!player.worldObj.isRemote && slot != null) {
					ItemStack stack = slot.getStack();
					if (stack != null) {
						if (slot instanceof SlotCrafting) {
							ItemStack template = stack.copy();
							for (int i = 0; i < craftingAmount.amountToCraft.apply(template); i++) {
								stack = slot.getStack();
								if (stack == null || !ItemStack.areItemsEqual(template, stack)) break;
								boolean success;
								switch (craftingTarget) {
									case INVENTORY:
										success = player.inventory.addItemStackToInventory(stack);
										break;
									case NETWORK:
										int amountOrig = stack.stackSize;
										ItemStack res = addItemToNetwork(stack);
										success = (res == null || res.stackSize <= 0);
										if (!success && res != null) {
											removeItemsFromNetwork(stack, amountOrig-res.stackSize);
										}
										break;
									default:
										success = false;
								}
								if (!success) break;
								for (int j = 0; j < 9; j++) {
									ItemStack inSlot = craftMatrix.getStackInSlot(j);
									if (inSlot == null) continue;
									ItemStack is = removeItemsFromNetwork(inSlot, 1);
									if (is != null && is.stackSize > 0) {
										inSlot.stackSize += is.stackSize;
									}
								}
								slot.onPickupFromSlot(player, stack);
							}
							if (craftingAmount == CraftingAmount.MAX) {
								craftingAmount = CraftingAmount.ONE;
								for (ICrafting ic : crafters) {
									ic.sendProgressBarUpdate(this, 4, craftingAmount.ordinal());
								}
							}
							detectAndSendChanges();
							return null;
						} else {
							ItemStack is = addItemToNetwork(stack);
							getSlot(slotId).putStack(is);
							return is;
						}
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

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.worldObj.isRemote) {
			for (int i = 0; i < 9; ++i) {
				ItemStack itemstack = craftMatrix.removeStackFromSlot(i);

				if (itemstack != null) {
					player.dropPlayerItemWithRandomChoice(itemstack, false);
				}
			}
		}
		UserPreferences prefs = vt.getPreferences(player);
		prefs.sortMode = sortMode;
		prefs.sortAscending = sortAscending;
		prefs.craftingTarget = craftingTarget;
		prefs.lastSearchQuery = searchQuery;
		vt.markDirty();
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventory) {
		craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(craftMatrix, player.worldObj));
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slot) {
		return slot.inventory != craftResult && !(slot instanceof SlotVirtual) && super.canMergeSlot(stack, slot);
	}

}
