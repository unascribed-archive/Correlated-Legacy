package com.elytradev.correlated.inventory;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import com.elytradev.correlated.storage.NetworkType;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum SortMode {
	QUANTITY((a, b) -> {
		int quantityComp = Ints.compare(a.getStack().getCount(), b.getStack().getCount());
		if (quantityComp != 0) return quantityComp;
		return Collator.getInstance().compare(a.getStack().getDisplayName(), b.getStack().getDisplayName());
	}),
	MOD_MINECRAFT_FIRST((a, b) -> {
		String modA = getModId(a.getStack());
		String modB = getModId(b.getStack());
		boolean aMinecraft = "minecraft".equals(modA);
		boolean bMinecraft = "minecraft".equals(modB);
		if (aMinecraft || bMinecraft && aMinecraft != bMinecraft) return Booleans.compare(aMinecraft, bMinecraft);
		int modComp = Collator.getInstance().compare(modA, modB);
		if (modComp != 0) return modComp;
		return Collator.getInstance().compare(a.getStack().getDisplayName(), b.getStack().getDisplayName());
	}),
	MOD((a, b) -> {
		int modComp = Collator.getInstance().compare(getModId(a.getStack()), getModId(b.getStack()));
		if (modComp != 0) return modComp;
		return Collator.getInstance().compare(a.getStack().getDisplayName(), b.getStack().getDisplayName());
	}),
	NAME((a, b) -> Collator.getInstance().compare(a.getStack().getDisplayName(), b.getStack().getDisplayName())),
	LAST_MODIFIED((a, b) -> Longs.compare(a.getLastModified(), b.getLastModified()));
	public final Comparator<NetworkType> comparator;
	public final String lowerName = name().toLowerCase(Locale.ROOT);
	private SortMode(Comparator<NetworkType> comparator) {
		this.comparator = comparator;
	}

	private static String getModId(ItemStack is) {
		return Item.REGISTRY.getNameForObject(is.getItem()).getResourceDomain();
	}
}