package com.elytradev.correlated.network;

import com.elytradev.correlated.Correlated;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.CLIENT)
public class AutomatonSpeakMessage extends Message {
	@MarshalledAs("i32")
	public int entityId;
	public String line;

	public AutomatonSpeakMessage(NetworkContext ctx) {
		super(ctx);
	}
	public AutomatonSpeakMessage(int entityId, String line) {
		super(Correlated.inst.network);
		this.entityId = entityId;
		this.line = line;
	}
	
	@Override
	protected void handle(EntityPlayer sender) {
		// TODO Auto-generated method stub
		
	}

}
