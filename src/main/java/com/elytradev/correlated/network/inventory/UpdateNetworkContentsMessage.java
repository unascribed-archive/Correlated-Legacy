package com.elytradev.correlated.network.inventory;

import java.util.List;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.network.NetworkTypeMarshaller;
import com.elytradev.correlated.storage.NetworkType;
import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class UpdateNetworkContentsMessage extends Message {
	
	@MarshalledAs(NetworkTypeMarshaller.NAME_LIST)
	private List<NetworkType> entries;
	
	public UpdateNetworkContentsMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public UpdateNetworkContentsMessage(Iterable<NetworkType> entries) {
		super(CNetwork.CONTEXT);
		this.entries = Lists.newArrayList(entries);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer player) {

	}

}
