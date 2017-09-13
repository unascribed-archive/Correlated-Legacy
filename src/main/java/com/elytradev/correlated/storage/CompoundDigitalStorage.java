package com.elytradev.correlated.storage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.elytradev.correlated.storage.InsertResult.Result;
import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Multiset;

import net.minecraft.item.ItemStack;

public class CompoundDigitalStorage implements IDigitalStorage {

	private List<IDigitalStorage> storages;
	
	public CompoundDigitalStorage(List<IDigitalStorage> storages) {
		this.storages = storages;
	}

	@Override
	public int getChangeId() {
		int i = 0;
		for (IDigitalStorage ids : storages) {
			if (!ids.isPowered()) continue;
			i += ids.getChangeId();
		}
		return i;
	}

	@Override
	public void getTypes(Set<IDigitalStorage> alreadyChecked, Collection<NetworkType> target) {
		for (IDigitalStorage ids : storages) {
			ids.getTypes(alreadyChecked, target);
			alreadyChecked.add(ids);
		}
	}

	@Override
	public InsertResult addItemToNetwork(ItemStack stack, Set<IDigitalStorage> alreadyChecked) {
		Multiset<Result> results = EnumMultiset.create(Result.class);
		for (IDigitalStorage ids : storages) {
			if (!ids.isPowered()) continue;
			InsertResult ir = ids.addItemToNetwork(stack, alreadyChecked);
			stack = ir.stack;
			alreadyChecked.add(ids);
		}
		if (!results.contains(Result.SUCCESS) && !results.contains(Result.SUCCESS_VOIDED) && results.size() > 0) {
			Result result = null;
			int num = 0;
			for (Multiset.Entry<Result> en : results.entrySet()) {
				if (en.getCount() > num) {
					result = en.getElement();
					num = en.getCount();
				}
			}
			return new InsertResult(result, stack);
		}
		return stack.isEmpty() ? results.count(Result.SUCCESS_VOIDED) > results.count(Result.SUCCESS) ?
				InsertResult.successVoided(stack) : InsertResult.success(stack) : InsertResult.insufficientStorage(stack);
	}

	@Override
	public ItemStack removeItemsFromNetwork(ItemStack prototype, int amount, boolean b, Set<IDigitalStorage> alreadyChecked) {
		ItemStack stack = prototype.copy();
		stack.setCount(0);
		for (IDigitalStorage ids : storages) {
			if (!ids.isPowered()) continue;
			if (stack.getCount() >= amount) break;
			ItemStack remote = ids.removeItemsFromNetwork(prototype, amount-stack.getCount(), b, alreadyChecked);
			if (remote != null) {
				stack.grow(remote.getCount());
			}
			alreadyChecked.add(ids);
		}
		return stack;
	}

	@Override
	public boolean isPowered() {
		for (IDigitalStorage ids : storages) {
			if (ids.isPowered()) return true;
		}
		return false;
	}

	@Override
	public int getKilobitsStorageFree(Set<IDigitalStorage> alreadyChecked) {
		int i = 0;
		for (IDigitalStorage ids : storages) {
			if (!ids.isPowered()) continue;
			i += ids.getKilobitsStorageFree(alreadyChecked);
			alreadyChecked.add(ids);
		}
		return i;
	}

}
