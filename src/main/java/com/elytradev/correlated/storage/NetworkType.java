package com.elytradev.correlated.storage;

import java.util.Date;

import javax.annotation.Nonnull;

import com.elytradev.correlated.C28n;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;

import net.minecraft.item.ItemStack;

public class NetworkType {

	@Nonnull
	private ItemStack stack;
	private long lastModified;
	
	public NetworkType(@Nonnull ItemStack stack, long lastModified) {
		this.setStack(stack);
		this.setLastModified(lastModified);
	}
	
	@Override
	public String toString() {
		// using a quick and dirty format string for clarity
		String format = "{name}@{meta}{nbt} x{count} modified {date}";
		
		return format
				.replace("{name}", getStack().getItem().getRegistryName().toString())
				.replace("{meta}", Integer.toString(getStack().getMetadata()))
				.replace("{nbt}", (stack.hasTagCompound() ? stack.getTagCompound().toString() : ""))
				.replace("{count}", Integer.toString(getStack().getCount()))
				.replace("{date}", new Date(getLastModified()).toString());
	}

	@Nonnull
	public ItemStack getStack() {
		return stack;
	}
	
	@Nonnull
	public Prototype getPrototype() {
		return new Prototype(stack);
	}

	public void setStack(@Nonnull ItemStack stack) {
		Preconditions.checkNotNull(stack);
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
	
	private static final long MILLISECOND = 1;
	private static final long SECOND = MILLISECOND*1000;
	private static final long MINUTE = SECOND*60;
	private static final long HOUR = MINUTE*60;
	private static final long DAY = HOUR*24;
	private static final long WEEK = DAY*7;
	private static final long YEAR = DAY*365;
	private static final long DECADE = YEAR*10;
	
	public static String formatLastModified(long lastModified) {
		if (lastModified == 0) {
			return C28n.format("gui.correlated.last_modified.epoch");
		}
		long diff = System.currentTimeMillis()-lastModified;
		if (diff < 0) {
			return C28n.format("gui.correlated.last_modified.future");
		}
		if (diff < 5000) {
			return C28n.format("gui.correlated.last_modified.just_now");
		}
		
		if (diff < MINUTE) {
			return C28n.formatPlural("gui.correlated.last_modified.seconds", diff/SECOND);
		} else if (diff < HOUR) {
			return C28n.formatPlural("gui.correlated.last_modified.minutes", diff/MINUTE);
		} else if (diff < DAY) {
			return C28n.formatPlural("gui.correlated.last_modified.hours", diff/HOUR);
		} else if (diff < WEEK) {
			return C28n.formatPlural("gui.correlated.last_modified.days", diff/DAY);
		} else if (diff < YEAR) {
			return C28n.formatPlural("gui.correlated.last_modified.weeks", diff/WEEK);
		} else if (diff < DECADE) {
			return C28n.formatPlural("gui.correlated.last_modified.years", diff/YEAR);
		} else {
			return C28n.format("gui.correlated.last_modified.long_time");
		}
	}
	
	@Override
	public int hashCode() {
		int res = 1;
		res = (31 * res) + Longs.hashCode(lastModified);
		res = (31 * res) + Prototype.hashCode(stack);
		res = (31 * res) + stack.getCount();
		return res;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		NetworkType that = (NetworkType) obj;
		if (this.lastModified != that.lastModified) return false;
		if (!Prototype.equals(this.stack, that.stack)) return false;
		if (this.stack.getCount() != that.stack.getCount()) return false;
		
		return true;
	}
	
}
