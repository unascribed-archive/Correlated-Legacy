package io.github.elytra.copo.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public interface ITerminal {

	UserPreferences getPreferences(EntityPlayer player);
	IDigitalStorage getStorage();
	boolean hasStorage();
	boolean supportsDumpSlot();
	IInventory getDumpSlotInventory();
	boolean canContinueInteracting(EntityPlayer player);
	void markUnderlyingStorageDirty();

}

