package com.elytradev.correlated.network;

import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.client.gui.GuiFakeReboot;
import com.elytradev.correlated.init.CSoundEvents;
import com.elytradev.correlated.proxy.ClientProxy;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class DungeonTransitionMessage extends Message {
	public enum GlitchState {
		NONE,
		CORRUPTING,
		REBOOT
	}

	public GlitchState state;
	
	public boolean forcePosition;
	@MarshalledAs("f32")
	public float forceX;
	@MarshalledAs("f32")
	public float forceY;
	@MarshalledAs("f32")
	public float forceZ;
	
	public DungeonTransitionMessage(NetworkContext ctx) {
		super(ctx);
	}
	public DungeonTransitionMessage(GlitchState state) {
		super(CNetwork.CONTEXT);
		this.state = state;
		this.forcePosition = false;
	}
	public DungeonTransitionMessage(GlitchState state, float x, float y, float z) {
		super(CNetwork.CONTEXT);
		this.state = state;
		this.forcePosition = true;
		this.forceX = x;
		this.forceY = y;
		this.forceZ = z;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Minecraft.getMinecraft().getSoundHandler().stopSounds();
		if (forcePosition) {
			Minecraft.getMinecraft().player.posX = forceX;
			Minecraft.getMinecraft().player.posY = forceY;
			Minecraft.getMinecraft().player.posZ = forceZ;
			
			Minecraft.getMinecraft().player.lastTickPosX = forceX;
			Minecraft.getMinecraft().player.lastTickPosY = forceY;
			Minecraft.getMinecraft().player.lastTickPosZ = forceZ;
		}
		if (state == GlitchState.CORRUPTING) {
			Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(CSoundEvents.GLITCHBGM, 1f));
			ClientProxy.glitchTicks = 0;
		} else {
			ClientProxy.glitchTicks = -1;
		}
		if (state == GlitchState.REBOOT) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiFakeReboot());
		}
	}

}
