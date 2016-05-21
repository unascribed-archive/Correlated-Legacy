package io.github.elytra.copo.helper;

import java.util.Comparator;

import com.google.common.primitives.Booleans;
import com.google.common.primitives.Ints;

import io.github.elytra.copo.item.ItemDrive;
import net.minecraft.item.ItemStack;

public class DriveComparator implements Comparator<ItemStack> {

	@Override
	public int compare(ItemStack a, ItemStack b) {
		if (a == null || !(a.getItem() instanceof ItemDrive) || b == null || !(b.getItem() instanceof ItemDrive)) {
			boolean aSafe = a != null && a.getItem() instanceof ItemDrive;
			boolean bSafe = b != null && b.getItem() instanceof ItemDrive;
			return Booleans.compare(aSafe, bSafe);
		}
		ItemDrive aI = (ItemDrive)a.getItem();
		ItemDrive bI = (ItemDrive)b.getItem();
		return Ints.compare(aI.getPriority(a).ordinal(), bI.getPriority(b).ordinal());
	}

}
