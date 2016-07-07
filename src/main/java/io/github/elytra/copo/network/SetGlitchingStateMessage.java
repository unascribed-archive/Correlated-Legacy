package io.github.elytra.copo.network;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.ClientProxy;
import io.github.elytra.copo.client.gui.GuiFakeReboot;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SetGlitchingStateMessage implements IMessage, IMessageHandler<SetGlitchingStateMessage, IMessage> {
	public enum GlitchState {
		NONE,
		CORRUPTING,
		REBOOT
	}

	public GlitchState state;
	
	public SetGlitchingStateMessage() {}
	public SetGlitchingStateMessage(GlitchState state) {
		this.state = state;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		state = GlitchState.values()[buf.readUnsignedByte()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(state.ordinal());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(SetGlitchingStateMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			if (message.state == GlitchState.CORRUPTING) {
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(CoPo.glitch_bgm, 1f));
				ClientProxy.glitchTicks = 0;
			} else {
				Minecraft.getMinecraft().getSoundHandler().stopSounds();
				ClientProxy.glitchTicks = -1;
			}
			if (message.state == GlitchState.REBOOT) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiFakeReboot());
			}
		});
		return null;
	}

}
