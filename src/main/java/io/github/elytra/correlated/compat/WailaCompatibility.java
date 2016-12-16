package io.github.elytra.correlated.compat;

import io.github.elytra.correlated.block.BlockController;
import io.github.elytra.correlated.block.BlockDriveBay;
import io.github.elytra.correlated.block.BlockInterface;
import io.github.elytra.correlated.block.BlockMemoryBay;
import io.github.elytra.correlated.block.BlockTerminal;
import io.github.elytra.correlated.block.BlockWirelessEndpoint;
import io.github.elytra.correlated.tile.TileEntityNetworkMember;
import mcp.mobius.waila.api.impl.ModuleRegistrar;

public class WailaCompatibility {
	public static void init() {
		CorrelatedWailaProvider provider = new CorrelatedWailaProvider();
		ModuleRegistrar.instance().registerNBTProvider(provider, TileEntityNetworkMember.class);
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
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockWirelessEndpoint.class);
	}
}
