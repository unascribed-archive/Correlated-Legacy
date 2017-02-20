package com.elytradev.correlated.network;

import org.apache.commons.lang3.mutable.MutableInt;

import com.elytradev.correlated.Correlated;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
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
		super(Correlated.inst.network);
		this.entityId = entityId;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Entity e = Minecraft.getMinecraft().world.getEntityByID(entityId);
		if (e instanceof EntityPlayer) {
			Correlated.weldthrower.weldthrowing.put((EntityPlayer)e, new MutableInt());
		}
	}

}
