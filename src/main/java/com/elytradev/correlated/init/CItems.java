package com.elytradev.correlated.init;

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
import com.elytradev.correlated.item.ItemHandheldTerminal;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CItems {

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
	
	
	public static void register() {
		register(new ItemMisc(), "misc", -2);
		register(new ItemDrive(), "drive", -1);
		register(new ItemMemory(), "memory", -1);
		register(new ItemModule(), "module", ItemModule.types.length);
		register(new ItemFloppy(), "floppy", -2);
		register(new ItemHandheldTerminal(), "handheld_terminal", 0);
		register(new ItemWeldthrower(), "weldthrower", 0);
		register(new ItemKeycard(), "keycard", -2);
		register(new ItemDocTablet(), "doc_tablet", 0);
		register(new ItemDebugginator(), "debugginator", -2);
	}
	
	private static void register(Item item, String name, int variants) {
		item.setUnlocalizedName("correlated."+name);
		item.setCreativeTab(Correlated.CREATIVE_TAB);
		item.setRegistryName(name);
		GameRegistry.register(item);
		Correlated.proxy.registerItemModel(item, variants);
		try {
			CItems.class.getField(name.toUpperCase(Locale.ROOT)).set(null, item);
		} catch (NoSuchFieldException e) {
			throw new AssertionError("Missing field for item "+name);
		} catch (Exception e) {
			CLog.warn("Unexpected error while filling field for item {}", name, e);
		}
	}

}
