package com.elytradev.correlated.network.wireless;

import java.util.Collections;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.init.CNetwork;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.wifi.IWirelessClient;
import com.elytradev.correlated.wifi.Station;
import com.elytradev.correlated.wifi.WirelessManager;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class APNRequestMessage extends Message {

	@MarshalledAs("f64")
	private double x;
	@MarshalledAs("f64")
	private double y;
	@MarshalledAs("f64")
	private double z;
	private BlockPos pos;
	
	public APNRequestMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public APNRequestMessage(double x, double y, double z, BlockPos pos) {
		super(CNetwork.CONTEXT);
		this.x = x;
		this.y = y;
		this.z = z;
		this.pos = pos;
	}

	@Override
	protected void handle(EntityPlayer sender) {
		if (sender.getDistanceSq(x, y, z) > 16*16 || sender.getDistanceSqToCenter(pos) > 16*16) {
			CLog.warn("{} tried to get APN information for a block more than 16 blocks away", sender.getDisplayNameString());
			return;
		}
		WirelessManager wm = CorrelatedWorldData.getFor(sender.world).getWirelessManager();
		Set<String> selected = Collections.emptySet();
		System.out.println(pos);
		Station block = wm.getStation(pos);
		if (block != null) {
			selected = block.getAPNs();
		} else {
			TileEntity te = sender.world.getTileEntity(pos);
			if (te instanceof IWirelessClient) {
				selected = ((IWirelessClient) te).getAPNs();
			} else {
				if (sender.getDistanceSq(x, y, z) < 1 && sender.openContainer instanceof IWirelessClient) {
					selected = ((IWirelessClient) sender.openContainer).getAPNs();
				}
			}
		}
		System.out.println(selected);
		Set<String> apns = Sets.newHashSet();
		Set<Pair<String, Number>> pairs = Sets.newHashSet();
		for (Station s : wm.allStationsInChunk(sender.world.getChunkFromBlockCoords(pos))) {
			apns.addAll(s.getAPNs());
		}
		for (String apn : apns) {
			if (apn == null) continue;
			pairs.add(Pair.of(apn, wm.getSignalStrength(x, y, z, apn)));
		}
		new APNResponseMessage(pairs, selected).sendTo(sender);
	}

}
