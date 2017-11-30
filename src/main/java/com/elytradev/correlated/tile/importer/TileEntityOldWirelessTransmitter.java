package com.elytradev.correlated.tile.importer;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.ImportMode;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CStacks;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TileEntityOldWirelessTransmitter extends TileEntityOldWirelessEndpoint {

	@Override
	protected void doImport() {
		if (CConfig.importMode != ImportMode.REFUND_ALL) {
			CLog.info("Skipping refunding of ingredients for old wireless transmitter at {}, {}, {}", getPos().getX(), getPos().getY(), getPos().getZ());
			substitute(Blocks.AIR.getDefaultState(), null, false);
			return;
		}
		TileEntityImporterChest teic = new TileEntityImporterChest();
		teic.addItemToNetwork(new ItemStack(Items.IRON_INGOT, 7));
		teic.addItemToNetwork(CStacks.luminousPearl());
		teic.addItemToNetwork(CStacks.processor());
		CLog.info("Refunding ingredients for old wireless transmitter at {}, {}, {}", getPos().getX(), getPos().getY(), getPos().getZ());
		substitute(CBlocks.IMPORTER_CHEST.getDefaultState(), teic, false);
	}
	
}
