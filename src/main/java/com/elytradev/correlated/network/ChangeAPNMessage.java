package com.elytradev.correlated.network;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;

import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.inventory.ContainerTerminal;

@ReceivedOn(Side.SERVER)
public class ChangeAPNMessage extends Message {

	private String apn;
	
	public ChangeAPNMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public ChangeAPNMessage(String apn) {
		super(Correlated.inst.network);
		this.apn = apn;
	}

	@Override
	protected void handle(EntityPlayer sender) {
		ItemStack held = sender.getHeldItemMainhand();
		if (held == null || held.isEmpty()) {
			held = sender.getHeldItemOffhand();
		}
		if (sender.openContainer instanceof ContainerTerminal) {
			((ContainerTerminal)sender.openContainer).terminal.setAPN(apn);
		}
	}

}
