package com.elytradev.correlated.network.inventory;

import java.util.List;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.CLog;
import com.elytradev.correlated.client.gui.GuiTerminal;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.network.NetworkTypeMarshaller;
import com.elytradev.correlated.network.PrototypeMarshaller;
import com.elytradev.correlated.storage.NetworkType;
import com.elytradev.correlated.storage.Prototype;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class UpdateNetworkContentsMessage extends Message {
	
	@MarshalledAs(NetworkTypeMarshaller.NAME_LIST)
	private List<NetworkType> addOrChange;
	@MarshalledAs(PrototypeMarshaller.NAME_LIST)
	private List<Prototype> remove;
	private boolean overwrite;
	
	public UpdateNetworkContentsMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public UpdateNetworkContentsMessage(Iterable<NetworkType> addOrChange, Iterable<Prototype> remove, boolean overwrite) {
		super(CNetwork.CONTEXT);
		this.addOrChange = Lists.newArrayList(addOrChange);
		this.remove = Lists.newArrayList(remove);
		this.overwrite = overwrite;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer player) {
		if (!remove.isEmpty() && overwrite) {
			CLog.warn("Got an overwrite network update with a removal list; the removals will be ignored");
		}
		GuiScreen gs = Minecraft.getMinecraft().currentScreen;
		if (gs instanceof GuiTerminal) {
			((GuiTerminal) gs).updateNetworkContents(addOrChange, remove, overwrite);
		}
	}

}
