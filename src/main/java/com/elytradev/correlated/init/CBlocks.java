package com.elytradev.correlated.init;

import java.util.Locale;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockController;
import com.elytradev.correlated.block.BlockDecor;
import com.elytradev.correlated.block.BlockDecorSlab;
import com.elytradev.correlated.block.BlockDecorStairs;
import com.elytradev.correlated.block.BlockDriveBay;
import com.elytradev.correlated.block.BlockGlowingDecor;
import com.elytradev.correlated.block.BlockGlowingDecorSlab;
import com.elytradev.correlated.block.BlockImporterChest;
import com.elytradev.correlated.block.BlockInterface;
import com.elytradev.correlated.block.BlockMemoryBay;
import com.elytradev.correlated.block.BlockTerminal;
import com.elytradev.correlated.block.BlockWireless;
import com.elytradev.correlated.block.BlockGlowingDecor.Variant;
import com.elytradev.correlated.block.item.ItemBlockController;
import com.elytradev.correlated.block.item.ItemBlockDecor;
import com.elytradev.correlated.block.item.ItemBlockDriveBay;
import com.elytradev.correlated.block.item.ItemBlockGlowingDecor;
import com.elytradev.correlated.block.item.ItemBlockInterface;
import com.elytradev.correlated.block.item.ItemBlockMemoryBay;
import com.elytradev.correlated.block.item.ItemBlockTerminal;
import com.elytradev.correlated.block.item.ItemBlockWireless;
import com.google.common.base.Throwables;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CBlocks {

	public static BlockController CONTROLLER;
	public static BlockDriveBay DRIVE_BAY;
	public static BlockMemoryBay MEMORY_BAY;
	public static BlockTerminal TERMINAL;
	public static BlockInterface INTERFACE;
	public static BlockImporterChest IMPORTER_CHEST;
	public static BlockWireless WIRELESS;
	public static BlockDecor DECOR_BLOCK;
	public static BlockGlowingDecor GLOWING_DECOR_BLOCK;
	
	public static BlockDecorStairs DUNGEONCRETE_STAIRS;
	public static BlockDecorStairs DUNGEONCRETE_GRATE_STAIRS;
	public static BlockDecorStairs DUNGEONCRETE_LARGETILE_STAIRS;
	public static BlockDecorStairs DUNGEONCRETE_VERTICAL_STAIRS;
	public static BlockDecorStairs ELUCID_BRICK_STAIRS;
	public static BlockDecorStairs ELUCID_GRIT_STAIRS;
	public static BlockDecorStairs ELUCID_SCALE_STAIRS;
	public static BlockDecorStairs PLATING_STAIRS;
	public static BlockDecorStairs LITHOGRAPHENE_OFF_STAIRS;
	public static BlockDecorStairs LITHOGRAPHENE_OFF_VARIANT_STAIRS;
	public static BlockDecorStairs LITHOGRAPHENE_ON_STAIRS;
	public static BlockDecorStairs LITHOGRAPHENE_ON_VARIANT_STAIRS;
	
	public static BlockDecorSlab DECOR_SLAB;
	public static BlockGlowingDecorSlab GLOWING_DECOR_SLAB;
	public static BlockDecorSlab DECOR_DOUBLE_SLAB;
	public static BlockGlowingDecorSlab GLOWING_DECOR_DOUBLE_SLAB;
	
	public static void register() {
		register(new BlockController().setHardness(2), ItemBlockController.class, "controller", 16);
		register(new BlockDriveBay().setHardness(2), ItemBlockDriveBay.class, "drive_bay", 0);
		register(new BlockMemoryBay().setHardness(2), ItemBlockMemoryBay.class, "memory_bay", 0);
		register(new BlockTerminal().setHardness(2), ItemBlockTerminal.class, "terminal", 0);
		register(new BlockInterface().setHardness(2), ItemBlockInterface.class, "interface", 0);
		register(new BlockImporterChest().setHardness(2), null, "importer_chest", 0);
		register(new BlockWireless().setHardness(2), ItemBlockWireless.class, "wireless", 3);
		
		register(new BlockDecor().setHardness(2), ItemBlockDecor.class, "decor_block", BlockDecor.Variant.VALUES.length);
		register(new BlockGlowingDecor().setHardness(2), ItemBlockGlowingDecor.class, "glowing_decor_block", BlockGlowingDecor.Variant.VALUES.length);
		
		for (BlockDecor.Variant v : BlockDecor.Variant.VALUES) {
			register(new BlockDecorStairs(CBlocks.DECOR_BLOCK.getDefaultState().withProperty(BlockDecor.VARIANT, v)), ItemBlock.class, v.getName()+"_stairs", 0);
		}
		for (BlockGlowingDecor.Variant v : BlockGlowingDecor.Variant.VALUES) {
			if (v == Variant.LANTERN) continue;
			register(new BlockDecorStairs(CBlocks.GLOWING_DECOR_BLOCK.getDefaultState().withProperty(BlockGlowingDecor.VARIANT, v)), ItemBlock.class, v.getName()+"_stairs", 0);
		}
		
		register(new BlockDecorSlab(false).setHardness(2), null, "decor_slab", BlockDecor.Variant.VALUES.length);
		register(new BlockGlowingDecorSlab(false).setHardness(2), null, "glowing_decor_slab", BlockGlowingDecor.Variant.VALUES.length);
		
		register(new BlockDecorSlab(true).setHardness(2), null, "decor_double_slab", BlockDecor.Variant.VALUES.length);
		register(new BlockGlowingDecorSlab(true).setHardness(2), null, "glowing_decor_double_slab", BlockGlowingDecor.Variant.VALUES.length);
		
		Item dungeonslabitem = new ItemSlab(CBlocks.DECOR_SLAB, CBlocks.DECOR_SLAB, CBlocks.DECOR_DOUBLE_SLAB)
				.setHasSubtypes(true).setRegistryName(CBlocks.DECOR_SLAB.getRegistryName());
		GameRegistry.register(dungeonslabitem);
		Correlated.proxy.registerItemModel(dungeonslabitem, BlockDecor.Variant.VALUES.length);
		
		Item lithographeneslabitem = new ItemSlab(CBlocks.GLOWING_DECOR_SLAB, CBlocks.GLOWING_DECOR_SLAB, CBlocks.GLOWING_DECOR_DOUBLE_SLAB)
				.setHasSubtypes(true).setRegistryName(CBlocks.GLOWING_DECOR_SLAB.getRegistryName());
		GameRegistry.register(lithographeneslabitem);
		Correlated.proxy.registerItemModel(lithographeneslabitem, BlockGlowingDecor.Variant.VALUES.length);
	}

	private static void register(Block block, Class<? extends ItemBlock> item, String name, int itemVariants) {
		block.setUnlocalizedName("correlated."+name);
		block.setCreativeTab(Correlated.CREATIVE_TAB);
		block.setRegistryName(name);
		GameRegistry.register(block);
		if (item != null) {
			try {
				ItemBlock ib = item.getConstructor(Block.class).newInstance(block);
				ib.setRegistryName(name);
				GameRegistry.register(ib);
			} catch (Exception e1) {
				Throwables.propagate(e1);
			}
			Correlated.proxy.registerItemModel(Item.getItemFromBlock(block), itemVariants);
		}
		try {
			CBlocks.class.getField(name.toUpperCase(Locale.ROOT)).set(null, block);
		} catch (NoSuchFieldException e) {
			throw new AssertionError("Missing field for block "+name);
		} catch (Exception e) {
			CLog.warn("Unexpected error while filling field for block {}", name, e);
		}
	}

}
