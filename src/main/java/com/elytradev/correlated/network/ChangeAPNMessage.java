package com.elytradev.correlated.network;

import java.util.List;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.wifi.IWirelessClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@ReceivedOn(Side.SERVER)
public class ChangeAPNMessage extends Message {

	private boolean isBlock;
	private BlockPos pos;
	@MarshalledAs("string-list")
	private List<String> apns;
	
	public ChangeAPNMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public ChangeAPNMessage(Iterable<String> apns) {
		super(Correlated.inst.network);
		this.isBlock = false;
		this.pos = BlockPos.ORIGIN;
		this.apns = Lists.newArrayList(apns);
	}
	
	public ChangeAPNMessage(BlockPos pos, Iterable<String> apns) {
		super(Correlated.inst.network);
		this.isBlock = true;
		this.pos = pos;
		this.apns = Lists.newArrayList(apns);
	}

	@Override
	protected void handle(EntityPlayer sender) {
		if (!isBlock) {
			if (sender.openContainer instanceof IWirelessClient) {
				((IWirelessClient)sender.openContainer).setAPNs(Sets.newHashSet(apns));
			} else {
				Correlated.log.warn("{} attempted to update APNs when a wireless container is not open", sender.getName());
			}
		} else {
			TileEntity te = sender.world.getTileEntity(pos);
			if (te instanceof IWirelessClient) {
				((IWirelessClient)te).setAPNs(Sets.newHashSet(apns));
			} else {
				Correlated.log.warn("{} attempted to update APNs for a non-wireless block at {}, {}, {}", sender.getName(), pos.getX(), pos.getY(), pos.getZ());
			}
		}
	}

}
