package com.elytradev.correlated.client.gui;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.EnergyUnit;
import com.elytradev.correlated.client.IBMFontRenderer;
import com.elytradev.correlated.client.gui.shell.GuiTerminalShell;
import com.elytradev.correlated.helper.Numbers;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.inventory.ContainerTerminal;
import com.elytradev.correlated.inventory.ContainerTerminal.CraftingAmount;
import com.elytradev.correlated.inventory.ContainerTerminal.CraftingTarget;
import com.elytradev.correlated.inventory.SortMode;
import com.elytradev.correlated.network.inventory.CraftItemMessage;
import com.elytradev.correlated.network.inventory.InsertAllMessage;
import com.elytradev.correlated.network.inventory.SetCraftingGhostServerMessage;
import com.elytradev.correlated.network.inventory.SetSearchQueryServerMessage;
import com.elytradev.correlated.network.inventory.TerminalActionMessage;
import com.elytradev.correlated.network.inventory.TerminalActionMessage.TerminalAction;
import com.elytradev.correlated.proxy.ClientProxy;
import com.elytradev.correlated.storage.NetworkType;
import com.elytradev.correlated.storage.Prototype;
import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.math.IntMath;
import com.google.common.primitives.Ints;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.resources.I18n;

import com.elytradev.correlated.C28n;
import com.elytradev.correlated.CLog;
import com.elytradev.correlated.ColorType;

import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.creativetab.CreativeTabs;

public class GuiTerminal extends GuiContainer {
	
	public enum QueryType {
		BLANK(100, isEmpty(), (query, stack) -> true),
		
		MOD_NAME(10, startsWith("@"), (query, stack) -> {
			String domain = stack.getItem().getRegistryName().getResourceDomain();
			ModContainer mod = "minecraft".equals(domain) ?
					Loader.instance().getMinecraftModContainer() :
					Loader.instance().getIndexedModList().get(domain);
			if (mod == null) return false;
			String id = Strings.nullToEmpty(mod.getModId()).toLowerCase(Locale.ENGLISH);
			String name = Strings.nullToEmpty(mod.getName()).toLowerCase(Locale.ENGLISH);
			String idNoSpaces = CharMatcher.whitespace().replaceFrom(id, "");
			String nameNoSpaces = CharMatcher.whitespace().replaceFrom(name, "");
			if (id.contains(query)) return true;
			if (C28n.contains(name, query)) return true;
			if (idNoSpaces.contains(query)) return true;
			if (C28n.contains(nameNoSpaces, query)) return true;
			return false;
		}),
		TOOLTIP(10, startsWith("#"), (query, stack) -> {
			Minecraft mc = Minecraft.getMinecraft();
			List<String> tooltip = stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
			for (String s : tooltip) {
				if (C28n.contains(s.toLowerCase(), query)) return true;
			}
			return false;
		}),
		OREDICT(10, startsWith("$"), (query, stack) -> {
			int[] ids = OreDictionary.getOreIDs(stack);
			for (int id : ids) {
				String name = OreDictionary.getOreName(id);
				if (name.toLowerCase(Locale.ENGLISH).contains(query)) {
					return true;
				}
			}
			return false;
		}),
		CREATIVE_TAB(10, startsWith("%"), (query, stack) -> {
			for (CreativeTabs ct : stack.getItem().getCreativeTabs()) {
				if (ct != null) {
					String name = I18n.format(ct.getTranslatedTabLabel()).toLowerCase();
					if (C28n.contains(name, query)) return true;
				}
			}
			return false;
		}),
		COLORS(10, startsWith("^"), (query, stack) -> {
			return Correlated.inst.colorSearcher.test(query, stack);
		}),
		
		// have to do this to prevent a "cannot reference a field before it is defined" error
		UNION(20, contains("|"), QueryType::unionFilter),
		INTERSECTION(20, contains("&"), QueryType::intersectionFilter),
		
		NORMAL(0, alwaysTrue(), (query, stack) -> {
			if (C28n.contains(stack.getDisplayName().toLowerCase(), query)) return true;
			TOOLTIP.filter.test(query, stack);
			return false;
		}),
		;
		
		private static Function<String, String> alwaysTrue() {
			return (s) -> s;
		}
		private static Function<String, String> isEmpty() {
			return (s) -> {
				String trim = s.trim();
				if (trim.isEmpty()) {
					return trim;
				} else {
					return null;
				}
			};
		}
		private static Function<String, String> startsWith(String prefix) {
			return (s) -> s.startsWith(prefix) ? s.substring(prefix.length()) : null;
		}
		private static Function<String, List<String>> contains(String substring) {
			Splitter splitter = Splitter.on(substring);
			return (s) -> s.contains(substring) ? splitter.splitToList(s) : null;
		}
		private static Function<String, Matcher> matches(String regex) {
			Pattern p = Pattern.compile(regex);
			return (s) -> {
				Matcher m = p.matcher(s);
				if (m.matches()) {
					return m;
				} else {
					return null;
				}
			};
		}
		
		public static final ImmutableList<QueryType> VALUES_BY_PRIORITY;
		
		public final Function<String, Object> mangler;
		public final BiPredicate<Object, ItemStack> filter;
		public final int priority;
		<T> QueryType(int priority, Function<String, T> mangler, BiPredicate<T, ItemStack> filter) {
			this.priority = priority;
			this.mangler = (Function<String, Object>)mangler;
			this.filter = (BiPredicate<Object, ItemStack>)filter;
		}
		
		static {
			List<QueryType> li = Lists.newArrayList(values());
			// higher priorities come first
			li.sort((a, b) -> Ints.compare(b.priority, a.priority));
			VALUES_BY_PRIORITY = ImmutableList.copyOf(li);
		}
		
