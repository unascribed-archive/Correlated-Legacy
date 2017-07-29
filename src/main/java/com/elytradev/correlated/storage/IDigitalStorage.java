package com.elytradev.correlated.storage;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.item.ItemStack;

public interface IDigitalStorage {

	int getChangeId();
	void getTypes(Set<IDigitalStorage> alreadyChecked, List<NetworkType> target);
	InsertResult addItemToNetwork(ItemStack stack, Set<IDigitalStorage> alreadyChecked);
	ItemStack removeItemsFromNetwork(ItemStack prototype, int amount, boolean b, Set<IDigitalStorage> alreadyChecked);
	boolean isPowered();
	int getKilobitsStorageFree(Set<IDigitalStorage> alreadyChecked);

	default int getKilobitsStorageFree() {
		return getKilobitsStorageFree(Sets.newHashSet(this));
	}
	
	default List<NetworkType> getTypes() {
		List<NetworkType> li = Lists.newArrayList();
		getTypes(Sets.newHashSet(this), li);
		return li;
	}
	
	default InsertResult addItemToNetwork(ItemStack stack) {
		return addItemToNetwork(stack, Sets.newHashSet(this));
	}
	
	default ItemStack removeItemsFromNetwork(ItemStack prototype, int amount, boolean b) {
		return removeItemsFromNetwork(prototype, amount, b, Sets.newHashSet(this));
	}
	
}
