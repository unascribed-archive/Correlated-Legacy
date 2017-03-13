package com.elytradev.correlated.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface ITerminal {

	UserPreferences getPreferences(EntityPlayer player);
	IDigitalStorage getStorage();
	boolean hasStorage();
	boolean canContinueInteracting(EntityPlayer player);
	
	boolean hasMaintenanceSlot();
	ItemStack getMaintenanceSlotContent();
	void setMaintenanceSlotContent(ItemStack stack);
	
	// this ridiculous name is to avoid conflict with markDirty in TileEntity
	void markUnderlyingStorageDirty();
	
	boolean allowAPNSelection();
	void setAPN(String apn);
	String getAPN();
	
	BlockPos getPosition();
	
	int getSignalStrength();
	
}

