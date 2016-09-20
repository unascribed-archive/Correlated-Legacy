package io.github.elytra.copo.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.inventory.ContainerTerminal;
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
		super(CoPo.inst.network);
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
