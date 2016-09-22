package io.github.elytra.copo.crafting;

import io.github.elytra.copo.CoPo;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class CRecipes {

	public static void register() {
		ItemStack processor = new ItemStack(CoPo.misc, 1, 0);
		ItemStack drivePlatterCeramic = new ItemStack(CoPo.misc, 1, 1);
		ItemStack drivePlatterMetallic = new ItemStack(CoPo.misc, 1, 2);
		ItemStack luminousPearl = new ItemStack(CoPo.misc, 1, 3);
		ItemStack luminousTorch = new ItemStack(CoPo.misc, 1, 4);
		ItemStack dataCore = new ItemStack(CoPo.misc, 1, 8);

		String luminousPearlOrDataCore = "correlatedpotentialistics:INTERNAL_luminousPearlOrDataCore";
		
		OreDictionary.registerOre(luminousPearlOrDataCore, luminousPearl);
		OreDictionary.registerOre(luminousPearlOrDataCore, dataCore);
		
		// 1MiB Drive
		GameRegistry.addRecipe(new DriveRecipe(new ItemStack(CoPo.drive, 1, 0),
				"III",
				"IOI",
				"IoI",
				'I', "ingotIron",
				'O', luminousPearlOrDataCore,
				'o', drivePlatterCeramic
				));

		// 4MiB Drive
		GameRegistry.addRecipe(new DriveRecipe(new ItemStack(CoPo.drive, 1, 1),
				"III",
				"oOo",
				"IoI",
				'I', "ingotIron",
				'O', luminousPearlOrDataCore,
				'o', drivePlatterCeramic
				));

		// 16MiB Drive
		GameRegistry.addRecipe(new DriveRecipe(new ItemStack(CoPo.drive, 1, 2),
				"III",
				"dOd",
				"IoI",
				'I', "ingotIron",
				'd', "gemDiamond",
				'O', luminousPearlOrDataCore,
				'o', drivePlatterMetallic
				));
		// 64MiB Drive
		GameRegistry.addRecipe(new DriveRecipe(new ItemStack(CoPo.drive, 1, 3),
				"doI",
				"oOo",
				"Iod",
				'd', "gemDiamond",
				'O', luminousPearlOrDataCore,
				'o', drivePlatterMetallic,
				'I', "ingotIron"
				));
		// 128MiB Drive
		GameRegistry.addRecipe(new DriveRecipe(new ItemStack(CoPo.drive, 1, 5),
				"doo",
				"oOo",
				"ood",
				'd', "blockDiamond",
				'O', luminousPearlOrDataCore,
				'o', drivePlatterMetallic
				));

		// Void Drive
		GameRegistry.addRecipe(new DriveRecipe(new ItemStack(CoPo.drive, 1, 4),
				"###",
				"#O#",
				"###",
				'O', luminousPearl,
				'#', Blocks.OBSIDIAN
				));

		
		// 1KiB Memory
		GameRegistry.addRecipe(new DriveRecipe(new ItemStack(CoPo.memory, 1, 0),
				"iii",
				"gOg",
				'O', luminousPearl,
				'i', "ingotIron",
				'g', "ingotGold"
				));
		
		// 4KiB Memory
		GameRegistry.addRecipe(new DriveRecipe(new ItemStack(CoPo.memory, 1, 1),
				"iii",
				"dOd",
				'O', luminousPearl,
				'i', "ingotIron",
				'd', "gemDiamond"
				));
		
		

		// Enderic Processor
		if (CoPo.inst.easyProcessors) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.misc, 1, 0),
					"qdq",
					"gog",
					"qdq",
					'q', "gemQuartz",
					'g', "ingotGold",
					'd', "gemDiamond",
					'o', Items.ENDER_PEARL
					));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.misc, 2, 0),
					"qoq",
					"gpg",
					"qdq",
					'q', "gemQuartz",
					'g', "ingotGold",
					'd', "gemDiamond",
					'p', processor,
					'o', Items.ENDER_PEARL
					));
		}

		// Luminous Pearl
		GameRegistry.addRecipe(new ShapelessOreRecipe(luminousPearl,
				Items.ENDER_PEARL, "dustGlowstone"));

		// Ceramic Drive Platter
		GameRegistry.addRecipe(new ShapedOreRecipe(drivePlatterCeramic,
				" B ",
				"BiB",
				" B ",
				'B', "ingotBrick",
				'i', "ingotIron"
				));

		// Metallic Drive Platter
		GameRegistry.addRecipe(new ShapedOreRecipe(drivePlatterMetallic,
				"ioi",
				"oIo",
				"ioi",
				'o', drivePlatterCeramic,
				'i', "ingotIron",
				'I', "blockIron"
				));

		// Drive Bay
		GameRegistry.addRecipe(new ShapedOreRecipe(CoPo.drive_bay,
				"iii",
				" p ",
				"iii",
				'i', "ingotIron",
				'p', processor
				));
		
		// Memory Bay
		GameRegistry.addRecipe(new ShapedOreRecipe(CoPo.memory_bay,
				"iii",
				"gpg",
				"iii",
				'i', "ingotIron",
				'p', processor,
				'g', "nuggetGold"
				));

		// Controller
		GameRegistry.addRecipe(new ShapedOreRecipe(CoPo.controller,
				"ioi",
				"opo",
				"ioi",
				'i', "ingotIron",
				'p', processor,
				'o', luminousPearl
				));

		// Terminal
		GameRegistry.addRecipe(new ShapedOreRecipe(CoPo.terminal,
				"iii",
				"ooo",
				"ipi",
				'i', "ingotIron",
				'p', processor,
				'o', luminousPearl
				));

		// Interface
		GameRegistry.addRecipe(new ShapedOreRecipe(CoPo.iface,
				"igi",
				"gog",
				"igi",
				'i', "ingotIron",
				'g', "ingotGold",
				'o', luminousPearl
				));
		
		// Luminous Torch
		GameRegistry.addRecipe(new ShapedOreRecipe(luminousTorch,
				"o",
				"i",
				"i",
				'i', "ingotIron",
				'o', luminousPearl
				));
		
		// Wireless Receiver
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.wireless_endpoint, 1, 0),
				" t ",
				"___",
				"ipi",
				'_', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
				'i', "ingotIron",
				't', luminousTorch,
				'p', processor
				));
		
		// Wireless Transmitter
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.wireless_endpoint, 1, 1),
				" t ",
				"iii",
				"ipi",
				'i', "ingotIron",
				't', luminousTorch,
				'p', processor
				));
		
		// Wireless Terminal
		GameRegistry.addRecipe(new ShapedOreRecipe(CoPo.wireless_terminal,
				"r",
				"t",
				'r', new ItemStack(CoPo.wireless_endpoint, 1, 0),
				't', CoPo.terminal
				));
		
		// Weldthrower Fuel
		GameRegistry.addShapelessRecipe(new ItemStack(CoPo.misc, 4, 5), luminousPearl);
		
		// Weldthrower
		GameRegistry.addRecipe(new ShapedOreRecipe(CoPo.weldthrower,
				"i  ",
				"ti_",
				"  i",
				'i', "ingotIron",
				'_', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
				't', luminousTorch
				));
		
	}

}
