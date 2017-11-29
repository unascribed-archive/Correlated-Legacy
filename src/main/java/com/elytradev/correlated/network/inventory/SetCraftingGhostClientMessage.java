package com.elytradev.correlated.network.inventory;

import java.util.List;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.client.gui.GuiTerminal;
import com.elytradev.correlated.init.CNetwork;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@ReceivedOn(Side.CLIENT)
public class SetCraftingGhostClientMessage extends Message {

	@MarshalledAs("i32")
	private int windowId;
	@MarshalledAs("itemstack-list-list")
	private List<? extends List<ItemStack>> ghost;
	
	public SetCraftingGhostClientMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SetCraftingGhostClientMessage(int windowId, List<? extends List<ItemStack>> ghost) {
		super(CNetwork.CONTEXT);
		this.windowId = windowId;
		this.ghost = ghost;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer player) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiTerminal) {
			GuiTerminal gt = (GuiTerminal)Minecraft.getMinecraft().currentScreen;
			if (gt.inventorySlots.windowId == windowId) {
				gt.setRecipe(ghost);
			}
		}
	}

}
