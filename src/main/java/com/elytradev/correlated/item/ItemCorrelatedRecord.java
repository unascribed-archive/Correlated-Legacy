package com.elytradev.correlated.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;

public class ItemCorrelatedRecord extends ItemRecord {

	public ItemCorrelatedRecord(String name, SoundEvent sound) {
		super("correlated." + name, sound);
	}
	
	@Override
	public Item setUnlocalizedName(String unlocalizedName) {
		// This fixes a crash for some reason?????
		return super.setUnlocalizedName(unlocalizedName);
	}

}
