package com.elytradev.correlated.network.inventory;

import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.inventory.ContainerTerminal;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class SetSearchQueryServerMessage extends Message {
	@MarshalledAs("i32")
	public int windowId;
	public String query;

	public SetSearchQueryServerMessage(NetworkContext ctx) {
		super(ctx);
	}
	public SetSearchQueryServerMessage(int windowId, String query) {
		super(CNetwork.CONTEXT);
		this.windowId = windowId;
		this.query = query;
	}
	
	@Override
	protected void handle(EntityPlayer sender) {
		Container c = ((EntityPlayerMP)sender).openContainer;
		if (c instanceof ContainerTerminal && c.windowId == windowId) {
			((ContainerTerminal)c).updateSearchQuery(query);
		}
	}

}
