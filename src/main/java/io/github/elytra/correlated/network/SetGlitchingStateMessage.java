package io.github.elytra.correlated.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.client.gui.GuiFakeReboot;
import io.github.elytra.correlated.proxy.ClientProxy;
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
