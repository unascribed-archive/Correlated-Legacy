package com.elytradev.correlated.network.wireless;

import java.util.List;
import com.elytradev.correlated.init.CNetwork;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.CLog;
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
		super(CNetwork.CONTEXT);
		this.isBlock = false;
		this.pos = BlockPos.ORIGIN;
		this.apns = Lists.newArrayList(apns);
	}
	
	public ChangeAPNMessage(BlockPos pos, Iterable<String> apns) {
		super(CNetwork.CONTEXT);
		this.isBlock = true;
		this.pos = pos;
		this.apns = Lists.newArrayList(apns);
	}

	@Override
	protected void handle(EntityPlayer sender) {
		if (sender.isSpectator()) {
			CLog.warn("{}, a spectator, tried to send a packet only applicable to non-spectators", sender.getDisplayNameString());
			return;
		}
		if (!isBlock) {
			if (sender.openContainer instanceof IWirelessClient) {
				((IWirelessClient)sender.openContainer).setAPNs(Sets.newHashSet(apns));
			} else {
				CLog.warn("{} attempted to update APNs when a wireless container is not open", sender.getName());
			}
		} else {
			if (sender.getDistanceSqToCenter(pos) > 16*16) {
				CLog.warn("{} tried to set APN information for a block more than 16 blocks away", sender.getDisplayNameString());
				return;
			}
			TileEntity te = sender.world.getTileEntity(pos);
			if (te instanceof IWirelessClient) {
				((IWirelessClient)te).setAPNs(Sets.newHashSet(apns));
			} else {
				CLog.warn("{} attempted to update APNs for a non-wireless block at {}, {}, {}", sender.getName(), pos.getX(), pos.getY(), pos.getZ());
			}
		}
	}

}
