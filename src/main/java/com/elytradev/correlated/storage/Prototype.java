package com.elytradev.correlated.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import net.minecraft.item.ItemStack;

/**
 * Represents an item "prototype", which is to say a stack of size one.
 * Implements equals and hashCode.
 */
public final class Prototype {

	private final ItemStack stack;
	
	public Prototype(@Nonnull ItemStack stack) {
		Preconditions.checkNotNull(stack);
		this.stack = stack;
	}
	
	public ItemStack getStack() {
		return stack;
	}
	
	public ItemStack copy(int count) {
		ItemStack copy = stack.copy();
		copy.setCount(count);
		return copy;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hashCode(stack);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Prototype that = (Prototype) obj;
		if (this.stack == null) {
			if (that.stack != null) {
				return false;
			}
		} else if (!equals(this.stack, that.stack)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		// using a quick and dirty format string for clarity
		String format = "{name}@{meta}{nbt} x{count}";
		
		return format
				.replace("{name}", getStack().getItem().getRegistryName().toString())
				.replace("{meta}", Integer.toString(getStack().getMetadata()))
				.replace("{nbt}", (stack.hasTagCompound() ? stack.getTagCompound().toString() : ""))
				.replace("{count}", Integer.toString(getStack().getCount()));
	}

	public static int hashCode(@Nullable ItemStack is) {
		// intentionally excludes quantity
		
		// excludes capabilities, due to there being no good way to get
		// a capability hashcode - it'll have to collide and get
		// resolved in equals. oh well.
		if (is == null) return 0;
		int res = 1;
		if (is.hasTagCompound()) {
			res = (31 * res) + is.getTagCompound().hashCode();
		} else {
			res *= 31;
		}
		res = (31 * res) + is.getItem().hashCode();
		res = (31 * res) + is.getMetadata();
		return res;
	}

	public static boolean equals(@Nullable ItemStack o1, @Nullable ItemStack o2) {
		// also intentionally excludes quantity
		if (o1 == o2) return true;
		if (o1 == null || o2 == null) return false;
		if (o1.hasTagCompound() != o2.hasTagCompound()) return false;
		if (o1.getItem() != o2.getItem()) return false;
		if (o1.getMetadata() != o2.getMetadata()) return false;
		if (!Objects.equal(o1.getTagCompound(), o2.getTagCompound())) return false;
		if (!o1.areCapsCompatible(o2)) return false;
		return true;
	}
	
	
	
}
