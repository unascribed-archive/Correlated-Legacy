package com.elytradev.correlated.init;

import net.minecraftforge.oredict.OreDictionary;

public class COres {

	public static final String LUMINOUS_PEARL_OR_DATA_CORE = "correlated:INTERNAL_luminousPearlOrDataCore";
	
	public static final String GEM_CHRYSOCOLLA = "gemChrysocolla";
	public static final String INGOT_CHRYSOCOLLA = "ingotChrysocolla";
	
	public static final String INGOT_YTTRIUM = "ingotYttrium";
	public static final String INGOT_CERIUM = "ingotCerium";
	public static final String INGOT_PRASEODYMIUM = "ingotPraseodymium";
	public static final String INGOT_NEODYMIUM = "ingotNeodymium";
	public static final String INGOT_HOLMIUM = "ingotHolmium";
	public static final String INGOT_LUTETIUM = "ingotLutetium";
	public static final String INGOT_MIXED = "ingotMixed";
	
	
	public static final String INGOT_IRON = "ingotIron";
	public static final String INGOT_GOLD = "ingotGold";
	public static final String INGOT_BRICK = "ingotBrick";
	
	public static final String GEM_QUARTZ = "gemQuartz";
	public static final String GEM_DIAMOND = "gemDiamond";
	
	public static final String BLOCK_DIAMOND = "blockDiamond";
	public static final String BLOCK_IRON = "blockIron";
	public static final String BLOCK_GLASS = "blockGlass";
	
	public static final String PANE_GLASS = "paneGlass";
	
	public static final String DUST_GLOWSTONE = "dustGlowstone";
	
	public static final String NUGGET_GOLD = "nuggetGold";
	
	public static final String OBSIDIAN = "obsidian";
	
	public static void register() {
		OreDictionary.registerOre(LUMINOUS_PEARL_OR_DATA_CORE, CStacks.luminousPearl());
		OreDictionary.registerOre(LUMINOUS_PEARL_OR_DATA_CORE, CStacks.dataCore());
		
		OreDictionary.registerOre(GEM_CHRYSOCOLLA, CStacks.chrysocolla());
		OreDictionary.registerOre(INGOT_CHRYSOCOLLA, CStacks.chrysocollaIngot());
		
		OreDictionary.registerOre(INGOT_YTTRIUM, CStacks.yttriumIngot());
		OreDictionary.registerOre(INGOT_CERIUM, CStacks.ceriumIngot());
		OreDictionary.registerOre(INGOT_PRASEODYMIUM, CStacks.praseodymiumIngot());
		OreDictionary.registerOre(INGOT_NEODYMIUM, CStacks.neodymiumIngot());
		OreDictionary.registerOre(INGOT_HOLMIUM, CStacks.holmiumIngot());
		OreDictionary.registerOre(INGOT_LUTETIUM, CStacks.lutetiumIngot());
		OreDictionary.registerOre(INGOT_MIXED, CStacks.mixedIngot());
	}
	
}
