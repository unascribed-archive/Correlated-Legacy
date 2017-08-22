package com.elytradev.correlated.init;

import java.util.List;
import java.util.Locale;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.item.ItemDebugginator;
import com.elytradev.correlated.item.ItemDocTablet;
import com.elytradev.correlated.item.ItemDrive;
import com.elytradev.correlated.item.ItemFloppy;
import com.elytradev.correlated.item.ItemKeycard;
import com.elytradev.correlated.item.ItemMemory;
import com.elytradev.correlated.item.ItemMisc;
import com.elytradev.correlated.item.ItemModule;
import com.elytradev.correlated.item.ItemWeldthrower;
import com.google.common.collect.Lists;
import com.elytradev.correlated.item.ItemHandheldTerminal;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class CItems {

	final static List<Item> itemBlocks = Lists.newArrayList();
	final static List<Item> records = Lists.newArrayList();
	
	public static ItemMisc MISC;
	public static ItemDrive DRIVE;
	public static ItemMemory MEMORY;
	public static ItemModule MODULE;
	public static ItemFloppy FLOPPY;
	public static ItemHandheldTerminal HANDHELD_TERMINAL;
	public static ItemWeldthrower WELDTHROWER;
	public static ItemKeycard KEYCARD;
	public static ItemDocTablet DOC_TABLET;
	public static ItemDebugginator DEBUGGINATOR;
	
	@SubscribeEvent
	public static void register(RegistryEvent.Register<Item> e) {
		register(e.getRegistry(), new ItemMisc(), "misc");
		register(e.getRegistry(), new ItemDrive(), "drive");
		register(e.getRegistry(), new ItemMemory(), "memory");
		register(e.getRegistry(), new ItemModule(), "module");
		register(e.getRegistry(), new ItemFloppy(), "floppy");
		register(e.getRegistry(), new ItemHandheldTerminal(), "handheld_terminal");
		register(e.getRegistry(), new ItemWeldthrower(), "weldthrower");
		register(e.getRegistry(), new ItemKeycard(), "keycard");
		register(e.getRegistry(), new ItemDocTablet(), "doc_tablet");
		register(e.getRegistry(), new ItemDebugginator(), "debugginator");
		
		for (Item i : itemBlocks) {
			e.getRegistry().register(i);
		}
		for (Item i : records) {
			e.getRegistry().register(i);
		}
		
		COres.register();
	}
	
	private static void register(IForgeRegistry<Item> registry, Item item, String name) {
		item.setUnlocalizedName("correlated."+name);
		item.setCreativeTab(Correlated.CREATIVE_TAB);
		item.setRegistryName(name);
		registry.register(item);
		try {
			CItems.class.getField(name.toUpperCase(Locale.ROOT)).set(null, item);
		} catch (NoSuchFieldException e) {
			throw new AssertionError("Missing field for item "+name);
		} catch (Exception e) {
			CLog.warn("Unexpected error while filling field for item {}", name, e);
		}
	}

}
