package com.elytradev.correlated.storage;

import java.util.Collections;
import java.util.List;

import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.helper.ItemStacks;
import com.elytradev.correlated.item.ItemHandheldTerminal;
import com.elytradev.correlated.network.wireless.ChangeAPNMessage;
import com.elytradev.correlated.wifi.Station;
import com.elytradev.correlated.wifi.WirelessManager;
import com.google.common.base.Strings;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class WirelessTerminal implements ITerminal {
	private World world;
	private EntityPlayer player;
	private ItemHandheldTerminal iwt;
	private ItemStack stack = ItemStack.EMPTY;
	
	public WirelessTerminal(World world, EntityPlayer player, ItemHandheldTerminal iwt, ItemStack stack) {
		this.world = world;
		this.player = player;
		this.iwt = iwt;
		this.stack = stack;
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
		String apn = getAPN();
		if (apn == null) return null;
		Iterable<Station> li = CorrelatedWorldData.getFor(world).getWirelessManager().allStationsInChunk(world.getChunkFromBlockCoords(player.getPosition()));
		for (Station s : li) {
			if (s.getAPNs().contains(apn) && s.isInRange(player.posX, player.posY+player.getEyeHeight(), player.posZ)) {
				List<IDigitalStorage> storages = s.getStorages(apn);
				if (storages.size() == 1) {
					return storages.get(0);
				} else {
					return new CompoundDigitalStorage(storages);
				}
			}
		}
		return null;
	}

	@Override
	public boolean hasStorage() {
		return getStorage() != null;
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
	public int getSignalStrength() {
		WirelessManager wm = CorrelatedWorldData.getFor(player.world).getWirelessManager();
		return wm.getSignalStrength(player.posX, player.posY+player.getEyeHeight(), player.posZ, getAPN());
	}
	
	@Override
	public void setAPN(String apn) {
		if (world.isRemote) {
			new ChangeAPNMessage(apn == null ? Collections.emptyList() : Collections.singleton(apn)).sendToServer();
		} else {
			if (apn == null) {
				if (stack.hasTagCompound()) {
					stack.getTagCompound().removeTag("APN");
				}
			} else {
				ItemStacks.ensureHasTag(stack);
				stack.getTagCompound().setString("APN", apn);
			}
		}
	}
	
	@Override
	public String getAPN() {
		return stack.hasTagCompound() ? Strings.emptyToNull(stack.getTagCompound().getString("APN")) : null;
	}
	
	@Override
	public BlockPos getPosition() {
		return player.getPosition();
	}

}
