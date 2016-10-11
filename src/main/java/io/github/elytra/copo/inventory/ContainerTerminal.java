package io.github.elytra.copo.inventory;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.common.primitives.Ints;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.gui.GuiTerminal;
import io.github.elytra.copo.helper.Numbers;
import io.github.elytra.copo.item.ItemDrive;
import io.github.elytra.copo.network.AddStatusLineMessage;
import io.github.elytra.copo.network.SetSearchQueryClientMessage;
import io.github.elytra.copo.network.SetSlotSizeMessage;
import io.github.elytra.copo.storage.ITerminal;
import io.github.elytra.copo.storage.UserPreferences;
import io.github.elytra.copo.tile.TileEntityController;
import io.github.elytra.copo.tile.TileEntityTerminal;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.primitives.Booleans;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Yes, this class is a huge hardcoded mess and I'm sorry.
public class ContainerTerminal extends Container {
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
			return Item.REGISTRY.getNameForObject(is.getItem()).getResourceDomain();
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

	public ITerminal terminal;
	private World world;
	private EntityPlayer player;
	private int scrollOffset;
	private String searchQuery = "";
	public int rows;
	public SortMode sortMode = SortMode.QUANTITY;
	public boolean sortAscending = false;
	public CraftingAmount craftingAmount = CraftingAmount.ONE;
	public CraftingTarget craftingTarget = CraftingTarget.INVENTORY;
	public boolean jeiSyncEnabled = false;
	public boolean searchFocusedByDefault = false;
	private int lastChangeId;
	public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
	public InventoryCraftResult craftResult = new InventoryCraftResult();
	public int slotsAcross;
	public int slotsTall;
	public int startX;
	public int startY;
	public int playerInventoryOffsetX;
	public int playerInventoryOffsetY;
	public boolean hasCraftingMatrix = true;

	public class SlotVirtual extends Slot {
		private ItemStack stack;
		private int count;
		public SlotVirtual(int index, int xPosition, int yPosition) {
			super(null, index, xPosition, yPosition);
		}

		@Override
		public ItemStack getStack() {
			return stack;
		}

		@Override
		public void putStack(ItemStack stack) {
			if (world.isRemote) {
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
	
	public Slot floppySlot;

	public ContainerTerminal(IInventory playerInventory, EntityPlayer player, ITerminal terminal) {
		this.player = player;
		this.world = player.worldObj;
		this.terminal = terminal;
		initializeTerminalSize();
		int x = startX;
		int y = startY;

		if (!player.worldObj.isRemote) {
			UserPreferences prefs = terminal.getPreferences(player);
			sortMode = prefs.getSortMode();
			sortAscending = prefs.isSortAscending();
			searchQuery = prefs.getLastSearchQuery();
			craftingTarget = prefs.getCraftingTarget();
			searchFocusedByDefault = prefs.isSearchFocusedByDefault();
			jeiSyncEnabled = prefs.isJeiSyncEnabled();
		}

		if (terminal.supportsDumpSlot()) {
			addSlotToContainer(new Slot(terminal.getDumpSlotInventory(), 0, 25, 161));
		}
		if (terminal instanceof TileEntityTerminal) {
			TileEntityTerminal te = ((TileEntityTerminal)terminal);
			addSlotToContainer(floppySlot = new Slot(te, 1, -8000, -8000) {
				@Override
				public boolean canBeHovered() {
					return false;
				}
				@Override
				public boolean canTakeStack(EntityPlayer playerIn) {
					return false;
				}
			});
		}
		
		for (int i = 0; i < slotsTall; ++i) {
			for (int j = 0; j < slotsAcross; ++j) {
				addSlotToContainer(new SlotVirtual(j + i * slotsAcross, x + j * 18, (18 + i * 18) + startY));
			}
		}
		updateSlots();

		if (hasCraftingMatrix) {
			addSlotToContainer(new SlotCrafting(player, craftMatrix, craftResult, 0, 26, 104));
	
			for (int i = 0; i < 3; ++i) {
				for (int j = 0; j < 3; ++j) {
					this.addSlotToContainer(new Slot(craftMatrix, j + i * 3, 7 + j * 18, 18 + i * 18));
				}
			}
		}

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, (x + j * 18) + playerInventoryOffsetX, (103 + i * 18 + y) + playerInventoryOffsetY));
			}
		}

