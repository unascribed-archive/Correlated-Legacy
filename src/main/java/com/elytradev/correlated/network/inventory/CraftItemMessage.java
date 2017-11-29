package com.elytradev.correlated.network.inventory;

import java.util.List;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.CLog;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.inventory.ContainerTerminal;
import com.elytradev.correlated.inventory.ContainerTerminal.CraftingAmount;
import com.elytradev.correlated.inventory.ContainerTerminal.CraftingTarget;
import com.elytradev.correlated.storage.IDigitalStorage;
import com.elytradev.correlated.storage.InsertResult;
import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.ItemHandlerHelper;

@ReceivedOn(Side.SERVER)
public class CraftItemMessage extends Message {

	@MarshalledAs("i32")
	private int windowId;
	@MarshalledAs("itemstack-list")
	private List<ItemStack> matrix;
	@MarshalledAs("u8")
	private int action;
	
	public CraftItemMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public CraftItemMessage(int windowId, InventoryCrafting matrix, int action) {
		super(CNetwork.CONTEXT);
		this.windowId = windowId;
		List<ItemStack> list = Lists.newArrayListWithCapacity(9);
		for (int i = 0; i < matrix.getInventoryStackLimit(); i++) {
			list.add(matrix.getStackInSlot(i));
		}
		this.matrix = list;
		this.action = action;
	}
	
	public CraftItemMessage(int windowId, List<ItemStack> matrix, int action) {
		super(CNetwork.CONTEXT);
		this.windowId = windowId;
		this.matrix = matrix;
		this.action = action;
	}

	@Override
	protected void handle(EntityPlayer player) {
		if (player.isSpectator()) {
			CLog.warn("{}, a spectator, tried to send a packet only applicable to non-spectators", player.getDisplayNameString());
			return;
		}
		if (player.openContainer.windowId == windowId && player.openContainer instanceof ContainerTerminal) {
			ContainerTerminal ct = (ContainerTerminal)player.openContainer;
			InventoryCrafting dummyMatrix = new InventoryCrafting(ct, 3, 3);
			if (!fillMatrix(matrix, dummyMatrix, ct.terminal.getStorage(), player)) return;
			IRecipe ir = CraftingManager.findMatchingRecipe(dummyMatrix, player.world);
			if (ir == null) return;
			if (action == 2) {
				CraftingTarget target = ct.craftingTarget;
				CraftingAmount amt = ct.craftingAmount;
				int operationsLeft = -1;
				while (true) {
					ItemStack result = ir.getCraftingResult(dummyMatrix);
					if (operationsLeft == -1) {
						operationsLeft = amt.amountToCraft.apply(result);
					}
					operationsLeft--;
					IDigitalStorage ids = ct.terminal.getStorage();
					if (target == CraftingTarget.NETWORK && ids != null) {
						InsertResult res = ids.addItemToNetwork(result);
						if (!res.stack.isEmpty()) {
							player.dropItem(res.stack, false);
							break;
						}
					} else {
						if (!player.inventory.addItemStackToInventory(result)) {
							player.dropItem(result, false);
							break;
						}
					}
					NonNullList<ItemStack> remaining = ir.getRemainingItems(dummyMatrix);
					for (int i = 0; i < 9; i++) {
						dummyMatrix.setInventorySlotContents(i, remaining.get(i));
					}
					if (operationsLeft <= 0) break;
					if (!fillMatrix(matrix, dummyMatrix, ct.terminal.getStorage(), player)) break;
				}
			} else {
				ItemStack preview = ir.getRecipeOutput();
				if (!player.inventory.getItemStack().isEmpty() && !ItemHandlerHelper.canItemStacksStack(preview, player.inventory.getItemStack())) {
					clearMatrix(dummyMatrix, ct.terminal.getStorage(), player);
					return;
				}
				int operationsLeft = -1;
				ItemStack out = ItemStack.EMPTY;
				while (true) {
					ItemStack result = ir.getCraftingResult(dummyMatrix);
					if (operationsLeft == -1) {
						if (action == 0) {
							operationsLeft = 1;
						} else if (action == 1) {
							int existing = player.inventory.getItemStack().getCount();
							int toCraft = result.getMaxStackSize()-existing;
							operationsLeft = Math.max(1, toCraft/result.getCount());
						} else {
							CLog.warn("Unknown crafting operation {}", action);
							operationsLeft = 1;
						}
					}
					operationsLeft--;
					if (out.isEmpty()) {
						out = result;
					} else {
						if (ItemHandlerHelper.canItemStacksStack(result, out)) {
							out.grow(result.getCount());
						} else {
							player.dropItem(result, false);
							break;
						}
					}
					NonNullList<ItemStack> remaining = ir.getRemainingItems(dummyMatrix);
					for (int i = 0; i < 9; i++) {
						dummyMatrix.setInventorySlotContents(i, remaining.get(i));
					}
					if (operationsLeft <= 0) break;
					if (!fillMatrix(matrix, dummyMatrix, ct.terminal.getStorage(), player)) break;
				}
				if (!player.inventory.getItemStack().isEmpty() && !ItemHandlerHelper.canItemStacksStack(preview, player.inventory.getItemStack())) {
					player.dropItem(out, false);
				} else {
					ItemStack is = player.inventory.getItemStack();
					if (is.isEmpty()) {
						is = out;
					} else {
						is.grow(out.getCount());
					}
					player.inventory.setItemStack(is);
				}
				if (player instanceof EntityPlayerMP) {
					((EntityPlayerMP)player).connection.sendPacket(new SPacketSetSlot(-1, 0, player.inventory.getItemStack()));
				}
			}
			clearMatrix(dummyMatrix, ct.terminal.getStorage(), player);
		}
	}

	private static boolean fillMatrix(List<ItemStack> template, InventoryCrafting matrix, IDigitalStorage storage, EntityPlayer player) {
		clearMatrix(matrix, storage, player);
		for (int i = 0; i < 9; i++) {
			if (template.get(i).isEmpty()) continue;
			ItemStack is = storage == null ? ItemStack.EMPTY : storage.removeItemsFromNetwork(template.get(i), 1, true);
			if (is.isEmpty()) {
				int idx = player.inventory.findSlotMatchingUnusedItem(template.get(i));
				ItemStack inSlot = idx == -1 ? ItemStack.EMPTY : player.inventory.getStackInSlot(idx);
				if (idx != -1) {
					ItemStack res = inSlot.splitStack(1);
					player.inventory.setInventorySlotContents(idx, inSlot);
					is = res;
				}
				if (is.isEmpty()) {
					clearMatrix(matrix, storage, player);
					return false;
				}
			}
			matrix.setInventorySlotContents(i, is);
		}
		return true;
	}

	private static void clearMatrix(InventoryCrafting matrix, IDigitalStorage storage, EntityPlayer player) {
		for (int i = 0; i < matrix.getSizeInventory(); i++) {
			if (storage != null) {
				InsertResult ir = storage.addItemToNetwork(matrix.removeStackFromSlot(i));
				player.dropItem(ir.stack, false);
			} else {
				player.dropItem(matrix.removeStackFromSlot(i), false);
			}
		}
	}

}
