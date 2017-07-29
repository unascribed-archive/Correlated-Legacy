package com.elytradev.correlated.storage;

import java.util.Date;

import net.minecraft.item.ItemStack;

public class NetworkType {

	private ItemStack stack;
	private long lastModified;
	
	public NetworkType(ItemStack stack, long lastModified) {
		this.setStack(stack);
		this.setLastModified(lastModified);
	}
	
	@Override
	public String toString() {
		return getStack().getItem().getRegistryName()+"@"+getStack().getMetadata()+" x"+getStack().getCount()+" modified "+new Date(getLastModified());
	}

	public ItemStack getStack() {
		return stack;
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	
	public static NetworkType createNoTime(ItemStack is) {
		return new NetworkType(is, 0L);
	}
	
}
