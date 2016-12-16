package io.github.elytra.correlated.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.correlated.Correlated;
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
