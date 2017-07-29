package com.elytradev.correlated.network.fx;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.client.debug.Line;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.proxy.ClientProxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class AddLineMessage extends Message {

	@MarshalledAs("f32")
	private double x1;
	@MarshalledAs("f32")
	private double y1;
	@MarshalledAs("f32")
	private double z1;
	
	@MarshalledAs("f32")
	private double x2;
	@MarshalledAs("f32")
	private double y2;
	@MarshalledAs("f32")
	private double z2;
	
	public AddLineMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public AddLineMessage(double x1, double y1, double z1, double x2, double y2, double z2) {
		super(CNetwork.CONTEXT);
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
	}



	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer player) {
		ClientProxy.shapes.add(new Line(x1, y1, z1, x2, y2, z2, 1, 0, 0, 6));
	}

}
