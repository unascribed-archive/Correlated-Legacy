package com.elytradev.correlated.network;

import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.world.LimboProvider;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class LeaveDungeonMessage extends Message {

	public LeaveDungeonMessage(NetworkContext ctx) {
		super(ctx);
	}
	public LeaveDungeonMessage() {
		super(CNetwork.CONTEXT);
	}

	@Override
	protected void handle(EntityPlayer sender) {
		int dim = CConfig.limboDimId;
		WorldServer world = ((EntityPlayerMP)sender).mcServer.worldServerForDimension(dim);
		WorldProvider provider = world.provider;
		if (provider instanceof LimboProvider) {
			((LimboProvider)provider).addLeavingPlayer(sender.getGameProfile().getId());
		}
	}

}
