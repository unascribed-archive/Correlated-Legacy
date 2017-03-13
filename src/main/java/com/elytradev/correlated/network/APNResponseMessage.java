package com.elytradev.correlated.network;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.client.gui.GuiSelectAPN;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class APNResponseMessage extends Message {

	@MarshalledAs("string-list")
	private List<String> apns;
	@MarshalledAs("u8-list")
	private List<Number> signalStrengths;
	
	@MarshalledAs("string-list")
	private List<String> selected;
	
	public APNResponseMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public APNResponseMessage(Iterable<Pair<String, Number>> apns, Iterable<String> selected) {
		super(Correlated.inst.network);
		this.apns = Lists.newArrayList();
		this.signalStrengths = Lists.newArrayList();
		for (Pair<String, Number> apn : apns) {
			this.apns.add(apn.getLeft());
			this.signalStrengths.add(apn.getRight());
		}
		this.selected = Lists.newArrayList(selected);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		GuiScreen gs = Minecraft.getMinecraft().currentScreen;
		if (gs instanceof GuiSelectAPN) {
			List<Pair<String, Integer>> li = Lists.newArrayList();
			for (int i = 0; i < apns.size(); i++) {
				li.add(Pair.of(apns.get(i), signalStrengths.get(i).intValue()));
			}
			((GuiSelectAPN)gs).updateAPNList(li, selected);
		}
	}

}
