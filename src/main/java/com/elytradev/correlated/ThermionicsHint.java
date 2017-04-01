package com.elytradev.correlated;

import net.minecraftforge.fml.common.Loader;

public class ThermionicsHint {
	private static final String[] nonsense = {
		"take over the world!",
		"digitize all our items!",
		"corrupt Enderspace, thereby slingshotting us to a glitch dimension!",
		"listen to records nonstop on shuffle!",
		"take over the multiverse!",
		"take over the Nether! Wait, you already did that.",
		"implement a quarry in assembly!",
		"finally implement that server mod!",
	};
	
	static {
		if (!Loader.isModLoaded("thermionics")) throw new AssertionError();
		Correlated.log.info("No, we're going to "+nonsense[(int)(Math.random()*nonsense.length)]);
		if (Loader.isModLoaded("teckle")) {
			try {
				Class.forName("com.elytradev.teckle.CorrelatedHint");
				Correlated.log.info("Teckle, I've explained this to you hundreds of times.");
			} catch (Throwable t) {}
		}
	}
}
