package io.github.elytra.copo.item;

import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;

public class ItemCoPoRecord extends ItemRecord {

	public ItemCoPoRecord(String name, SoundEvent sound) {
		super("correlatedpotentialistics." + name, sound);
	}

}
