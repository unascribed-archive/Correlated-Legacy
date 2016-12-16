package io.github.elytra.correlated.item;

import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;

public class ItemCorrelatedRecord extends ItemRecord {

	public ItemCorrelatedRecord(String name, SoundEvent sound) {
		super("correlated." + name, sound);
	}

}
