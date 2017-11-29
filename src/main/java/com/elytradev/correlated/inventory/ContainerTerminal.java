package com.elytradev.correlated.inventory;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.client.gui.GuiTerminal;
import com.elytradev.correlated.helper.Numbers;
import com.elytradev.correlated.item.ItemDrive;
import com.elytradev.correlated.network.inventory.AddStatusLineMessage;
import com.elytradev.correlated.network.inventory.SetCraftingGhostClientMessage;
import com.elytradev.correlated.network.inventory.SetSearchQueryClientMessage;
import com.elytradev.correlated.network.inventory.UpdateNetworkContentsMessage;
import com.elytradev.correlated.network.wireless.SignalStrengthMessage;
import com.elytradev.correlated.storage.ITerminal;
import com.elytradev.correlated.storage.InsertResult;
import com.elytradev.correlated.storage.NetworkType;
import com.elytradev.correlated.storage.Prototype;
import com.elytradev.correlated.storage.InsertResult.Result;
import com.elytradev.correlated.storage.UserPreferences;
import com.elytradev.correlated.tile.TileEntityController;
import com.elytradev.correlated.tile.TileEntityTerminal;
import com.elytradev.correlated.wifi.IWirelessClient;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerTerminal extends Container implements IWirelessClient {
	public enum CraftingTarget {
		NETWORK,
		INVENTORY;
		public final String lowerName = name().toLowerCase(Locale.ROOT);
	}
	public enum CraftingAmount {
		ONE(s -> 1),
		STACK(s -> s.getMaxStackSize()/Math.max(1, s.getCount())),
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
	private String searchQuery = "";
	public SortMode sortMode = SortMode.QUANTITY;
	public boolean sortAscending = false;
	public CraftingAmount craftingAmount = CraftingAmount.ONE;
	public CraftingTarget craftingTarget = CraftingTarget.INVENTORY;
	public boolean jeiSyncEnabled = false;
	public boolean searchFocusedByDefault = false;
	private int lastChangeId;
	public int slotsAcross;
	public int slotsTall;
	public int startX;
	public int startY;
	public int playerInventoryOffsetX;
	public int playerInventoryOffsetY;
	public boolean hasCraftingMatrix = true;
	private int lastSignal = -1;
	public boolean isDumping = false;
	public boolean isFilling = false;
	private String lastApn;
	
	public Slot maintenanceSlot;
	public Slot floppySlot;
	
	private Set<NetworkType> lastNetworkContents = Collections.emptySet();
	private Set<Prototype> lastNetworkPrototypeContents = Collections.emptySet();
	
	private List<? extends List<ItemStack>> craftingGhost = NonNullList.withSize(9, NonNullList.from(ItemStack.EMPTY));

	public ContainerTerminal(IInventory playerInventory, EntityPlayer player, ITerminal terminal) {
		this.player = player;
		this.world = player.world;
		this.terminal = terminal;
		initializeTerminalSize();
		int x = startX;
		int y = startY;

		if (!player.world.isRemote) {
			UserPreferences prefs = terminal.getPreferences(player);
			sortMode = prefs.getSortMode();
			sortAscending = prefs.isSortAscending();
			searchQuery = prefs.getLastSearchQuery();
			craftingTarget = prefs.getCraftingTarget();
			searchFocusedByDefault = prefs.isSearchFocusedByDefault();
			jeiSyncEnabled = prefs.isJeiSyncEnabled();
			craftingGhost = prefs.getCraftingGhost();
		}

		if (terminal.hasMaintenanceSlot()) {
			addSlotToContainer(maintenanceSlot = new Slot(new InventoryAdapter() {
				@Override
				protected ItemStack get() {
					return terminal.getMaintenanceSlotContent();
				}
				@Override
				protected void set(ItemStack stack) {
					terminal.setMaintenanceSlotContent(stack);
				}
				@Override
				public boolean isItemValidForSlot(int index, ItemStack stack) {
					return stack.getItem() instanceof ItemDrive;
				}
			}, 0, 16, 161));
		}
		if (terminal instanceof TileEntityTerminal) {
			TileEntityTerminal te = ((TileEntityTerminal)terminal);
			addSlotToContainer(floppySlot = new Slot(te, 1, -8000, -8000) {
				@Override
				public boolean isEnabled() {
					return false;
				}
				@Override
				public boolean canTakeStack(EntityPlayer playerIn) {
					return false;
				}
			});
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
	 * Perform an incremental client resync, sending only what changed.
	 */
	protected void resync() {
		if (world.isRemote) return;
		Set<NetworkType> out = Sets.newHashSet();
		if (!terminal.hasStorage()) {
			fullResync();
			return;
		}
		terminal.getStorage().getTypes(Sets.newHashSet(terminal.getStorage()), out);
		
		if (lastNetworkContents.equals(out)) return;
		
		Set<Prototype> protoOut = Sets.newHashSet();
		out.stream().map(NetworkType::getPrototype).forEach(protoOut::add);
		
		Set<NetworkType> addOrChange = Sets.newHashSet(out);
		Set<Prototype> remove = Sets.newHashSet(lastNetworkPrototypeContents);
		
		addOrChange.removeAll(lastNetworkContents);
		remove.removeAll(protoOut);
		
		UpdateNetworkContentsMessage msg = new UpdateNetworkContentsMessage(addOrChange, remove, false);
		for (IContainerListener ic : listeners) {
			if (ic instanceof EntityPlayerMP) {
				EntityPlayerMP p = (EntityPlayerMP)ic;
				msg.sendTo(p);
			}
		}
		
		lastNetworkContents = out;
		lastNetworkPrototypeContents = protoOut;
	}
	
	/**
	 * Perform a complete client resync, overwriting whatever data the client
	 * may already have.
	 */
	protected void fullResync() {
		if (world.isRemote) return;
		Set<NetworkType> out = Sets.newHashSet();
		if (terminal.hasStorage()) {
			terminal.getStorage().getTypes(Sets.newHashSet(terminal.getStorage()), out);
		}
		lastNetworkContents = out;
		UpdateNetworkContentsMessage msg = new UpdateNetworkContentsMessage(terminal.hasStorage() ? terminal.getStorage().getTypes() : Collections.emptyList(), Collections.emptyList(), true);
		for (IContainerListener ic : listeners) {
			if (ic instanceof EntityPlayerMP) {
				EntityPlayerMP p = (EntityPlayerMP)ic;
				msg.sendTo(p);
			}
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
			case -7:
				sortMode = SortMode.LAST_MODIFIED;
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
					addStatusLine(new TextComponentTranslation("correlated.shell.free.total", Numbers.humanReadableBits(cont.getMaxMemory())));
					addStatusLine(new TextComponentTranslation("correlated.shell.free.used", Numbers.humanReadableBits(cont.getTotalUsedMemory())));
					addStatusLine(new TextComponentTranslation("correlated.shell.free.free", Numbers.humanReadableBits(cont.getBitsMemoryFree())));
					addStatusLine(new TextComponentString(""));
					addStatusLine(new TextComponentTranslation("correlated.shell.free.network", Numbers.humanReadableBits(cont.getUsedNetworkMemory())));
					addStatusLine(new TextComponentTranslation("correlated.shell.free.wireless", Numbers.humanReadableBits(cont.getUsedWirelessMemory())));
					addStatusLine(new TextComponentTranslation("correlated.shell.free.type", Numbers.humanReadableBits(cont.getUsedTypeMemory())));
					addStatusLine(new TextComponentString(""));
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
				
			case -28:
				isDumping = true;
				isFilling = false;
				break;
			case -29:
				isDumping = false;
				isFilling = false;
				break;
				
			/*
			 * -30 (inclusive) through -60 (inclusive) are for subclass use
			 */
				
			case -61:
				isDumping = false;
				isFilling = true;
				break;
				
			case -62:
				player.openGui(Correlated.inst, 1, world, 0, 0, 0);
				break;
		}
		return true;
	}

	public InsertResult addItemToNetwork(ItemStack stack) {
		if (player.world.isRemote) return InsertResult.success(ItemStack.EMPTY);
		long startingMem = 0;
		long startingDiskFree = terminal.getStorage().getKilobitsStorageFree();
		if (terminal.getStorage() instanceof TileEntityController) {
			startingMem = ((TileEntityController)terminal.getStorage()).getUsedTypeMemory();
		}
		InsertResult ir = addItemToNetworkSilently(stack);
		if (!ir.wasSuccessful()) {
			addStatusLine(new TextComponentTranslation("msg.correlated.insertFailed", new TextComponentTranslation("msg.correlated.insertFailed."+ir.result.name().toLowerCase(Locale.ROOT))));
		} else {
			if (ir.result == Result.SUCCESS_VOIDED) {
				addStatusLine(new TextComponentTranslation("msg.correlated.insertSuccessVoid"));
			} else {
				long endingMem = 0;
				long endingDiskFree = terminal.getStorage().getKilobitsStorageFree();
				if (terminal.getStorage() instanceof TileEntityController) {
					endingMem = ((TileEntityController)terminal.getStorage()).getUsedTypeMemory();
				}
				long memChange = endingMem-startingMem;
				if (memChange != 0) {
					addStatusLine(new TextComponentTranslation("msg.correlated.insertSuccessMem", Numbers.humanReadableBits(memChange), Numbers.humanReadableBits((startingDiskFree-endingDiskFree)*1024)));
				} else {
					addStatusLine(new TextComponentTranslation("msg.correlated.insertSuccess", Numbers.humanReadableBits((startingDiskFree-endingDiskFree)*1024)));
				}
			}
		}
		return ir;
	}
	
	public InsertResult addItemToNetworkSilently(ItemStack stack) {
		if (player.world.isRemote) return InsertResult.success(ItemStack.EMPTY);
		return terminal.getStorage().addItemToNetwork(stack);
	}

	public void addStatusLine(ITextComponent line) {
		status.add(line);
		for (IContainerListener ic : listeners) {
			if (ic instanceof EntityPlayerMP) {
				EntityPlayerMP p = (EntityPlayerMP)ic;
				new AddStatusLineMessage(windowId, line).sendTo(p);
			}
		}
	}

	public ItemStack removeItemsFromNetwork(ItemStack prototype, int amount) {
		if (player.world.isRemote) return ItemStack.EMPTY;
		long startingMem = 0;
		long startingDiskFree = terminal.getStorage().getKilobitsStorageFree();
		if (terminal.getStorage() instanceof TileEntityController) {
			startingMem = ((TileEntityController)terminal.getStorage()).getUsedTypeMemory();
		}
		ItemStack res = removeItemsFromNetworkSilently(prototype, amount);
		long endingMem = 0;
		long endingDiskFree = terminal.getStorage().getKilobitsStorageFree();
		if (terminal.getStorage() instanceof TileEntityController) {
			endingMem = ((TileEntityController)terminal.getStorage()).getUsedTypeMemory();
		}
		long memChange = startingMem-endingMem;
		if (memChange != 0) {
			addStatusLine(new TextComponentTranslation("msg.correlated.removeSuccessMem", Numbers.humanReadableBits(memChange), Numbers.humanReadableBits((endingDiskFree-startingDiskFree)*1024)));
		} else {
			addStatusLine(new TextComponentTranslation("msg.correlated.removeSuccess", Numbers.humanReadableBits((endingDiskFree-startingDiskFree)*1024)));
		}
		return res;
	}
	
	public ItemStack removeItemsFromNetworkSilently(ItemStack prototype, int amount) {
		if (player.world.isRemote) return ItemStack.EMPTY;
		return terminal.getStorage().removeItemsFromNetwork(prototype, amount, true);
	}

	private List<Integer> oldStackSizes = Lists.newArrayList();
	public List<ITextComponent> status = Lists.newArrayList();

	@Override
	protected Slot addSlotToContainer(Slot slotIn) {
		oldStackSizes.add(0);
		return super.addSlotToContainer(slotIn);
	}

	@Override
	public void detectAndSendChanges() {
		if (terminal.hasMaintenanceSlot() && (isDumping || isFilling)) {
			if (terminal.getMaintenanceSlotContent().getItem() instanceof ItemDrive) {
				ItemDrive id = (ItemDrive)terminal.getMaintenanceSlotContent().getItem();
				List<NetworkType> prototypes = isFilling ? terminal.getStorage().getTypes() : id.getPrototypes(terminal.getMaintenanceSlotContent());
				for (int i = 0; i < 100; i++) {
					if (prototypes.isEmpty()) break;
					ItemStack prototype = prototypes.get(0).getStack();
					ItemStack split;
					if (isFilling) {
						split = removeItemsFromNetworkSilently(prototype, prototype.getMaxStackSize());
					} else {
						split = id.removeItems(terminal.getMaintenanceSlotContent(), prototype, prototype.getMaxStackSize());
					}
					if (split.isEmpty()) {
						prototypes.remove(0);
						continue;
					}
					if (isFilling) {
						id.addItem(terminal.getMaintenanceSlotContent(), split, false);
					} else {
						addItemToNetworkSilently(split);
					}
					if (!split.isEmpty()) {
						// no more room for this item in the target, skip it this tick
						prototypes.remove(0);
						if (isFilling) {
							addItemToNetworkSilently(split);
						} else {
							id.addItem(terminal.getMaintenanceSlotContent(), split, false);
						}
					}
				}
			}
		}
		
		if (!world.isRemote) {
			int changeId = terminal.hasStorage() ? terminal.getStorage().getChangeId() : 0;
			if (terminal.hasStorage() && changeId != lastChangeId) {
				resync();
				lastChangeId = changeId;
			}
			if (!Objects.equal(lastApn, terminal.getAPN())) {
				lastApn = terminal.getAPN();
				fullResync();
			}
		}
		
		super.detectAndSendChanges();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		boolean b = player == this.player && terminal.canContinueInteracting(player);
		if (b) {
			int signal = terminal.getSignalStrength();
			if (signal != lastSignal) {
				lastSignal = signal;
				new SignalStrengthMessage(signal).sendTo(player);
			}
		}
		return b;
	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listener.sendWindowProperty(this, 1, sortMode.ordinal());
		listener.sendWindowProperty(this, 2, sortAscending ? 1 : 0);
		listener.sendWindowProperty(this, 3, craftingTarget.ordinal());
		listener.sendWindowProperty(this, 4, craftingAmount.ordinal());
		listener.sendWindowProperty(this, 5, searchFocusedByDefault ? 1 : 0);
		listener.sendWindowProperty(this, 6, jeiSyncEnabled ? 1 : 0);
		if (listener instanceof EntityPlayerMP) {
			new SetSearchQueryClientMessage(windowId, searchQuery).sendTo((EntityPlayerMP)listener);
			new SetCraftingGhostClientMessage(windowId, craftingGhost).sendTo((EntityPlayerMP)listener);
			if (terminal.hasStorage()) {
				new UpdateNetworkContentsMessage(terminal.getStorage().getTypes(), Collections.emptyList(), true).sendTo((EntityPlayerMP)listener);
			} else {
				new UpdateNetworkContentsMessage(Collections.emptyList(), Collections.emptyList(), true).sendTo((EntityPlayerMP)listener);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int id, int data) {
		if (id == 1) {
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
		if (clickTypeIn == ClickType.QUICK_MOVE) {
			// shift click
			if (!player.world.isRemote && slot != null) {
				ItemStack stack = slot.getStack();
				if (!stack.isEmpty()) {
					ItemStack is = addItemToNetwork(stack).stack;
					getSlot(slotId).putStack(is);
					return is;
				}
			}
			if (slotId >= 0) {
				return getSlot(slotId).getStack();
			} else {
				return ItemStack.EMPTY;
			}
		}
		return super.slotClick(slotId, clickedButton, clickTypeIn, player);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		UserPreferences prefs = terminal.getPreferences(player);
		prefs.setSortMode(sortMode);
		prefs.setSortAscending(sortAscending);
		prefs.setCraftingTarget(craftingTarget);
		prefs.setLastSearchQuery(searchQuery);
		prefs.setJeiSyncEnabled(jeiSyncEnabled);
		prefs.setSearchFocusedByDefault(searchFocusedByDefault);
		prefs.setCraftingGhost(craftingGhost);
		terminal.markUnderlyingStorageDirty();
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slot) {
		return super.canMergeSlot(stack, slot);
	}

	@Override
	public void setAPNs(Set<String> apn) {
		if (apn.size() > 1) throw new IllegalArgumentException("Only supports 1 APN");
		terminal.setAPN(apn.isEmpty() ? null : apn.iterator().next());
	}

	@Override
	public Set<String> getAPNs() {
		return terminal.getAPN() == null ? Collections.emptySet() : Collections.singleton(terminal.getAPN());
	}
	
	@Override
	public BlockPos getPosition() {
		return terminal.getPosition();
	}
	
	@Override
	public double getX() {
		return terminal.getPosition().getX()+0.5;
	}
	
	@Override
	public double getY() {
		return terminal.getPosition().getY()+0.5;
	}
	
	@Override
	public double getZ() {
		return terminal.getPosition().getZ()+0.5;
	}

	public void updateSearchQuery(String query) {
		this.searchQuery = query;
	}

	public void setCraftingGhost(List<? extends List<ItemStack>> ghost) {
		this.craftingGhost = ghost;
	}

}

