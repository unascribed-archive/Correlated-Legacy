package com.elytradev.correlated.compat;

import com.elytradev.correlated.block.BlockController;
import com.elytradev.correlated.block.BlockDriveBay;
import com.elytradev.correlated.block.BlockInterface;
import com.elytradev.correlated.block.BlockMemoryBay;
import com.elytradev.correlated.block.BlockTerminal;
import com.elytradev.correlated.block.BlockWirelessEndpoint;
import com.elytradev.correlated.tile.TileEntityNetworkMember;

public class WailaCompatibility {
	public static void init() {
		CorrelatedWailaProvider provider = new CorrelatedWailaProvider();
		/*ModuleRegistrar.instance().registerNBTProvider(provider, TileEntityNetworkMember.class);
		ModuleRegistrar.instance().registerStackProvider(provider, BlockController.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockController.class);

		ModuleRegistrar.instance().registerStackProvider(provider, BlockDriveBay.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockDriveBay.class);
		
		ModuleRegistrar.instance().registerStackProvider(provider, BlockMemoryBay.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockMemoryBay.class);

		ModuleRegistrar.instance().registerStackProvider(provider, BlockTerminal.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockTerminal.class);

		ModuleRegistrar.instance().registerStackProvider(provider, BlockInterface.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockInterface.class);
		
		ModuleRegistrar.instance().registerStackProvider(provider, BlockWirelessEndpoint.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockWirelessEndpoint.class);*/
	}
}
