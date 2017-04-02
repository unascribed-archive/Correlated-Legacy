package com.elytradev.correlated.network;

import com.elytradev.concrete.Message;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class AnimationSeenMessage extends Message {

	public AnimationSeenMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public AnimationSeenMessage() {
		super(CNetwork.CONTEXT);
	}
	
	@Override
	protected void handle(EntityPlayer sender) {
		sender.getEntityData().setBoolean("correlated:seenAnimation", true);
	}

}
