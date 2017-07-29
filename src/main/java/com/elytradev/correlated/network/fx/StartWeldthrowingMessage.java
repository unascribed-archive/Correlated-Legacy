package com.elytradev.correlated.network.fx;

import org.apache.commons.lang3.mutable.MutableInt;
import com.elytradev.correlated.init.CNetwork;

import com.elytradev.correlated.init.CItems;
import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class StartWeldthrowingMessage extends Message {
	@MarshalledAs("i32")
	public int entityId;
	
	public StartWeldthrowingMessage(NetworkContext ctx) {
		super(ctx);
	}
	public StartWeldthrowingMessage(int entityId) {
		super(CNetwork.CONTEXT);
		this.entityId = entityId;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Entity e = Minecraft.getMinecraft().world.getEntityByID(entityId);
		if (e instanceof EntityPlayer) {
			CItems.WELDTHROWER.weldthrowing.put((EntityPlayer)e, new MutableInt());
		}
	}

}
