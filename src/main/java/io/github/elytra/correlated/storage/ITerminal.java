package io.github.elytra.correlated.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public interface ITerminal {

	UserPreferences getPreferences(EntityPlayer player);
	IDigitalStorage getStorage();
	boolean hasStorage();
	boolean supportsDumpSlot();
	IInventory getDumpSlotInventory();
	boolean canContinueInteracting(EntityPlayer player);
	// this ridiculous name is to avoid conflict with markDirty in TileEntity
	void markUnderlyingStorageDirty();

}

