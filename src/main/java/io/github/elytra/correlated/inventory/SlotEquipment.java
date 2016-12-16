package io.github.elytra.correlated.inventory;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class SlotEquipment extends Slot {
	private EntityLivingBase elb;
	private EntityEquipmentSlot slot;
	public SlotEquipment(EntityLivingBase elb, EntityEquipmentSlot slot, int x, int y) {
		super(null, slot.ordinal(), x, y);
		this.elb = elb;
		this.slot = slot;
	}
	
	@Override
	public void putStack(ItemStack stack) {
		elb.setItemStackToSlot(slot, stack);
		onSlotChanged();
	}
	
	@Override
	public ItemStack getStack() {
		return elb.getItemStackFromSlot(slot);
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return stack.getMaxStackSize();
	}
	
	@Override
	public int getSlotStackLimit() {
		return slot.getSlotType() == Type.HAND ? 64 : 1;
	}
	
	@Override
	public ItemStack decrStackSize(int amount) {
		if (getStack() == null) return null;
		ItemStack result = getStack().splitStack(amount);
		if (getStack().stackSize <= 0) {
			putStack(null);
		}
		return result;
	}
	
	@Override
	public boolean isHere(IInventory inv, int slotIn) {
		return false;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		if (slot.getSlotType() == Type.HAND) return true;
		return stack.getItem().isValidArmor(stack, slot, elb);
	}
	
	@Override
	public void onSlotChanged() {
	}
	
	@Override
	public String getSlotTexture() {
		if (slot == EntityEquipmentSlot.MAINHAND) return null;
		return slot == EntityEquipmentSlot.OFFHAND ? "minecraft:items/empty_armor_slot_shield" : ItemArmor.EMPTY_SLOT_NAMES[slot.getIndex()];
	}
	

}