		private static boolean unionFilter(List<String> queries, ItemStack stack) {
			for (String s : queries) {
				for (QueryType qt : VALUES_BY_PRIORITY) {
					Object mangled = qt.mangler.apply(s);
					if (mangled == null) continue;
					if (qt.filter.test(mangled, stack)) return true;
				}
			}
			return false;
		}
		private static boolean intersectionFilter(List<String> queries, ItemStack stack) {
			boolean matches = true;
			for (String s : queries) {
				if (!matches) break;
				boolean anyFilterAccepted = false;
				for (QueryType qt : VALUES_BY_PRIORITY) {
					Object mangled = qt.mangler.apply(s);
					if (mangled == null) continue;
					anyFilterAccepted = true;
					if (!qt.filter.test(mangled, stack)) {
						matches = false;
						break;
					}
				}
				if (!anyFilterAccepted) {
					matches = false;
					break;
				}
			}
			return matches;
		}
	}
	
	private static final ResourceLocation background = new ResourceLocation("correlated", "textures/gui/container/terminal.png");
	private static final ResourceLocation ENERGY = new ResourceLocation("correlated", "textures/misc/energy.png");

	private static final NumberFormat GROUPED_INTEGER = ((Supplier<NumberFormat>)() -> {
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setGroupingUsed(true);
		return nf;
	}).get();
	
