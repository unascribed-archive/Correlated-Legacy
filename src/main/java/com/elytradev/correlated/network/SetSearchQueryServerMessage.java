package com.elytradev.correlated.network;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.inventory.ContainerTerminal;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
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
		super(Correlated.inst.network);
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
