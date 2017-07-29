package com.elytradev.correlated.network.fx;

import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.client.gui.GuiFakeReboot;
import com.elytradev.correlated.init.CSoundEvents;
import com.elytradev.correlated.proxy.ClientProxy;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
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
	
	public String seed;
	
	public DungeonTransitionMessage(NetworkContext ctx) {
		super(ctx);
	}
	public DungeonTransitionMessage(GlitchState state) {
		super(CNetwork.CONTEXT);
		this.state = state;
		this.forcePosition = false;
		this.seed = "";
	}
	public DungeonTransitionMessage(GlitchState state, float x, float y, float z, String seed) {
		super(CNetwork.CONTEXT);
		this.state = state;
		this.forcePosition = true;
		this.forceX = x;
		this.forceY = y;
		this.forceZ = z;
		this.seed = seed;
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
			ClientProxy.seed = seed;
		} else {
			ClientProxy.glitchTicks = -1;
			ClientProxy.seed = null;
		}
		if (state == GlitchState.REBOOT) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiFakeReboot());
		}
	}

}
