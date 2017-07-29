package com.elytradev.correlated.network.dungeon;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.world.LimboProvider;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
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
		if (sender.isSpectator()) {
			CLog.warn("{}, a spectator, tried to send a packet only applicable to non-spectators", sender.getDisplayNameString());
			return;
		}
		int dim = CConfig.limboDimId;
		WorldServer world = ((EntityPlayerMP)sender).mcServer.getWorld(dim);
		WorldProvider provider = world.provider;
		if (provider instanceof LimboProvider) {
			((LimboProvider)provider).addLeavingPlayer(sender.getGameProfile().getId());
		}
	}

}
