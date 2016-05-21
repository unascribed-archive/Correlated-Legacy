package io.github.elytra.copo;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class CRecipes {

	public static void register() {
		ItemStack processor = new ItemStack(CoPo.misc, 1, 0);
		ItemStack drivePlatterCeramic = new ItemStack(CoPo.misc, 1, 1);
		ItemStack drivePlatterMetallic = new ItemStack(CoPo.misc, 1, 2);
		ItemStack luminousPearl = new ItemStack(CoPo.misc, 1, 3);
		ItemStack luminousTorch = new ItemStack(CoPo.misc, 1, 4);

		// 1KiB Drive
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.drive, 1, 0),
				"III",
				"IOI",
				"IoI",
				'I', "ingotIron",
				'O', luminousPearl,
				'o', drivePlatterCeramic
				));

		// 4KiB Drive
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.drive, 1, 1),
				"III",
				"oOo",
				"IoI",
				'I', "ingotIron",
				'O', luminousPearl,
				'o', drivePlatterCeramic
				));

		// 16KiB Drive
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.drive, 1, 2),
				"III",
				"dOd",
				"IoI",
				'I', "ingotIron",
				'd', "gemDiamond",
				'O', luminousPearl,
				'o', drivePlatterMetallic
				));
		// 64KiB Drive
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.drive, 1, 3),
				"doO",
				"odo",
				"Ood",
				'd', "gemDiamond",
				'O', luminousPearl,
				'o', drivePlatterMetallic
				));

		// Void Drive
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.drive, 1, 4),
				"###",
				"#O#",
				"###",
				'O', luminousPearl,
				'#', Blocks.OBSIDIAN
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
		GameRegistry.addRecipe(new ShapedOreRecipe(CoPo.vt,
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
				"v",
				'r', new ItemStack(CoPo.wireless_endpoint, 1, 0),
				'v', CoPo.vt
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
