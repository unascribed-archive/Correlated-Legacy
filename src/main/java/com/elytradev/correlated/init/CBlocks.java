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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

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
	
	@SubscribeEvent
	public static void register(RegistryEvent.Register<Block> e) {
		register(e.getRegistry(), new BlockController().setHardness(2), ItemBlockController.class, "controller");
		register(e.getRegistry(), new BlockDriveBay().setHardness(2), ItemBlockDriveBay.class, "drive_bay");
		register(e.getRegistry(), new BlockMemoryBay().setHardness(2), ItemBlockMemoryBay.class, "memory_bay");
		register(e.getRegistry(), new BlockTerminal().setHardness(2), ItemBlockTerminal.class, "terminal");
		register(e.getRegistry(), new BlockInterface().setHardness(2), ItemBlockInterface.class, "interface");
		register(e.getRegistry(), new BlockImporterChest().setHardness(2), null, "importer_chest");
		register(e.getRegistry(), new BlockWireless().setHardness(2), ItemBlockWireless.class, "wireless");
		
		register(e.getRegistry(), new BlockDecor().setHardness(2), ItemBlockDecor.class, "decor_block");
		register(e.getRegistry(), new BlockGlowingDecor().setHardness(2), ItemBlockGlowingDecor.class, "glowing_decor_block");
		
		for (BlockDecor.Variant v : BlockDecor.Variant.VALUES) {
			register(e.getRegistry(), new BlockDecorStairs(CBlocks.DECOR_BLOCK.getDefaultState().withProperty(BlockDecor.VARIANT, v)), ItemBlock.class, v.getName()+"_stairs");
		}
		for (BlockGlowingDecor.Variant v : BlockGlowingDecor.Variant.VALUES) {
			if (v == Variant.LANTERN) continue;
			register(e.getRegistry(), new BlockDecorStairs(CBlocks.GLOWING_DECOR_BLOCK.getDefaultState().withProperty(BlockGlowingDecor.VARIANT, v)), ItemBlock.class, v.getName()+"_stairs");
		}
		
		register(e.getRegistry(), new BlockDecorSlab(false).setHardness(2), null, "decor_slab");
		register(e.getRegistry(), new BlockGlowingDecorSlab(false).setHardness(2), null, "glowing_decor_slab");
		
		register(e.getRegistry(), new BlockDecorSlab(true).setHardness(2), null, "decor_double_slab");
		register(e.getRegistry(), new BlockGlowingDecorSlab(true).setHardness(2), null, "glowing_decor_double_slab");
		
		Item dungeonslabitem = new ItemSlab(CBlocks.DECOR_SLAB, CBlocks.DECOR_SLAB, CBlocks.DECOR_DOUBLE_SLAB)
				.setHasSubtypes(true).setRegistryName(CBlocks.DECOR_SLAB.getRegistryName());
		CItems.itemBlocks.add(dungeonslabitem);
		
		Item lithographeneslabitem = new ItemSlab(CBlocks.GLOWING_DECOR_SLAB, CBlocks.GLOWING_DECOR_SLAB, CBlocks.GLOWING_DECOR_DOUBLE_SLAB)
				.setHasSubtypes(true).setRegistryName(CBlocks.GLOWING_DECOR_SLAB.getRegistryName());
		CItems.itemBlocks.add(lithographeneslabitem);
	}

	private static void register(IForgeRegistry<Block> registry, Block block, Class<? extends ItemBlock> item, String name) {
		block.setUnlocalizedName("correlated."+name);
		block.setCreativeTab(Correlated.CREATIVE_TAB);
		block.setRegistryName(name);
		registry.register(block);
		if (item != null) {
			try {
				ItemBlock ib = item.getConstructor(Block.class).newInstance(block);
				ib.setRegistryName(name);
				CItems.itemBlocks.add(ib);
			} catch (Exception e1) {
				Throwables.propagate(e1);
			}
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
