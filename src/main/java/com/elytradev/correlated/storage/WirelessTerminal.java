package com.elytradev.correlated.storage;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.helper.ItemStacks;
import com.elytradev.correlated.item.ItemWirelessTerminal;
import com.elytradev.correlated.network.ChangeAPNMessage;
import com.elytradev.correlated.wifi.Station;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants.NBT;

public class WirelessTerminal implements ITerminal {
	private World world;
	private EntityPlayer player;
	private ItemWirelessTerminal iwt;
	private ItemStack stack = ItemStack.EMPTY;
	
	private String apn;
	
	public WirelessTerminal(World world, EntityPlayer player, ItemWirelessTerminal iwt, ItemStack stack) {
		this.world = world;
		this.player = player;
		this.iwt = iwt;
		this.stack = stack;
		this.apn = stack.getTagCompound().getString("APN");
	}

	@Override
	public UserPreferences getPreferences(EntityPlayer player) {
		String id = player.getGameProfile().getId().toString();
		NBTTagCompound prefs = stack.getOrCreateSubCompound("Preferences");
		if (!prefs.hasKey(id, NBT.TAG_COMPOUND)) {
			prefs.setTag(id, new NBTTagCompound());
		}
		return new NBTUserPreferences(prefs.getCompoundTag(id));
	}

	@Override
	public IDigitalStorage getStorage() {
		return null;
	}

	@Override
	public boolean hasStorage() {
		return false;
	}

	@Override
	public boolean hasMaintenanceSlot() {
		return false;
	}
	
	@Override
	public void setMaintenanceSlotContent(ItemStack stack) {
		
	}
	
	@Override
	public ItemStack getMaintenanceSlotContent() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canContinueInteracting(EntityPlayer player) {
		return true;
	}

	@Override
	public void markUnderlyingStorageDirty() {
	}
	
	@Override
	public boolean allowAPNSelection() {
		return true;
	}
	
	@Override
	public int getSignalStrength() {
		Chunk c = player.world.getChunkFromBlockCoords(player.getPosition());
		double minDist = Double.POSITIVE_INFINITY;
		Station closest = null;
		for (Station s : Correlated.getDataFor(player.world).getWirelessManager().allStationsInChunk(c)) {
			if (s.getAPNs().contains(apn) && s.isInRange(player)) {
				double dist = s.distanceTo(player);
				if (dist < minDist) {
					minDist = dist;
					closest = s;
				}
			}
		}
		if (closest != null) {
			double div = 1-(minDist/closest.getRadius());
			return (int)(Math.log10(div*100)*2.5);
		}
		return 0;
	}
	
	@Override
	public void setAPN(String apn) {
		if (world.isRemote) {
			new ChangeAPNMessage(apn).sendToServer();
		} else {
			ItemStacks.ensureHasTag(stack);
			stack.getTagCompound().setString("APN", apn);
			this.apn = apn;
		}
	}

}
