package io.github.elytra.copo.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.world.LimboProvider;
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
		super(CoPo.inst.network);
	}

	@Override
	protected void handle(EntityPlayer sender) {
		int dim = CoPo.limboDimId;
		WorldServer world = ((EntityPlayerMP)sender).mcServer.worldServerForDimension(dim);
		WorldProvider provider = world.provider;
		if (provider instanceof LimboProvider) {
			((LimboProvider)provider).addLeavingPlayer(sender.getGameProfile().getId());
		}
	}

}
