package io.github.elytra.correlated.network;

import java.util.List;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.inventory.ContainerTerminal;
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
		super(Correlated.inst.network);
		this.windowId = windowId;
		this.matrix = matrix;
		this.max = max;
	}
	
	@Override
	protected void handle(EntityPlayer sender) {
		if (sender.openContainer instanceof ContainerTerminal && sender.openContainer.windowId == windowId) {
			ContainerTerminal terminal = ((ContainerTerminal)sender.openContainer);
			if (terminal.hasCraftingMatrix) {
				boolean anyFailed = false;
				ItemStack[] matrixResult = new ItemStack[9];
				for (int i = 0; i < 9; i++) {
					List<ItemStack> possibilities = matrix.get(i);
					if (possibilities.isEmpty()) continue;
					ItemStack res = ItemStack.EMPTY;
					for (ItemStack is : possibilities) {
						res = terminal.removeItemsFromNetwork(is, 1);
						if (!res.isEmpty()) break;
					}
					if (!res.isEmpty()) {
						matrixResult[i] = res;
					} else {
						anyFailed = true;
						break;
					}
				}
				if (anyFailed) {
					for (ItemStack is : matrixResult) {
						if (!is.isEmpty()) {
							terminal.addItemToNetwork(is);
						}
					}
				} else {
					for (int i = 0; i < 9; i++) {
						ItemStack cur = terminal.craftMatrix.getStackInSlot(i);
						if (!cur.isEmpty()) {
							cur = terminal.addItemToNetwork(cur);
							if (!cur.isEmpty()) {
								sender.entityDropItem(cur, 0.5f);
								cur = ItemStack.EMPTY;
							}
						}
						terminal.craftMatrix.setInventorySlotContents(i, matrixResult[i]);
					}
				}
			}
		}
	}

}
