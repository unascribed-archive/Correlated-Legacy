package com.elytradev.correlated.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class InventoryAdapter implements IInventory {

	protected abstract ItemStack get();
	protected abstract void set(ItemStack stack);
	
	@Override
	public String getName() {
		return "adapter";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return null;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return get().isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index != 0) throw new IndexOutOfBoundsException();
		return get();
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (index != 0) throw new IndexOutOfBoundsException();
		return get().splitStack(count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (index != 0) throw new IndexOutOfBoundsException();
		ItemStack is = get();
		set(ItemStack.EMPTY);
		return is;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index != 0) throw new IndexOutOfBoundsException();
		set(stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
		
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index != 0) throw new IndexOutOfBoundsException();
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		set(ItemStack.EMPTY);
	}

}