		for (int i = 0; i < 9; ++i) {
			addSlotToContainer(new Slot(playerInventory, i, (x + i * 18) + playerInventoryOffsetX, (161 + y) + playerInventoryOffsetY));
		}

	}

	/**
	 * If overridden, <b>do not call super</b>.
	 */
	protected void initializeTerminalSize() {
		slotsTall = 6;
		slotsAcross = 9;
		startX = 69;
		startY = 0;
		playerInventoryOffsetX = 0;
		playerInventoryOffsetY = 37;
		hasCraftingMatrix = true;
	}

	public void updateSlots() {
		if (world.isRemote) return;
		lastChangeId = terminal.getStorage().getChangeId();
		List<ItemStack> typesAll = terminal.getStorage().getTypes();
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
		if (scrollOffset < 0) {
			scrollOffset = 0;
		}
		int idx = scrollOffset*slotsAcross;
		for (Slot slot : inventorySlots) {
			if (slot instanceof SlotVirtual) {
				SlotVirtual sv = (SlotVirtual)slot;
				if (idx < types.size()) {
					sv.setStack(types.get(idx));
				} else {
					sv.setStack(null);
				}
				idx++;
			}
		}
		rows = (int)Math.ceil(types.size()/(float)slotsAcross);
		for (IContainerListener crafter : listeners) {
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
			
			case -22:
				/*if (storage instanceof TileEntityController) {
					TileEntityController cont = ((TileEntityController)storage);
					cont.
				}*/
				break;
				
			case -23:
				if (terminal.getStorage() instanceof TileEntityController) {
					TileEntityController cont = ((TileEntityController)terminal.getStorage());
					addStatusLine("total: "+Numbers.humanReadableBytes(cont.getMaxMemory()/8));
					addStatusLine("used: "+Numbers.humanReadableBytes(cont.getTotalUsedMemory()/8));
					addStatusLine("free: "+Numbers.humanReadableBytes(cont.getBitsMemoryFree()/8));
					addStatusLine("");
					addStatusLine("network: "+Numbers.humanReadableBytes(cont.getUsedNetworkMemory()/8));
					addStatusLine("wireless: "+Numbers.humanReadableBytes(cont.getUsedWirelessMemory()/8));
					addStatusLine("types: "+Numbers.humanReadableBytes(cont.getUsedTypeMemory()/8));
					addStatusLine("");
				}
				break;

			case -24:
				searchFocusedByDefault = true;
				break;
			case -25:
				searchFocusedByDefault = false;
				break;
				
			case -26:
				jeiSyncEnabled = true;
				break;
			case -27:
				jeiSyncEnabled = false;
				break;
				
			/*
			 * -30 (inclusive) through -60 (inclusive) are for subclass use
			 */
				
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
		int oldBits = terminal.getStorage().getKilobitsStorageFree();
		int initialStackSize = stack.stackSize;
		ItemStack is = terminal.getStorage().addItemToNetwork(stack);
		int newBits = terminal.getStorage().getKilobitsStorageFree();
		int delta = oldBits-newBits;
		String amt = delta < 8 ? delta+" Kib" : (delta/8)+" KiB";
		if (is == null) {
			addStatusLine("Add "+initialStackSize+": OK, used "+amt);
		} else if (is.stackSize < initialStackSize) {
			addStatusLine("Add "+(initialStackSize-is.stackSize)+": OK, used "+amt);
		} else {
			int needed = ItemDrive.getNBTComplexity(stack.getTagCompound());
			needed += 8*8;
			needed += stack.stackSize;
			addStatusLine("Add: Insufficient disk space.");
			addStatusLine("Need about "+needed+", only "+newBits+" available");
		}
		return is;
	}

	private void addStatusLine(String line) {
		status.add(line);
		for (IContainerListener ic : listeners) {
			if (ic instanceof EntityPlayerMP) {
				EntityPlayerMP p = (EntityPlayerMP)ic;
				new AddStatusLineMessage(windowId, line).sendTo(p);
			}
		}
	}

	public ItemStack removeItemsFromNetwork(ItemStack prototype, int amount) {
		if (player.worldObj.isRemote) return null;
		int oldBits = terminal.getStorage().getKilobitsStorageFree();
		ItemStack is = terminal.getStorage().removeItemsFromNetwork(prototype, amount, true);
		int newBits = terminal.getStorage().getKilobitsStorageFree();
		int delta = newBits-oldBits;
		String amt = delta < 8 ? delta+" Kib" : (delta/8)+" KiB";
		if (is != null) {
			addStatusLine("Remove "+is.stackSize+": OK, freed "+amt);
		}
		return is;
	}

	private List<Integer> oldStackSizes = Lists.newArrayList();
	public List<String> status = Lists.newArrayList();

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
					for (IContainerListener ic : listeners) {
						if (ic instanceof EntityPlayerMP) {
							EntityPlayerMP p = (EntityPlayerMP)ic;
							if (cur > 127) {
								new SetSlotSizeMessage(windowId, i, cur).sendTo(p);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		if (!world.isRemote) {
			if (terminal.hasStorage() && terminal.getStorage().getChangeId() != lastChangeId) {
				updateSlots();
			}
		}
		return player == this.player && terminal.hasStorage() && terminal.getStorage().isPowered() && terminal.canContinueInteracting(player);
	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listener.sendProgressBarUpdate(this, 0, rows);
		listener.sendProgressBarUpdate(this, 1, sortMode.ordinal());
		listener.sendProgressBarUpdate(this, 2, sortAscending ? 1 : 0);
		listener.sendProgressBarUpdate(this, 3, craftingTarget.ordinal());
		listener.sendProgressBarUpdate(this, 4, craftingAmount.ordinal());
		listener.sendProgressBarUpdate(this, 5, searchFocusedByDefault ? 1 : 0);
		listener.sendProgressBarUpdate(this, 6, jeiSyncEnabled ? 1 : 0);
		if (listener instanceof EntityPlayerMP) {
			new SetSearchQueryClientMessage(windowId, searchQuery).sendTo((EntityPlayerMP)listener);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
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
		} else if (id == 5) {
			searchFocusedByDefault = data != 0;
			GuiScreen cur = Minecraft.getMinecraft().currentScreen;
			if (searchFocusedByDefault && cur instanceof GuiTerminal) {
				((GuiTerminal)cur).focusSearch();
			}
		} else if (id == 6) {
			jeiSyncEnabled = data != 0;
		}
	}

	@Override
	public ItemStack slotClick(int slotId, int clickedButton, ClickType clickTypeIn, EntityPlayer player) {
		Slot slot = slotId >= 0 ? getSlot(slotId) : null;
		if (slot instanceof SlotVirtual) {
			if (!player.worldObj.isRemote) {
				if (player.inventory.getItemStack() != null) {
					if (clickTypeIn == ClickType.PICKUP) {
						if (clickedButton == 0) {
							addItemToNetwork(player.inventory.getItemStack());
						} else if (clickedButton == 1) {
							addItemToNetwork(player.inventory.getItemStack().splitStack(1));
						}
					}
				} else if (slot.getHasStack()) {
					if (clickTypeIn == ClickType.PICKUP) {
						if (clickedButton == 0) {
							player.inventory.setItemStack(removeItemsFromNetwork(slot.getStack(), Math.min(64, slot.getStack().getMaxStackSize())));
						} else if (clickedButton == 1) {
							player.inventory.setItemStack(removeItemsFromNetwork(slot.getStack(), 1));
						}
					} else if (clickTypeIn == ClickType.QUICK_MOVE) {
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
					} else if (clickTypeIn == ClickType.THROW) {
						ItemStack is = null;
						if (clickedButton == 0) {
							is = removeItemsFromNetwork(slot.getStack(), 1);
						} else if (clickedButton == 1) {
							is = removeItemsFromNetwork(slot.getStack(), Math.min(64, slot.getStack().getMaxStackSize()));
						}
						detectAndSendChanges();
						player.dropItem(is, false);
					}
				}
			}
			if (player.inventory.getItemStack() != null && player.inventory.getItemStack().stackSize <= 0) {
				player.inventory.setItemStack(null);
			}
			return player.inventory.getItemStack();
		} else {
			if (clickTypeIn == ClickType.QUICK_MOVE) {
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
								for (IContainerListener ic : listeners) {
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
				if (slotId >= 0) {
					return getSlot(slotId).getStack();
				} else {
					return null;
				}
			}
			return super.slotClick(slotId, clickedButton, clickTypeIn, player);
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
					player.dropItem(itemstack, false);
				}
			}
		}
		UserPreferences prefs = terminal.getPreferences(player);
		prefs.setSortMode(sortMode);
		prefs.setSortAscending(sortAscending);
		prefs.setCraftingTarget(craftingTarget);
		prefs.setLastSearchQuery(searchQuery);
		prefs.setJeiSyncEnabled(jeiSyncEnabled);
		prefs.setSearchFocusedByDefault(searchFocusedByDefault);
		terminal.markUnderlyingStorageDirty();
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
