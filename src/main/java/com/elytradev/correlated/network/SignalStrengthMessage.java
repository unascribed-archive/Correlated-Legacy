package com.elytradev.correlated.network;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.client.gui.GuiTerminal;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class SignalStrengthMessage extends Message {

	@MarshalledAs("i8")
	private int strength;
	
	public SignalStrengthMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SignalStrengthMessage(int strength) {
		super(Correlated.inst.network);
		this.strength = strength;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiTerminal) {
			((GuiTerminal)Minecraft.getMinecraft().currentScreen).signalStrength = strength;
		}
	}

}
