package com.elytradev.correlated.network;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.client.gui.GuiTerminal;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class SetSearchQueryClientMessage extends Message {
	@MarshalledAs("i32")
	public int windowId;
	public String query;

	public SetSearchQueryClientMessage(NetworkContext ctx) {
		super(ctx);
	}
	public SetSearchQueryClientMessage(int windowId, String query) {
		super(Correlated.inst.network);
		this.windowId = windowId;
		this.query = query;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		GuiScreen open = Minecraft.getMinecraft().currentScreen;
		if (open instanceof GuiTerminal) {
			GuiTerminal terminal = ((GuiTerminal)open);
			if (terminal.inventorySlots.windowId == windowId) {
				terminal.updateSearchQuery(query);
			}
		}
	}

}
