package com.elytradev.correlated.init;

import com.elytradev.correlated.crafting.DriveRecipe;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.oredict.RecipeSorter.Category;

public class CRecipes {

	public static void register() {
		RecipeSorter.register("correlated:drive", DriveRecipe.class, Category.SHAPED, "after:minecraft:shaped");
		
		
		// 1MiB Drive
		GameRegistry.addRecipe(new DriveRecipe(CStacks.drive1MiB(),
				"III",
				"IOI",
				"IoI",
				
				'I', COres.INGOT_IRON,
				'O', COres.LUMINOUS_PEARL_OR_DATA_CORE,
				'o', CStacks.drivePlatterCeramic()
				));

		// 4MiB Drive
		GameRegistry.addRecipe(new DriveRecipe(CStacks.drive4MiB(),
				"III",
				"oOo",
				"IoI",
				
				'I', COres.INGOT_IRON,
				'O', COres.LUMINOUS_PEARL_OR_DATA_CORE,
				'o', CStacks.drivePlatterCeramic()
				));

		// 16MiB Drive
		GameRegistry.addRecipe(new DriveRecipe(CStacks.drive16MiB(),
				"III",
				"dOd",
				"IoI",
				
				'I', COres.INGOT_IRON,
				'd', COres.GEM_DIAMOND,
				'O', COres.LUMINOUS_PEARL_OR_DATA_CORE,
				'o', CStacks.drivePlatterMetallic()
				));
		// 64MiB Drive
		GameRegistry.addRecipe(new DriveRecipe(CStacks.drive64MiB(),
				"doI",
				"oOo",
				"Iod",
				
				'd', COres.GEM_DIAMOND,
				'O', COres.LUMINOUS_PEARL_OR_DATA_CORE,
				'o', CStacks.drivePlatterMetallic(),
				'I', COres.INGOT_IRON
				));
		// 128MiB Drive
		GameRegistry.addRecipe(new DriveRecipe(CStacks.drive128MiB(),
				"doo",
				"oOo",
				"ood",
				
				'd', COres.BLOCK_DIAMOND,
				'O', COres.LUMINOUS_PEARL_OR_DATA_CORE,
				'o', CStacks.drivePlatterMetallic()
				));

		// Void Drive
		GameRegistry.addRecipe(new DriveRecipe(CStacks.driveVoid(),
				"###",
				"#O#",
				"###",
				
				'O', CStacks.luminousPearl(),
				'#', COres.OBSIDIAN
				));

		
		// 1KiB Memory
		GameRegistry.addRecipe(new DriveRecipe(CStacks.memory1KiB(),
				"iii",
				"gOg",
				
				'O', CStacks.luminousPearl(),
				'i', COres.INGOT_IRON,
				'g', COres.INGOT_GOLD
				));
		
		// 4KiB Memory
		GameRegistry.addRecipe(new DriveRecipe(CStacks.memory4KiB(),
				"iii",
				"dOd",
				
				'O', CStacks.luminousPearl(),
				'i', COres.INGOT_IRON,
				'd', COres.GEM_DIAMOND
				));
		
		

		// Enderic Processor
		if (CConfig.easyProcessors) {
			GameRegistry.addRecipe(new ShapedOreRecipe(CStacks.processor(),
					"qdq",
					"gog",
					"qdq",
					
					'q', COres.GEM_QUARTZ,
					'g', COres.INGOT_GOLD,
					'd', COres.GEM_DIAMOND,
					'o', Items.ENDER_PEARL
					));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(CStacks.processor(2),
					"qoq",
					"gpg",
					"qdq",
					
					'q', COres.GEM_QUARTZ,
					'g', COres.INGOT_GOLD,
					'd', COres.GEM_DIAMOND,
					'p', CStacks.processor(),
					'o', Items.ENDER_PEARL
					));
		}

		// Luminous Pearl
		GameRegistry.addRecipe(new ShapelessOreRecipe(CStacks.luminousPearl(),
				Items.ENDER_PEARL, COres.DUST_GLOWSTONE));

		// Ceramic Drive Platter
		GameRegistry.addRecipe(new ShapedOreRecipe(CStacks.drivePlatterCeramic(),
				" B ",
				"BiB",
				" B ",
				
				'B', COres.INGOT_BRICK,
				'i', COres.INGOT_IRON
				));

		// Metallic Drive Platter
		GameRegistry.addRecipe(new ShapedOreRecipe(CStacks.drivePlatterMetallic(),
				"ioi",
				"oIo",
				"ioi",
				
				'o', CStacks.drivePlatterCeramic(),
				'i', COres.INGOT_IRON,
				'I', COres.BLOCK_IRON
				));

