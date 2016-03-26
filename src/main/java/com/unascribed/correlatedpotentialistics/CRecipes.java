package com.unascribed.correlatedpotentialistics;

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
				'o', drivePlatterCeramic
				));
		
		// Void Drive
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.drive, 1, 4),
				"###",
				"#O#",
				"###",
				'O', luminousPearl,
				'#', Blocks.obsidian
				));
		
		
		// Enderic Processor
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CoPo.misc, 2, 0),
				"qoq",
				"gpg",
				"qdq",
				'q', "gemQuartz",
				'g', "ingotGold",
				'd', "gemDiamond",
				'p', processor,
				'o', Items.ender_pearl
				));
		
		// Luminous Pearl
		GameRegistry.addRecipe(new ShapelessOreRecipe(luminousPearl,
				Items.ender_pearl, "dustGlowstone"));
		
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
	}

}
