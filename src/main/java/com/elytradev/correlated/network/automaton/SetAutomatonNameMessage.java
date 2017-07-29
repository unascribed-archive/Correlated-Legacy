package com.elytradev.correlated.network.automaton;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.inventory.ContainerAutomaton;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
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
		super(CNetwork.CONTEXT);
		this.windowId = windowId;
		this.name = name;
	}

	@Override
	protected void handle(EntityPlayer sender) {
		if (sender.isSpectator()) {
			CLog.warn("{}, a spectator, tried to send a packet only applicable to non-spectators", sender.getDisplayNameString());
			return;
		}
		Container c = ((EntityPlayerMP)sender).openContainer;
		if (c instanceof ContainerAutomaton && c.windowId == windowId) {
			((ContainerAutomaton)c).automaton.setCustomNameTag(name);
		}
	}

}