		// Drive Bay
		GameRegistry.addRecipe(new ShapedOreRecipe(CBlocks.DRIVE_BAY,
				"iii",
				" p ",
				"iii",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.processor()
				));
		
		// Memory Bay
		GameRegistry.addRecipe(new ShapedOreRecipe(CBlocks.MEMORY_BAY,
				"iii",
				"gpg",
				"iii",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.processor(),
				'g', COres.NUGGET_GOLD
				));

		// Controller
		GameRegistry.addRecipe(new ShapedOreRecipe(CBlocks.CONTROLLER,
				"ioi",
				"opo",
				"ioi",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.processor(),
				'o', CStacks.luminousPearl()
				));

		// Terminal
		GameRegistry.addRecipe(new ShapedOreRecipe(CBlocks.TERMINAL,
				"iii",
				"ooo",
				"ipi",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.processor(),
				'o', CStacks.luminousPearl()
				));

		// Interface
		GameRegistry.addRecipe(new ShapedOreRecipe(CBlocks.INTERFACE,
				"igi",
				"gog",
				"igi",
				
				'i', COres.INGOT_IRON,
				'g', COres.INGOT_GOLD,
				'o', CStacks.luminousPearl()
				));
		
		// Luminous Torch
		GameRegistry.addRecipe(new ShapedOreRecipe(CStacks.luminousTorch(),
				"o",
				"i",
				"i",
				
				'i', COres.INGOT_IRON,
				'o', CStacks.luminousPearl()
				));
		
		// Microwave Beam
		GameRegistry.addRecipe(new ShapedOreRecipe(CStacks.microwaveBeam(),
				" | ",
				"___",
				"idi",
				
				'i', COres.INGOT_IRON,
				'd', COres.GEM_DIAMOND,
				'|', CStacks.luminousTorch(),
				'_', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
				));
		
		// Optical Receiver
		GameRegistry.addRecipe(new ShapedOreRecipe(CStacks.opticalReceiver(),
				"igi",
				"gpg",
				"igi",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.luminousPearl(),
				'g', COres.BLOCK_GLASS
				));
		
		// Beacon Lens
		GameRegistry.addRecipe(new ShapedOreRecipe(CStacks.beaconLens(),
				"ipi",
				"pgp",
				"ipi",
				
				'i', COres.INGOT_IRON,
				'p', CStacks.luminousPearl(),
				'g', COres.PANE_GLASS
				));
		
		// Decorative Blocks
		GameRegistry.addRecipe(new ShapedOreRecipe(CStacks.plating(64),
				"III",
				"IoI",
				"III",
				
				'I', COres.INGOT_IRON,
				'o', CStacks.luminousPearl()));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(CStacks.lantern(16),
				"oo",
				"oo",
				
				'o', CStacks.volatileDust()));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CBlocks.PLATING_STAIRS, 4),
				"#  ",
				"## ",
				"###",
				
				'#', CStacks.plating()));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CBlocks.PLATING_STAIRS, 4),
				"  #",
				" ##",
				"###",
				
				'#', CStacks.plating()));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CBlocks.DECOR_SLAB, 6, 7),
				"###",
				
				'#', CStacks.plating()));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CBlocks.GLOWING_DECOR_SLAB, 6, 4),
				"###",
				
				'#', CStacks.lantern()));
		
		// Wireless Terminal
		GameRegistry.addRecipe(new ShapelessOreRecipe(CItems.HANDHELD_TERMINAL,
				CBlocks.TERMINAL
				));
		
		// Wireless Terminal back to Terminal
		GameRegistry.addRecipe(new ShapelessOreRecipe(CBlocks.TERMINAL,
				CItems.HANDHELD_TERMINAL
				));
		
		// Reset Microwave Beam
		GameRegistry.addRecipe(new ShapelessOreRecipe(CBlocks.WIRELESS,
				CBlocks.WIRELESS
				));
		
		// Weldthrower Fuel
		GameRegistry.addShapelessRecipe(CStacks.volatileDust(4), CStacks.luminousPearl());
		
		// Weldthrower
		GameRegistry.addRecipe(new ShapedOreRecipe(CItems.WELDTHROWER,
				"i  ",
				"ti_",
				"  i",
				'i', COres.INGOT_IRON,
				'_', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
				't', CStacks.luminousTorch()
				));
		
	}

}
