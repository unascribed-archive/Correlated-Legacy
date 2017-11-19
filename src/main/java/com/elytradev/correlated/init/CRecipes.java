package com.elytradev.correlated.init;

import com.elytradev.correlated.crafting.DriveRecipe;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

public class CRecipes {

	@SubscribeEvent
	public static void register(RegistryEvent.Register<IRecipe> e) {
		//RecipeSorter.register("correlated:drive", DriveRecipe.class, Category.SHAPED, "after:minecraft:shaped");
		
		
		// 1MiB Drive
		addRecipe(e.getRegistry(), new DriveRecipe(null, CStacks.drive1MiB(),
				"III",
				"IOI",
				"IoI",
				
				'I', COres.INGOT_IRON,
				'O', COres.LUMINOUS_PEARL_OR_DATA_CORE,
				'o', CStacks.drivePlatterCeramic()
				), "drive_1mib");

		// 4MiB Drive
		addRecipe(e.getRegistry(), new DriveRecipe(null, CStacks.drive4MiB(),
				"III",
				"oOo",
				"IoI",
				
				'I', COres.INGOT_IRON,
				'O', COres.LUMINOUS_PEARL_OR_DATA_CORE,
				'o', CStacks.drivePlatterCeramic()
				), "drive_4mib");

		// 16MiB Drive
		addRecipe(e.getRegistry(), new DriveRecipe(null, CStacks.drive16MiB(),
				"III",
				"dOd",
				"IoI",
				
				'I', COres.INGOT_IRON,
				'd', COres.GEM_DIAMOND,
				'O', COres.LUMINOUS_PEARL_OR_DATA_CORE,
				'o', CStacks.drivePlatterMetallic()
				), "drive_16mib");
		// 64MiB Drive
		addRecipe(e.getRegistry(), new DriveRecipe(null, CStacks.drive64MiB(),
				"doI",
				"oOo",
				"Iod",
				
				'd', COres.GEM_DIAMOND,
				'O', COres.LUMINOUS_PEARL_OR_DATA_CORE,
				'o', CStacks.drivePlatterMetallic(),
				'I', COres.INGOT_IRON
				), "drive_64mib");
		// 128MiB Drive
		addRecipe(e.getRegistry(), new DriveRecipe(null, CStacks.drive128MiB(),
				"doo",
				"oOo",
				"ood",
				
				'd', COres.BLOCK_DIAMOND,
				'O', COres.LUMINOUS_PEARL_OR_DATA_CORE,
				'o', CStacks.drivePlatterMetallic()
				), "drive_128mib");

		// Void Drive
		addRecipe(e.getRegistry(), new DriveRecipe(null, CStacks.driveVoid(),
				"###",
				"#O#",
				"###",
				
				'O', CStacks.luminousPearl(),
				'#', COres.OBSIDIAN
				), "drive_void");

		
		// 1KiB Memory
		addRecipe(e.getRegistry(), new DriveRecipe(null, CStacks.memory1KiB(),
				"iii",
				"gOg",
				
				'O', CStacks.luminousPearl(),
				'i', COres.INGOT_IRON,
				'g', COres.INGOT_GOLD
				), "memory_1kib");
		
		// 4KiB Memory
		addRecipe(e.getRegistry(), new DriveRecipe(null, CStacks.memory4KiB(),
				"iii",
				"dOd",
				
				'O', CStacks.luminousPearl(),
				'i', COres.INGOT_IRON,
				'd', COres.GEM_DIAMOND
				), "memory_4kib");
		
		

		// Enderic Processor
		if (CConfig.easyProcessors) {
			addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CStacks.processor(),
					"qdq",
					"gog",
					"qdq",
					
					'q', COres.GEM_QUARTZ,
					'g', COres.INGOT_GOLD,
					'd', COres.GEM_DIAMOND,
					'o', Items.ENDER_PEARL
					), "processor");
		} else {
			addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CStacks.processor(2),
					"qoq",
					"gpg",
					"qdq",
					
					'q', COres.GEM_QUARTZ,
					'g', COres.INGOT_GOLD,
					'd', COres.GEM_DIAMOND,
					'p', CStacks.processor(),
					'o', Items.ENDER_PEARL
					), "processor_dupe");
		}

		// Luminous Pearl
		addRecipe(e.getRegistry(), new ShapelessOreRecipe(null, CStacks.luminousPearl(),
				Items.ENDER_PEARL, COres.DUST_GLOWSTONE), "luminous_pearl");

		// Ceramic Drive Platter
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CStacks.drivePlatterCeramic(),
				" B ",
				"BiB",
				" B ",
				
				'B', COres.INGOT_BRICK,
				'i', COres.INGOT_IRON
				), "drive_platter_ceramic");

		// Metallic Drive Platter
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CStacks.drivePlatterMetallic(),
				"ioi",
				"oIo",
				"ioi",
				
				'o', CStacks.drivePlatterCeramic(),
				'i', COres.INGOT_IRON,
				'I', COres.BLOCK_IRON
				), "drive_platter_metallic");

		// Drive Bay
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CBlocks.DRIVE_BAY,
				"iii",
				" p ",
				"iii",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.processor()
				), "drive_bay");
		
		// Memory Bay
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CBlocks.MEMORY_BAY,
				"iii",
				"gpg",
				"iii",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.processor(),
				'g', COres.NUGGET_GOLD
				), "memory_bay");

		// Controller
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CBlocks.CONTROLLER,
				"ioi",
				"opo",
				"ioi",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.processor(),
				'o', CStacks.luminousPearl()
				), "controller");

		// Terminal
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CBlocks.TERMINAL,
				"iii",
				"ooo",
				"ipi",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.processor(),
				'o', CStacks.luminousPearl()
				), "terminal");

		// Interface
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CBlocks.INTERFACE,
				"igi",
				"gog",
				"igi",
				
				'i', COres.INGOT_IRON,
				'g', COres.INGOT_GOLD,
				'o', CStacks.luminousPearl()
				), "interface");
		
		// Luminous Torch
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CStacks.luminousTorch(),
				"o",
				"i",
				"i",
				
				'i', COres.INGOT_IRON,
				'o', CStacks.luminousPearl()
				), "luminous_torch");
		
		// Microwave Beam
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CStacks.microwaveBeam(),
				" | ",
				"___",
				"idi",
				
				'i', COres.INGOT_IRON,
				'd', COres.GEM_DIAMOND,
				'|', CStacks.luminousTorch(),
				'_', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
				), "microwave_beam");
		
		// Optical Receiver
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CStacks.opticalTransceiver(),
				"igi",
				"gpg",
				"igi",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.luminousPearl(),
				'g', COres.BLOCK_GLASS
				), "optical");
		
		// Beacon Lens
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CStacks.beaconLens(),
				"ipi",
				"pgp",
				"ipi",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.luminousPearl(),
				'g', COres.PANE_GLASS
				), "beacon_lens");
		
		// Decorative Blocks
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CStacks.plating(64),
				"III",
				"IoI",
				"III",
				
				'I', COres.INGOT_IRON,
				'o', CStacks.luminousPearl()), "plating");
		
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CStacks.lantern(16),
				"oo",
				"oo",
				
				'o', CStacks.volatileDust()), "lantern");
		
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, new ItemStack(CBlocks.PLATING_STAIRS, 4),
				"#  ",
				"## ",
				"###",
				
				'#', CStacks.plating()), "plating_stairs");
		
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, new ItemStack(CBlocks.PLATING_STAIRS, 4),
				"  #",
				" ##",
				"###",
				
				'#', CStacks.plating()), "plating_stairs_flipped");
		
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, new ItemStack(CBlocks.DECOR_SLAB, 6, 7),
				"###",
				
				'#', CStacks.plating()), "plating_slab");
		
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, new ItemStack(CBlocks.GLOWING_DECOR_SLAB, 6, 4),
				"###",
				
				'#', CStacks.lantern()), "lantern_slab");
		
		// Wireless Terminal
		addRecipe(e.getRegistry(), new ShapelessOreRecipe(null, CItems.HANDHELD_TERMINAL,
				CBlocks.TERMINAL
				), "handheld_terminal");
		
		// Wireless Terminal back to Terminal
		addRecipe(e.getRegistry(), new ShapelessOreRecipe(null, CBlocks.TERMINAL,
				CItems.HANDHELD_TERMINAL
				), "handheld_terminal_revert");
		
		// Reset Microwave Beam
		addRecipe(e.getRegistry(), new ShapelessOreRecipe(null, CBlocks.WIRELESS,
				CBlocks.WIRELESS
				), "microwave_beam_reset");
		
		// Weldthrower Fuel
		addRecipe(e.getRegistry(), new ShapelessOreRecipe(null,
				CStacks.volatileDust(4), CStacks.luminousPearl()), "volatile_dust");
		
		// Weldthrower
		addRecipe(e.getRegistry(), new ShapedOreRecipe(null, CItems.WELDTHROWER,
				"i  ",
				"ti_",
				"  i",
				'i', COres.INGOT_IRON,
				'_', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
				't', CStacks.luminousTorch()
				), "weldthrower");
		
	}

	private static void addRecipe(IForgeRegistry<IRecipe> registry, IRecipe ir, String name) {
		registry.register(ir.setRegistryName(new ResourceLocation("correlated", name)));
	}

}
