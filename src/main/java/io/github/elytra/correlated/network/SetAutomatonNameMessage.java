package io.github.elytra.correlated.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.inventory.ContainerAutomaton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class SetAutomatonNameMessage extends Message {
	@MarshalledAs("i32")
	public int windowId;
	public String name;

	public SetAutomatonNameMessage(NetworkContext ctx) {
		super(ctx);
	}
	public SetAutomatonNameMessage(int windowId, String name) {
		super(Correlated.inst.network);
		this.windowId = windowId;
		this.name = name;
	}

	@Override
	protected void handle(EntityPlayer sender) {
		Container c = ((EntityPlayerMP)sender).openContainer;
		if (c instanceof ContainerAutomaton && c.windowId == windowId) {
			((ContainerAutomaton)c).automaton.setCustomNameTag(name);
		}
	}

}
