package io.github.elytra.copo.inventory;

import io.github.elytra.copo.tile.TileEntityImporterChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ContainerImporterChest extends ContainerTerminal {
	private TileEntityImporterChest teic;
	
	public ContainerImporterChest(IInventory playerInventory, EntityPlayer player, TileEntityImporterChest teic) {
		super(playerInventory, player, teic);
		this.teic = teic;
		teic.openChest();
	}
	
	@Override
	protected void initializeTerminalSize() {
		startX = 8;
		startY = 0;
		slotsAcross = 9;
		slotsTall = 6;
		hasCraftingMatrix = false;
		playerInventoryOffsetX = 0;
		playerInventoryOffsetY = 37;
	}
	
	@Override
	public ItemStack addItemToNetwork(ItemStack stack) {
		// refuse to insert items, read-only
		return stack;
	}
	
	@Override
	public void onContainerClosed(EntityPlayer player) {
		teic.closeChest();
	}

}
