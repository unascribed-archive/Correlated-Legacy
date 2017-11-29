package com.elytradev.correlated.network.inventory;

import java.util.List;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.inventory.ContainerTerminal;

import net.minecraftforge.fml.relauncher.Side;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@ReceivedOn(Side.SERVER)
public class SetCraftingGhostServerMessage extends Message {

	@MarshalledAs("i32")
	private int windowId;
	@MarshalledAs("itemstack-list-list")
	private List<? extends List<ItemStack>> ghost;
	
	public SetCraftingGhostServerMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SetCraftingGhostServerMessage(int windowId, List<? extends List<ItemStack>> ghost) {
		super(CNetwork.CONTEXT);
		this.windowId = windowId;
		this.ghost = ghost;
	}
	
	@Override
	protected void handle(EntityPlayer player) {
		if (player.openContainer.windowId == windowId && player.openContainer instanceof ContainerTerminal) {
			((ContainerTerminal)player.openContainer).setCraftingGhost(ghost);
		}
	}

}
