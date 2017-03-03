package com.elytradev.correlated.tile;

import com.elytradev.correlated.Correlated;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TileEntityOldWirelessReceiver extends TileEntityOldWirelessEndpoint {

	@Override
	protected void doImport() {
		if (!Correlated.inst.refundComponents) {
			Correlated.log.info("Skipping refunding of ingredients for old wireless receiver at {}, {}, {}", getPos().getX(), getPos().getY(), getPos().getZ());
			substitute(Blocks.AIR.getDefaultState(), null, false);
			return;
		}
		TileEntityImporterChest teic = new TileEntityImporterChest();
		teic.addItemToNetwork(new ItemStack(Items.IRON_INGOT, 10));
		// luminous pearl
		teic.addItemToNetwork(new ItemStack(Correlated.misc, 1, 3));
		// processor
		teic.addItemToNetwork(new ItemStack(Correlated.misc, 1, 0));
		Correlated.log.info("Refunding ingredients for old wireless receiver at {}, {}, {}", getPos().getX(), getPos().getY(), getPos().getZ());
		substitute(Correlated.importer_chest.getDefaultState(), teic, false);
	}
	
}
