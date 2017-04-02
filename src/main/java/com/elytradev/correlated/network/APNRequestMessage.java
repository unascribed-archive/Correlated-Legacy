package com.elytradev.correlated.network;

import java.util.Collections;

import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.init.CNetwork;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
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
	
	public APNRequestMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public APNRequestMessage(double x, double y, double z) {
		super(CNetwork.CONTEXT);
		this.x = x;
		this.y = y;
		this.z = z;}

	@Override
	protected void handle(EntityPlayer sender) {
		BlockPos pos = new BlockPos((int)x, (int)y, (int)z);
		WirelessManager wm = CorrelatedWorldData.getFor(sender.world).getWirelessManager();
		Set<String> selected = Collections.emptySet();
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
