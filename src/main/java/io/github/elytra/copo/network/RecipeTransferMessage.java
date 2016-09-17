package io.github.elytra.copo.network;

import java.util.List;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.inventory.ContainerVT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class RecipeTransferMessage extends Message {
	@MarshalledAs("i32")
	public int windowId;
	@MarshalledAs("itemstack-list-list")
	public List<List<ItemStack>> matrix;
	public boolean max;
	
	public RecipeTransferMessage(NetworkContext ctx) {
		super(ctx);
	}
	public RecipeTransferMessage(int windowId, List<List<ItemStack>> matrix, boolean max) {
		super(CoPo.inst.network);
		this.windowId = windowId;
		this.matrix = matrix;
		this.max = max;
	}
	
	@Override
	protected void handle(EntityPlayer sender) {
		if (sender.openContainer instanceof ContainerVT && sender.openContainer.windowId == windowId) {
			ContainerVT vt = ((ContainerVT)sender.openContainer);
			if (vt.hasCraftingMatrix) {
				boolean anyFailed = false;
				ItemStack[] matrixResult = new ItemStack[9];
				for (int i = 0; i < 9; i++) {
					List<ItemStack> possibilities = matrix.get(i);
					if (possibilities.isEmpty()) continue;
					ItemStack res = null;
					for (ItemStack is : possibilities) {
						res = vt.removeItemsFromNetwork(is, 1);
						if (res != null) break;
					}
					if (res != null) {
						matrixResult[i] = res;
					} else {
						anyFailed = true;
						break;
					}
				}
				if (anyFailed) {
					for (ItemStack is : matrixResult) {
						if (is != null) {
							vt.addItemToNetwork(is);
						}
					}
				} else {
					for (int i = 0; i < 9; i++) {
						ItemStack cur = vt.craftMatrix.getStackInSlot(i);
						if (cur != null) {
							cur = vt.addItemToNetwork(cur);
							if (cur != null) {
								sender.entityDropItem(cur, 0.5f);
								cur = null;
							}
						}
						vt.craftMatrix.setInventorySlotContents(i, matrixResult[i]);
					}
				}
			}
		}
	}

}