	private ContainerTerminal container;
	private GuiTextField searchField = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 85, 8);
	private String lastSearchQuery = "";
	private GuiButtonExt sortDirection;
	private GuiButtonExt sortMode;
	private GuiButtonExt craftingTarget;
	private GuiButtonExt craftingAmount;
	private GuiButtonExt clearGrid;
	private GuiButtonExt focusByDefault;
	private GuiButtonExt jeiSync;
	private GuiButtonExt dump;
	private GuiButtonExt partition;
	private GuiButtonExt preferredEnergySystem;
	
	private boolean isDrawingHoverTooltip = false;
	
	public int signalStrength = -1;
	private int rows;
	private boolean mouseInNetworkListing;
	private NetworkType hovered;
	
	private Map<Prototype, NetworkType> networkContents = Maps.newHashMap();
	private List<NetworkType> networkContentsView = Lists.newArrayList();
	
	private boolean dirty = false;
	private String lastJeiQuery;
	private QueryType queryType;
	
	private int scrollOffset = 0;
	
	private final NonNullList<NonNullList<ItemStack>> craftMatrix = NonNullList.withSize(9, NonNullList.from(ItemStack.EMPTY));
	private final NonNullList<ItemStack> craftMatrixDummyLast = NonNullList.withSize(9, ItemStack.EMPTY);
	private final InventoryCrafting craftMatrixResolved;
	
	private ItemStack craftMatrixResult = ItemStack.EMPTY;
	
	public GuiTerminal(ContainerTerminal container) {
		super(container);
		searchField.setEnableBackgroundDrawing(false);
		searchField.setTextColor(-1);
		searchField.setFocused(container.searchFocusedByDefault);
		this.container = container;
		xSize = 256;
		ySize = 222;
		lastJeiQuery = Correlated.inst.jeiQueryReader.get();
		
		craftMatrixResolved = new InventoryCrafting(container, 3, 3);
		
		MinecraftForge.EVENT_BUS.register(this);
	}

	protected boolean hasStatusLine() {
		return true;
	}
	
	protected boolean hasSearchAndSort() {
		return true;
	}
	
	protected ResourceLocation getBackground() {
		return background;
	}
	
	public void updateNetworkContents(List<NetworkType> addOrChange, List<Prototype> remove, boolean overwrite) {
		if (overwrite) {
			networkContents.clear();
		} else {
			for (Prototype rm : remove) {
				networkContents.remove(rm);
			}
		}
		for (NetworkType add : addOrChange) {
			Prototype p = add.getPrototype();
			networkContents.put(p, add);
		}
		dirty = true;
	}
	
	private void updateNetworkView() {
		networkContentsView.clear();
		networkContentsView.addAll(networkContents.values());
		String query = searchField.getText().toLowerCase();
		queryType = null;
		Object mangledQuery = null;
		for (QueryType qt : QueryType.VALUES_BY_PRIORITY) {
			mangledQuery = qt.mangler.apply(query);
			if (mangledQuery != null) {
				queryType = qt;
				break;
			}
		}
		if (queryType == null || mangledQuery == null) {
			CLog.warn("Failed to determine query type for {}", query);
			return;
		}
		ListIterator<NetworkType> iter = networkContentsView.listIterator();
		while (iter.hasNext()) {
			NetworkType type = iter.next();
			ItemStack stack = type.getStack();
			if (stack.isEmpty()) {
				iter.remove();
				continue;
			}
			if (queryType.filter.test(mangledQuery, stack)) continue;
			iter.remove();
		}
		Comparator<NetworkType> comparator = container.sortMode.comparator;
		Collections.sort(networkContentsView, container.sortAscending ? comparator : comparator.reversed());
		rows = IntMath.divide(networkContentsView.size(), container.slotsAcross, RoundingMode.UP);
		dirty = false;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1);
		if (dirty) {
			updateNetworkView();
		}
		if (container.status.isEmpty()) {
			if (Math.random() == 0.5) {
				container.status.add(new TextComponentTranslation("correlated.shell.readyEgg"));
			} else {
				container.status.add(signalStrength != -1 ? new TextComponentTranslation("correlated.shell.ready_wireless") : new TextComponentTranslation("correlated.shell.ready"));
			}
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
		mc.getTextureManager().bindTexture(getBackground());
		drawTexturedModalRect(0, 0, 0, 0, 256, 136);
		drawTexturedModalRect(61, 136, 61, 136, 176, 87);
		drawTexturedModalRect(237, 135, 237, 135, 19, 11);
		if (container.terminal.hasMaintenanceSlot()) {
			drawTexturedModalRect(8, 152, 8, 152, 44, 34);
			dump.enabled = !container.terminal.getMaintenanceSlotContent().isEmpty();
			partition.enabled = !container.terminal.getMaintenanceSlotContent().isEmpty();
		}
		GlStateManager.popMatrix();
	}
	
	protected String getTitle() {
		return C28n.format("gui.correlated.terminal");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(getTitle(), 8, 6, 0x404040);
		GlStateManager.color(1, 1, 1);
		
		int mouseXOfs = mouseX - (width - xSize) / 2;
		int mouseYOfs = mouseY - (height - ySize) / 2;
		
		hovered = null;
		
		if (container.hasCraftingMatrix) {
			if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1))
					&& mouseXOfs >= 6 && mouseXOfs < (6+54)
					&& mouseYOfs >= 17 && mouseYOfs < (17+54)) {
				int cX = (mouseXOfs-6)/18;
				int cY = (mouseYOfs-17)/18;
				int idx = cX+(cY*3);
				ItemStack stack = mc.player.inventory.getItemStack().copy();
				if (!stack.isEmpty()) {
					stack.setCount(1);
				}
				craftMatrix.set(idx, stack.isEmpty() ? NonNullList.from(ItemStack.EMPTY) : NonNullList.from(ItemStack.EMPTY, stack));
				new SetCraftingGhostServerMessage(container.windowId, craftMatrix).sendToServer();
			}
		}
		
		RenderHelper.enableGUIStandardItemLighting();
		int l = 0;
		for (int j = -(scrollOffset * container.slotsAcross); j < (container.slotsAcross*container.slotsTall); j++) {
			l++;
			if (j < 0) {
				continue;
			}
			NetworkType type = (l < networkContentsView.size() ? networkContentsView.get(l) : null);
			int x = container.startX + (j % container.slotsAcross) * 18;
			int y = (container.startY + 18) + (j / container.slotsAcross) * 18;
			if (j >= (container.slotsAcross * container.slotsTall)) break;
			if (type != null) {
				itemRender.renderItemAndEffectIntoGUI(mc.player, type.getStack(), x, y);
				itemRender.renderItemOverlayIntoGUI(fontRenderer, type.getStack(), x, y, "");
			}
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			GlStateManager.colorMask(true, true, true, false);

			if (type != null) {
				GlStateManager.pushMatrix();
				GlStateManager.scale(0.5f, 0.5f, 1f);
				boolean oldBidiFlag = fontRenderer.getBidiFlag();
				boolean oldUnicodeMode = fontRenderer.getUnicodeFlag();
				fontRenderer.setBidiFlag(false);
				fontRenderer.setUnicodeFlag(false);
				String str;
				if (type.getStack().hasTagCompound() && type.getStack().getTagCompound().getBoolean("correlated:FromVendingDrive")) {
					// Infinity
					str = "\u221E";
				} else {
					str = Numbers.humanReadableItemCount(type.getStack().getCount());
				}
				fontRenderer.drawStringWithShadow(str, ((x*2)+32)-fontRenderer.getStringWidth(str), (y*2)+24, -1);
				fontRenderer.setBidiFlag(oldBidiFlag);
				fontRenderer.setUnicodeFlag(oldUnicodeMode);
				GlStateManager.popMatrix();
			}
			
			if (mouseXOfs > x && mouseXOfs < x+16 &&
					mouseYOfs > y && mouseYOfs < y+16) {
				drawRect(x, y, x+16, y+16, 0x80FFFFFF);
				hovered = type;
			}
			
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
		}
		
		int craftMatrixHovered = -1;
		NonNullList<ItemStack> craftMatrixPreview = NonNullList.withSize(9, ItemStack.EMPTY);
		
		if (container.hasCraftingMatrix) {
			Multiset<Prototype> alreadySeenNet = HashMultiset.create();
			Multiset<Prototype> alreadySeenInv = HashMultiset.create();
			boolean hasAll = true;
			for (int i = 0; i < craftMatrix.size(); i++) {
				int x = i % 3;
				int y = i / 3;
				x*=18;
				y*=18;
				x+=6;
				y+=17;
				
				boolean available = false;
				NonNullList<ItemStack> possibilities = craftMatrix.get(i);
				ItemStack is = ItemStack.EMPTY;
				if (possibilities.isEmpty()) {
					available = true;
				} else {
					glass: for (ItemStack possibility : possibilities) {
						Prototype pt = new Prototype(possibility);
						if (networkContents.containsKey(pt) && networkContents.get(pt).getStack().getCount() >= alreadySeenNet.count(pt)+1) {
							is = possibility;
							available = true;
							alreadySeenNet.add(pt);
							break;
						} else {
							int found = 0;
							for (int k = 0; k < mc.player.inventory.getSizeInventory(); k++) {
								ItemStack inSlot = mc.player.inventory.getStackInSlot(k);
								if (ItemHandlerHelper.canItemStacksStack(possibility, inSlot)) {
									found += inSlot.getCount();
									if (found >= alreadySeenInv.count(pt)+1) {
										is = possibility;
										available = true;
										alreadySeenInv.add(pt);
										break glass;
									}
								}
							}
						}
					}
				}
				craftMatrixResolved.setInventorySlotContents(i, is);
				
				if (!available) {
					is = possibilities.get(((((int)ClientProxy.ticks)/20)+i)%possibilities.size());
					hasAll = false;
				}
				
				craftMatrixPreview.set(i, is);
				if (drawItemStack(is, x+1, y+1, mouseXOfs, mouseYOfs, !is.isEmpty() && !available)) {
					craftMatrixHovered = i;
				}
			}
			
			if (!hasAll) {
				craftMatrixResolved.clear();
			}
			
			boolean craftMatrixChanged = false;
			
			for (int i = 0; i < 9; i++) {
				if (!ItemStack.areItemStacksEqual(craftMatrixResolved.getStackInSlot(i), craftMatrixDummyLast.get(i)) ||
						!ItemStack.areItemStackTagsEqual(craftMatrixResolved.getStackInSlot(i), craftMatrixDummyLast.get(i))) {
					craftMatrixChanged = true;
					break;
				}
			}
			
			if (craftMatrixChanged) {
				craftMatrixResult = CraftingManager.findMatchingResult(craftMatrixResolved, mc.world);
				for (int i = 0; i < 9; i++) {
					craftMatrixDummyLast.set(i, craftMatrixResolved.getStackInSlot(i));
				}
			}
			
			ItemStack shownResult = craftMatrixResult;
			
			if (!hasAll) {
				shownResult = ItemStack.EMPTY;
			}
			
			if (drawItemStack(shownResult, 26, 104, mouseXOfs, mouseYOfs, false)) {
				craftMatrixHovered = -2;
			}
		}
		mouseInNetworkListing = (mouseXOfs > container.startX && mouseXOfs < container.startX+(container.slotsAcross*18) &&
				mouseYOfs > container.startY && mouseYOfs < container.startY+(container.slotsTall*18));
		
		GlStateManager.disableLighting();
		
		GlStateManager.color(1, 1, 1);
		
		if (hasStatusLine()) {
			String lastLine = signalStrength == 0 ? C28n.format("gui.correlated.noSignal") : container.status.get(container.status.size()-1).getFormattedText().trim();
			int maxLength = signalStrength == -1 ? 160 : 144;
			int len = (int)IBMFontRenderer.measure(lastLine);
			if (len > maxLength) {
				String s = lastLine;
				for (int i = 0; i < lastLine.length(); i++) {
					String str = lastLine.substring(0, i)+"...";
					int slen = (int)IBMFontRenderer.measure(str);
					if (slen > maxLength) break;
					s = str;
				}
				lastLine = s;
			}
			int left = 68+container.playerInventoryOffsetX;
			int top = 90+container.playerInventoryOffsetY;
			int right = 162+68+container.playerInventoryOffsetX;
			int bottom = 11+89+container.playerInventoryOffsetY;
			
			drawRect(left, top, right, bottom, 0xFF006D4B);
			IBMFontRenderer.drawString(left+2, top+1, lastLine, 0x00DBAD);
		}
		
		int u = 232;
		if (rows <= container.slotsTall) {
			u += 12;
		}
		int y = 18;
		mc.getTextureManager().bindTexture(getBackground());
		if (hasStatusLine() && signalStrength != -1) {
			int color = ColorType.PALETTE.getColor(1);
			float r = ((color >> 16)&0xFF)/255f;
			float g = ((color >> 8)&0xFF)/255f;
			float b = (color&0xFF)/255f;
			
			GlStateManager.color(r, g, b);
			int right = 162+68+container.playerInventoryOffsetX;
			int top = 90+container.playerInventoryOffsetY;
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			drawTexturedModalRect(right-18, top+1, 0, 224, 16, 8);
			drawTexturedModalRect(right-18, top+1, 0, 232, 5+(signalStrength*2), 8);
			GlStateManager.disableBlend();
		}
		GlStateManager.enableDepth();
		GlStateManager.color(1, 1, 1);
		drawTexturedModalRect(getScrollTrackX(), getScrollTrackY()+y+(scrollKnobY-6), u, 241, 12, 15);

		GlStateManager.pushMatrix();
		GlStateManager.translate(-(width - xSize) / 2, -(height - ySize) / 2, 0);
		
		GlStateManager.color(1, 1, 1);
		
		if (container.hasCraftingMatrix) {
			drawTexturedModalRect(clearGrid.x+2, clearGrid.y+2, 0, 190, 2, 10);
			drawTexturedModalRect(craftingTarget.x+2, craftingTarget.y+2, container.craftingTarget.ordinal()*8, 200, 8, 8);
			drawTexturedModalRect(craftingAmount.x+2, craftingAmount.y+2, container.craftingAmount.ordinal()*8, 208, 8, 8);			
		}
		
		if (container.terminal.hasMaintenanceSlot()) {
			if (!dump.enabled) {
				GlStateManager.color(0.5f, 0.5f, 0.5f);
			} else if (container.isDumping || container.isFilling) {
				GlStateManager.color(1, 1, 0.25f);
			}
			drawTexturedModalRect(dump.x+2, dump.y+2, 16, (container.isFilling || isShiftKeyDown()) ? 232 : 240, 8, 8);
			if (!partition.enabled) {
				GlStateManager.color(0.5f, 0.5f, 0.5f);
			} else {
				GlStateManager.color(1, 1, 1);
			}
			drawTexturedModalRect(partition.x+2, partition.y+2, 16, 248, 8, 8);
			GlStateManager.color(1, 1, 1);
		}
		
		if (hasSearchAndSort()) {
			drawTexturedModalRect(sortDirection.x+2, sortDirection.y+2, container.sortAscending ? 0 : 8, 216, 8, 8);
			drawTexturedModalRect(sortMode.x+2, sortMode.y+2, 16+(container.sortMode.ordinal()*8), 216, 8, 8);
			drawTexturedModalRect(focusByDefault.x+2, focusByDefault.y+2, container.searchFocusedByDefault ? 8 : 0, 240, 8, 8);
			if (jeiSync != null) {
				drawTexturedModalRect(jeiSync.x+2, jeiSync.y+2, container.jeiSyncEnabled ? 8 : 0, 248, 8, 8);
			}
			searchField.drawTextBox();
			if (sortMode.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						C28n.format("tooltip.correlated.sortmode"),
						"\u00A77"+C28n.format("tooltip.correlated.sortmode."+container.sortMode.lowerName)
					), mouseX, mouseY);
			}
			if (sortDirection.isMouseOver()) {
				String str = (container.sortAscending ? "ascending" : "descending");
				drawHoveringText(Lists.newArrayList(
						C28n.format("tooltip.correlated.sortdirection"),
						"\u00A77"+C28n.format("tooltip.correlated.sortdirection."+str)
					), mouseX, mouseY);
			}
			if (focusByDefault.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						C28n.format("tooltip.correlated.focus_search_by_default."+container.searchFocusedByDefault)
					), mouseX, mouseY);
			}
			if (preferredEnergySystem.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						C28n.format("tooltip.correlated.preferred_energy"),
						"\u00A77"+CConfig.preferredUnit.displayName
					), mouseX, mouseY);
			}
			if (jeiSync != null && jeiSync.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						C28n.format("tooltip.correlated.jei_sync."+container.jeiSyncEnabled)
					), mouseX, mouseY);
			}
			if (container.hasCraftingMatrix) {
				if (craftingAmount.isMouseOver()) {
					drawHoveringText(Lists.newArrayList(
							C28n.format("tooltip.correlated.crafting_amount"),
							"\u00A77"+C28n.format("tooltip.correlated.crafting.only_shift_click"),
							"\u00A77"+C28n.format("tooltip.correlated.crafting_amount."+container.craftingAmount.lowerName)
						), mouseX, mouseY);
				}
				if (craftingTarget.isMouseOver()) {
					drawHoveringText(Lists.newArrayList(
							C28n.format("tooltip.correlated.crafting_target"),
							"\u00A77"+C28n.format("tooltip.correlated.crafting.only_shift_click"),
							"\u00A77"+C28n.format("tooltip.correlated.crafting_target."+container.craftingTarget.lowerName)
						), mouseX, mouseY);
				}
				if (clearGrid.isMouseOver()) {
					drawHoveringText(Lists.newArrayList(C28n.format("tooltip.correlated.clear_crafting_grid")), mouseX, mouseY);
				}
			}
		}
		if (container.terminal.hasMaintenanceSlot()) {
			if (dump.enabled && dump.isMouseOver()) {
				if (isShiftKeyDown() || container.isFilling) {
					drawHoveringText(Lists.newArrayList(
							C28n.format("tooltip.correlated.fill"),
							"\u00A77"+C28n.format("tooltip.correlated.release_shift_to_dump")
						), mouseX, mouseY);
				} else {
					drawHoveringText(Lists.newArrayList(
							C28n.format("tooltip.correlated.dump"),
							"\u00A77"+C28n.format("tooltip.correlated.shift_to_fill")
						), mouseX, mouseY);
				}
			}
			if (partition.enabled && partition.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						C28n.format("tooltip.correlated.partition")
					), mouseX, mouseY);
			}
		}
		if (mc.player.inventory.getItemStack().isEmpty() && craftMatrixHovered != -1) {
			ItemStack is = craftMatrixHovered == -2 ? craftMatrixResult : craftMatrixPreview.get(craftMatrixHovered);
			if (!is.isEmpty()) {
				GuiUtils.drawHoveringText(is, getItemToolTip(is), mouseX, mouseY, width, height, -1, fontRenderer);
			}
		}
		
		GlStateManager.disableLighting();
		mc.renderEngine.bindTexture(ENERGY);
		GlStateManager.color(1, 1, 1);
		drawModalRectWithCustomSizedTexture(preferredEnergySystem.x+2, preferredEnergySystem.y+2, 0, CConfig.preferredUnit.ordinal()*8, 8, 8, 8, 72);
		
		if (mc.player.inventory.getItemStack().isEmpty() && hovered != null) {
			List<String> tooltip = getItemToolTip(hovered.getStack());
			int totalWidth = fontRenderer.getStringWidth(GROUPED_INTEGER.format(hovered.getStack().getCount())+" total")/2;
			int lastModifiedWidth = fontRenderer.getStringWidth(NetworkType.formatLastModified(hovered.getLastModified()))/2;
			int spaceWidth = fontRenderer.getCharWidth(' ');
			tooltip.add(Strings.repeat(" ", IntMath.divide(Math.max(totalWidth, lastModifiedWidth), spaceWidth, RoundingMode.UP)));
			isDrawingHoverTooltip = true;
			GuiUtils.drawHoveringText(hovered.getStack(), tooltip, mouseX, mouseY, width, height, -1, fontRenderer);
			isDrawingHoverTooltip = false;
		}
		
		GlStateManager.disableDepth();
		GlStateManager.disableLighting();
		
		if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
			fontRenderer.drawStringWithShadow("- DEBUG ENABLED -", 2, 2, 0xFF0000);
			fontRenderer.drawStringWithShadow("S&S: "+(hasSearchAndSort() ? "\u00A7atrue" : "\u00A7cfalse"), 2, 12+2, -1);
			fontRenderer.drawStringWithShadow("MS: "+(container.maintenanceSlot != null ? "\u00A7atrue" : "\u00A7cfalse"), 2, 12+12+2, -1);
			fontRenderer.drawStringWithShadow("CM: "+(container.hasCraftingMatrix ? "\u00A7atrue" : "\u00A7cfalse"), 2, 12+12+12+2, -1);
			fontRenderer.drawStringWithShadow("SL: "+(hasStatusLine() ? "\u00A7atrue" : "\u00A7cfalse"), 2, 12+12+12+12+2, -1);
			fontRenderer.drawStringWithShadow("QT: "+queryType, 2, 12+12+12+12+12+2, -1);
			fontRenderer.drawStringWithShadow("S/K: "+networkContentsView.size()+"/"+networkContents.size(), 2, 12+12+12+12+12+12+2, -1);
		}
		
		GlStateManager.popMatrix();

		
		if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
			Gui.drawRect(getScrollTrackX(), getScrollTrackY()+18, getScrollTrackX()+12, getScrollTrackY()+18+getScrollTrackHeight(), 0x440000FF);
			Gui.drawRect(0, 0, getXOffset(), 1, 0xAAFF0000);
			Gui.drawRect(0, 1, 1, getYOffset(), 0xAAFF0000);
			Gui.drawRect(container.startX-1, container.startY+17, container.startX+(container.slotsAcross*18)-1, container.startY+(container.slotsTall*18)+18, 0x4400FF00);
			Gui.drawRect(container.startX+container.playerInventoryOffsetX-1, 102+container.startY+container.playerInventoryOffsetY, container.startX+container.playerInventoryOffsetX+(9*18)-1, 102+container.startY+container.playerInventoryOffsetY+(3*18), 0x44FF00FF);
			Gui.drawRect(container.startX+container.playerInventoryOffsetX-1, 160+container.startY+container.playerInventoryOffsetY, container.startX+container.playerInventoryOffsetX+(9*18)-1, 160+container.startY+container.playerInventoryOffsetY+18, 0x44FF00FF);
			
			fontRenderer.drawString(container.slotsAcross+"x"+container.slotsTall, container.startX+2, container.startY+18+2, 0);
			fontRenderer.drawString(scrollOffset+"", getScrollTrackX(), getScrollTrackY()+20, 0);
			fontRenderer.drawString(scrollKnobY+"", getScrollTrackX(), getScrollTrackY()+32, 0);
			GlStateManager.enableDepth();
		}
	}
	
	private boolean drawItemStack(ItemStack is, int x, int y, int mouseX, int mouseY, boolean redOverlay) {
		boolean hovered = false;

		itemRender.renderItemAndEffectIntoGUI(mc.player, is, x, y);
		itemRender.renderItemOverlayIntoGUI(fontRenderer, is, x, y, "");
		
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		GlStateManager.colorMask(true, true, true, false);
		if (mouseX > x && mouseX < x+16 &&
				mouseY > y && mouseY < y+16) {
			drawRect(x, y, x+16, y+16, 0x80FFFFFF);
			hovered = true;
		}
		
		if (redOverlay) {
			drawRect(x, y, x+16, y+16, 0x60FF0000);
		}
		
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		
		return hovered;
	}

	@SubscribeEvent
	public void onRenderTooltipPostText(RenderTooltipEvent.PostText e) {
		if (isDrawingHoverTooltip) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5f, 0.5f, 1);
			int x = e.getX()*2;
			int y = (e.getY()+e.getHeight()-8)*2;
			String totalString;
			if (hovered.getStack().hasTagCompound() && hovered.getStack().getTagCompound().getBoolean("correlated:FromVendingDrive")) {
				totalString = "Infinity total";
			} else {
				totalString = GROUPED_INTEGER.format(hovered.getStack().getCount())+" total";
			}
			String modifiedString = NetworkType.formatLastModified(hovered.getLastModified());
			fontRenderer.drawStringWithShadow(totalString, (x+(e.getWidth()*2))-fontRenderer.getStringWidth(totalString), y, 0xAAAAAA);
			fontRenderer.drawStringWithShadow(modifiedString, (x+(e.getWidth()*2))-fontRenderer.getStringWidth(modifiedString), y+10, 0xAAAAAA);
			GlStateManager.popMatrix();
		}
	}

	private boolean draggingScrollKnob = false;
	private int scrollKnobY = 6;
	private int ticksSinceLastQueryChange = 0;

	@Override
	public void initGui() {
		super.initGui();
		int x = (width - xSize) / 2;
		x += getXOffset();
		int y = (height - ySize) / 2;
		y += getYOffset();
		if (hasSearchAndSort()) {
			searchField.x = x+143;
			searchField.y = y+6;
			buttonList.add(sortDirection = new GuiButtonExt(0, x+100, y+4, 12, 12, ""));
			buttonList.add(sortMode = new GuiButtonExt(1, x+114, y+4, 12, 12, ""));
			buttonList.add(focusByDefault = new GuiButtonExt(5, x+128, y+4, 12, 12, ""));
			buttonList.add(preferredEnergySystem = new GuiButtonExt(9, x-getXOffset()+getEnergyUnitX(), y-getYOffset()+getEnergyUnitY(), 12, 12, ""));
			if (Correlated.inst.jeiAvailable) {
				buttonList.add(jeiSync = new GuiButtonExt(6, x+236, y+4, 12, 12, ""));
			}
		}
		if (container.hasCraftingMatrix) {
			buttonList.add(craftingAmount = new GuiButtonExt(2, x+51, y+99, 12, 12, ""));
			buttonList.add(craftingTarget = new GuiButtonExt(3, x+51, y+113, 12, 12, ""));
			buttonList.add(clearGrid = new GuiButtonExt(4, x+61, y+37, 6, 14, ""));
		}
		if (container.terminal.hasMaintenanceSlot()) {
			buttonList.add(dump = new GuiButtonExt(7, x+35, y+156, 12, 12, ""));
			buttonList.add(partition = new GuiButtonExt(8, x+35, y+170, 12, 12, ""));
		}
	}
	
	protected int getYOffset() {
		return 0;
	}
	
	protected int getXOffset() {
		return 0;
	}
	
	protected int getEnergyUnitX() {
		return 236;
	}
	
	protected int getEnergyUnitY() {
		return 130;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			mc.playerController.sendEnchantPacket(container.windowId, container.sortAscending ? -2 : -1);
			container.sortAscending = !container.sortAscending;
			dirty = true;
		} else if (button.id == 1) {
			switch (container.sortMode) {
				case QUANTITY:
					container.sortMode = SortMode.MOD_MINECRAFT_FIRST;
					mc.playerController.sendEnchantPacket(container.windowId, -4);
					break;
				case MOD_MINECRAFT_FIRST:
					container.sortMode = SortMode.MOD;
					mc.playerController.sendEnchantPacket(container.windowId, -5);
					break;
				case MOD:
					container.sortMode = SortMode.NAME;
					mc.playerController.sendEnchantPacket(container.windowId, -6);
					break;
				case NAME:
					container.sortMode = SortMode.LAST_MODIFIED;
					mc.playerController.sendEnchantPacket(container.windowId, -7);
					break;
				case LAST_MODIFIED:
					container.sortMode = SortMode.QUANTITY;
					mc.playerController.sendEnchantPacket(container.windowId, -3);
					break;
			}
			dirty = true;
		} else if (button.id == 2) {
			switch (container.craftingAmount) {
				case ONE:
					container.craftingAmount = CraftingAmount.STACK;
					mc.playerController.sendEnchantPacket(container.windowId, -11);
					break;
				case STACK:
					container.craftingAmount = CraftingAmount.MAX;
					mc.playerController.sendEnchantPacket(container.windowId, -12);
					break;
				case MAX:
					container.craftingAmount = CraftingAmount.ONE;
					mc.playerController.sendEnchantPacket(container.windowId, -10);
					break;
			}
		} else if (button.id == 3) {
			switch (container.craftingTarget) {
				case INVENTORY:
					container.craftingTarget = CraftingTarget.NETWORK;
					mc.playerController.sendEnchantPacket(container.windowId, -21);
					break;
				case NETWORK:
					container.craftingTarget = CraftingTarget.INVENTORY;
					mc.playerController.sendEnchantPacket(container.windowId, -20);
					break;

			}
		} else if (button.id == 4) {
			craftMatrix.clear();
			new SetCraftingGhostServerMessage(container.windowId, craftMatrix).sendToServer();
		} else if (button.id == 5) {
			mc.playerController.sendEnchantPacket(container.windowId, container.searchFocusedByDefault ? -25 : -24);
			container.searchFocusedByDefault = !container.searchFocusedByDefault;
		} else if (button.id == 6) {
			mc.playerController.sendEnchantPacket(container.windowId, container.jeiSyncEnabled ? -27 : -26);
			container.jeiSyncEnabled = !container.jeiSyncEnabled;
		} else if (button.id == 7) {
			if (container.isDumping || container.isFilling) {
				mc.playerController.sendEnchantPacket(container.windowId, -29);
				container.isDumping = false;
				container.isFilling = false;
			} else {
				if (isShiftKeyDown()) {
					mc.playerController.sendEnchantPacket(container.windowId, -61);
					container.isDumping = false;
					container.isFilling = true;
				} else {
					mc.playerController.sendEnchantPacket(container.windowId, -28);
					container.isDumping = true;
					container.isFilling = false;
				}
			}
		} else if (button.id == 8) {
			mc.playerController.sendEnchantPacket(container.windowId, -62);
		} else if (button.id == 9) {
			int ordinal = CConfig.preferredUnit.ordinal();
			ordinal = (ordinal + 1) % EnergyUnit.values().length;
			EnergyUnit eu = EnergyUnit.values()[ordinal];
			CConfig.preferredUnit = eu;
			CConfig.save();
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (rows > container.slotsTall) {
			int dWheel = Mouse.getDWheel() / 120;
			if (dWheel != 0) {
				scrollOffset = Math.max(Math.min(rows-1, scrollOffset-dWheel), 0);
				dWheel *= IntMath.divide(getScrollTrackHeight()+6, rows, RoundingMode.UP);
				scrollKnobY = Math.max(Math.min(getScrollTrackHeight()-9, scrollKnobY-dWheel), 6);
			}
		} else {
			scrollKnobY = 6;
		}
		if (hasSearchAndSort()) {
			searchField.updateCursorCounter();
			if (container.jeiSyncEnabled) {
				String jeiQuery = Correlated.inst.jeiQueryReader.get();
				if (!Objects.equal(jeiQuery, lastJeiQuery)) {
					lastJeiQuery = jeiQuery;
					searchField.setText(jeiQuery);
				}
			}
			if (!Objects.equal(searchField.getText(), lastSearchQuery)) {
				lastSearchQuery = searchField.getText();
				ticksSinceLastQueryChange = 0;
				if (scrollKnobY != 6) {
					scrollKnobY = 6;
					mc.playerController.sendEnchantPacket(container.windowId, 0);
				}
			}
			ticksSinceLastQueryChange++;
			if (ticksSinceLastQueryChange == 2) {
				new SetSearchQueryServerMessage(container.windowId, lastSearchQuery).sendToServer();
				dirty = true;
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (searchField.isFocused()) {
			if (keyCode == Keyboard.KEY_ESCAPE) {
				mc.player.closeScreen();
			} else {
				searchField.textboxKeyTyped(typedChar, keyCode);
				if (container.jeiSyncEnabled) {
					Correlated.inst.jeiQueryUpdater.accept(searchField.getText());
				}
			}
		} else {
			if (keyCode == Keyboard.KEY_F3) {
				mc.gameSettings.showDebugInfo = !mc.gameSettings.showDebugInfo;
				return;
			}
			tryAction(keyCode);
			super.keyTyped(typedChar, keyCode);
		}
	}

	protected int getScrollTrackX() {
		return 236;
	}
	
	protected int getScrollTrackY() {
		return 0;
	}
	
	protected int getScrollTrackHeight() {
		return 110;
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		
		if (container.hasCraftingMatrix) {
			if ((mouseButton == 0 || mouseButton == 1)
					&& mouseX >= x+26 && mouseX <= x+(26+16)
					&& mouseY >= y+104 && mouseY <= y+(104+16)) {
				int action;
				if (isShiftKeyDown()) {
					action = 2;
				} else if (mouseButton == 0) {
					action = 0;
				} else if (mouseButton == 1) {
					action = 1;
				} else {
					throw new AssertionError();
				}
				new CraftItemMessage(container.windowId, craftMatrixResolved, action).sendToServer();
			}
		}
		
		int left = 68+container.playerInventoryOffsetX;
		int top = 90+container.playerInventoryOffsetY;
		int right = 162+68+container.playerInventoryOffsetX;
		int bottom = 11+89+container.playerInventoryOffsetY;
		if (hasStatusLine() && mouseButton == 0
				&& mouseX >= x+left && mouseX <= x+right
				&& mouseY >= y+top && mouseY <= y+bottom) {
			if (signalStrength != -1) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiSelectAPN(this, true, false, container));
			} else {
				Minecraft.getMinecraft().displayGuiScreen(new GuiTerminalShell(this, container));
			}
		}
		
		int width = 12;
		int height = getScrollTrackHeight();
		x += getScrollTrackX();
		y += getScrollTrackY();
		y += 18;
		if (mouseButton == 0
				&& mouseX >= x && mouseX <= x+width
				&& mouseY >= y && mouseY <= y+height) {
			draggingScrollKnob = true;
			mouseClickMove(mouseX, mouseY, mouseButton, 0);
			return;
		}
		if (hasSearchAndSort()) {
			if (mouseButton == 1
					&& mouseX >= searchField.x && mouseX <= searchField.x+searchField.width
					&& mouseY >= searchField.y && mouseY <= searchField.y+searchField.height) {
				searchField.setText("");
				if (container.jeiSyncEnabled) {
					Correlated.inst.jeiQueryUpdater.accept("");
				}
			} else if (mouseButton == 1 && preferredEnergySystem.isMouseOver()) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
				int ordinal = CConfig.preferredUnit.ordinal();
				ordinal = (ordinal - 1) % EnergyUnit.values().length;
				if (ordinal < 0) {
					ordinal += EnergyUnit.values().length;
				}
				EnergyUnit eu = EnergyUnit.values()[ordinal];
				CConfig.preferredUnit = eu;
				CConfig.save();
			}
			searchField.mouseClicked(mouseX, mouseY, mouseButton);
		}
		tryAction(mouseButton - 100);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	private void tryAction(int keyCode) {
		if (mc.player.inventory.getItemStack().isEmpty()) {
			if (hovered != null) {
				Prototype proto = hovered.getPrototype();
				if (mc.gameSettings.keyBindDrop.isActiveAndMatches(keyCode)) {
					new TerminalActionMessage(TerminalAction.DROP, proto, isCtrlKeyDown() ? 1 : 0).sendToServer();
				} else if (mc.gameSettings.keyBindPickBlock.isActiveAndMatches(keyCode)) {
					new TerminalActionMessage(TerminalAction.INVOKE, proto, (isCtrlKeyDown() ? 1 : 0) | (isShiftKeyDown() ? 2 : 0)).sendToServer();
				} else if (keyCode == -100) {
					if (isShiftKeyDown()) {
						new TerminalActionMessage(TerminalAction.GET, proto, 3).sendToServer();
					} else {
						new TerminalActionMessage(TerminalAction.GET, proto, 1).sendToServer();
						moveItemToCursor(hovered, Math.min(hovered.getStack().getCount(), hovered.getStack().getMaxStackSize()));
					}
				} else if (keyCode == -99) {
					if (isShiftKeyDown()) {
						new TerminalActionMessage(TerminalAction.GET, proto, 2).sendToServer();
					} else {
						new TerminalActionMessage(TerminalAction.GET, proto, 0).sendToServer();
						moveItemToCursor(hovered, 1);
					}
				}
			}
		} else if (mouseInNetworkListing) {
			if (keyCode == -100) {
				new TerminalActionMessage(TerminalAction.PUT, Prototype.EMPTY, 0).sendToServer();
				mc.player.inventory.setItemStack(ItemStack.EMPTY);
			} else if (keyCode == -99) {
				new TerminalActionMessage(TerminalAction.PUT, Prototype.EMPTY, 1).sendToServer();
				mc.player.inventory.getItemStack().shrink(1);;
			}
		}
	}

	private void moveItemToCursor(NetworkType nt, int amt) {
		ItemStack copy = nt.getStack().copy();
		nt.getStack().shrink(amt);
		copy.setCount(amt);
		mc.player.inventory.setItemStack(copy);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
		if (draggingScrollKnob && rows > container.slotsTall) {
			int y = (height - ySize) / 2;
			y += getScrollTrackY();
			scrollKnobY = Math.max(Math.min(getScrollTrackHeight()-9, (mouseY-24)-y), 6);
			float pct = ((scrollKnobY-6)/(float)(getScrollTrackHeight()-9));
			scrollOffset = (int)(pct * rows);
		}
		super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			draggingScrollKnob = false;
		}
		Slot slot = getSlotAtPosition(mouseX, mouseY);
		if (doubleClick && slot != null && mouseButton == 0 && inventorySlots.canMergeSlot(ItemStack.EMPTY, slot)
				&& isShiftKeyDown()
				&& slot != null && slot.inventory != null && shiftClickedSlot != null) {
			new InsertAllMessage(inventorySlots.windowId, shiftClickedSlot).sendToServer();
			doubleClick = false;
		} else {
			super.mouseReleased(mouseX, mouseY, mouseButton);
		}
	}

	public void updateSearchQuery(String query) {
		lastSearchQuery = query;
		if (hasSearchAndSort()) {
			searchField.setText(query);
			if (container.jeiSyncEnabled) {
				Correlated.inst.jeiQueryUpdater.accept(query);
			}
		}
	}

	public void addLine(ITextComponent line) {
		container.status.add(line);
	}

	public void focusSearch() {
		if (searchField != null) {
			searchField.setFocused(true);
		}
	}

	public void setRecipe(List<? extends List<ItemStack>> matrix) {
		for (int i = 0; i < 9; i++) {
			List<ItemStack> li = matrix.get(i);
			NonNullList<ItemStack> nnl = NonNullList.from(ItemStack.EMPTY, li.toArray(new ItemStack[li.size()]));
			craftMatrix.set(i, nnl);
		}
		new SetCraftingGhostServerMessage(container.windowId, craftMatrix).sendToServer();
	}

}
