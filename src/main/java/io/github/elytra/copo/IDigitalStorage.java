package io.github.elytra.copo;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IDigitalStorage {

	int getChangeId();
	List<ItemStack> getTypes();
	ItemStack addItemToNetwork(ItemStack stack);
	ItemStack removeItemsFromNetwork(ItemStack prototype, int amount, boolean b);
	boolean isPowered();
	int getBitsFree();

}
