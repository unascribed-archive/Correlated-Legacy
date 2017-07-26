package com.elytradev.correlated.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.client.debug.Globe;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.proxy.ClientProxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class AddGlobeMessage extends Message {

	@MarshalledAs("f32")
	private double x;
	@MarshalledAs("f32")
	private double y;
	@MarshalledAs("f32")
	private double z;
	
	@MarshalledAs("f32")
	private float radius;
	
	public AddGlobeMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public AddGlobeMessage(double x, double y, double z, float radius) {
		super(CNetwork.CONTEXT);
		this.x = x;
		this.y = y;
		this.z = z;
		this.radius = radius;
	}



	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer player) {
		ClientProxy.shapes.add(new Globe(x, y, z, 1, 0, 0, 6, radius));
	}

}
