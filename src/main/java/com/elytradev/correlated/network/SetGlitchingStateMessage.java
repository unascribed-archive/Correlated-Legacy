package com.elytradev.correlated.network;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.client.gui.GuiFakeReboot;
import com.elytradev.correlated.proxy.ClientProxy;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class SetGlitchingStateMessage extends Message {
	public enum GlitchState {
		NONE,
		CORRUPTING,
		REBOOT
	}

	public GlitchState state;
	
	public SetGlitchingStateMessage(NetworkContext ctx) {
		super(ctx);
	}
	public SetGlitchingStateMessage(GlitchState state) {
		super(Correlated.inst.network);
		this.state = state;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Minecraft.getMinecraft().getSoundHandler().stopSounds();
		if (state == GlitchState.CORRUPTING) {
			Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(Correlated.glitchbgm, 1f));
			ClientProxy.glitchTicks = 0;
		} else {
			ClientProxy.glitchTicks = -1;
		}
		if (state == GlitchState.REBOOT) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiFakeReboot());
		}
	}

}
