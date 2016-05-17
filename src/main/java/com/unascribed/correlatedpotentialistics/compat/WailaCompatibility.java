package com.unascribed.correlatedpotentialistics.compat;

import com.unascribed.correlatedpotentialistics.block.BlockController;
import com.unascribed.correlatedpotentialistics.block.BlockDriveBay;
import com.unascribed.correlatedpotentialistics.block.BlockInterface;
import com.unascribed.correlatedpotentialistics.block.BlockVT;
import com.unascribed.correlatedpotentialistics.block.BlockWirelessEndpoint;
import com.unascribed.correlatedpotentialistics.tile.TileEntityNetworkMember;

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
