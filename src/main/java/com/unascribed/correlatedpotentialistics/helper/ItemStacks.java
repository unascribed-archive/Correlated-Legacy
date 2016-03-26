package com.unascribed.correlatedpotentialistics.helper;

import com.google.common.base.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class ItemStacks {
	public static Optional<Integer> getInteger(ItemStack stack, String key) {
		if (!stack.hasTagCompound()) return Optional.absent();
		if (!stack.getTagCompound().hasKey(key, NBT.TAG_ANY_NUMERIC)) return Optional.absent();
		return Optional.of(stack.getTagCompound().getInteger(key));
	}
	
	public static Optional<Boolean> getBoolean(ItemStack stack, String key) {
		if (!stack.hasTagCompound()) return Optional.absent();
		if (!stack.getTagCompound().hasKey(key, NBT.TAG_ANY_NUMERIC)) return Optional.absent();
		return Optional.of(stack.getTagCompound().getBoolean(key));
	}
	
	public static NBTTagCompound getSubCompound(ItemStack stack, String key) {
		ensureHasTag(stack);
		if (!stack.getTagCompound().hasKey(key, NBT.TAG_COMPOUND)) {
			stack.getTagCompound().setTag(key, new NBTTagCompound());
		}
		return stack.getTagCompound().getCompoundTag(key);
	}
	
	public static ItemStack ensureHasTag(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		return stack;
	}

	public static NBTTagList getCompoundList(ItemStack stack, String key) {
		ensureHasTag(stack);
		NBTTagList li = stack.getTagCompound().getTagList(key, NBT.TAG_COMPOUND);
		// to make sure the list we got is actually in the tag
		// Vanilla likes to silently create new orphaned tags when there's a mismatch of any kind.
		stack.getTagCompound().setTag(key, li);
		return li;
	}

	public static <T extends Enum<T>> Optional<T> getEnum(ItemStack stack, String key, Class<T> clazz) {
		if (!stack.hasTagCompound()) return Optional.absent();
		if (!stack.getTagCompound().hasKey(key, NBT.TAG_STRING)) return Optional.absent();
		String str = stack.getTagCompound().getString(key);
		try {
			return Optional.of(Enum.valueOf(clazz, str));
		} catch (IllegalArgumentException|NullPointerException e) {
			e.printStackTrace();
			return Optional.absent();
		}
	}
}
