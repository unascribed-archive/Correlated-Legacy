package io.github.elytra.copo.compat;

import io.github.elytra.copo.block.BlockController;
import io.github.elytra.copo.block.BlockDriveBay;
import io.github.elytra.copo.block.BlockInterface;
import io.github.elytra.copo.block.BlockVT;
import io.github.elytra.copo.block.BlockWirelessEndpoint;
import io.github.elytra.copo.tile.TileEntityNetworkMember;
import mcp.mobius.waila.api.impl.ModuleRegistrar;

public class WailaCompatibility {
	public static void init() {
		CoPoWailaProvider provider = new CoPoWailaProvider();
		ModuleRegistrar.instance().registerNBTProvider(provider, TileEntityNetworkMember.class);
		ModuleRegistrar.instance().registerStackProvider(provider, BlockController.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockController.class);

		ModuleRegistrar.instance().registerStackProvider(provider, BlockDriveBay.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockDriveBay.class);

		ModuleRegistrar.instance().registerStackProvider(provider, BlockVT.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockVT.class);

		ModuleRegistrar.instance().registerStackProvider(provider, BlockInterface.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockInterface.class);
		
		ModuleRegistrar.instance().registerStackProvider(provider, BlockWirelessEndpoint.class);
		ModuleRegistrar.instance().registerBodyProvider(provider, BlockWirelessEndpoint.class);
	}
}
