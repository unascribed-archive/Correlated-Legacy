package com.elytradev.correlated.client;

import java.util.Random;

import com.elytradev.correlated.world.LimboProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.math.MathHelper;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.correlated.proxy.ClientProxy;

public class CorrelatedMusicTicker extends MusicTicker {

	private static final Accessor<Random> rand = Accessors.findField(MusicTicker.class, "field_147679_a", "rand", "a");
	private static final Accessor<Minecraft> mc = Accessors.findField(MusicTicker.class, "field_147677_b", "mc", "b");
	private static final Accessor<ISound> currentMusic = Accessors.findField(MusicTicker.class, "field_147678_c", "currentMusic", "c");
	private static final Accessor<Integer> timeUntilNextMusic = Accessors.findField(MusicTicker.class, "field_147676_d", "timeUntilNextMusic", "d");
	
	private MusicTicker delegate;
	
	public CorrelatedMusicTicker(Minecraft mc, MusicTicker delegate) {
		super(mc);
		this.delegate = delegate;
	}
	
	@Override
	public void playMusic(MusicType requestedMusicType) {
		delegate.playMusic(requestedMusicType);
	}
	
	@Override
	public void stopMusic() {
		delegate.stopMusic();
	}
	
	@Override
	public void update() {
		if (Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().world.provider instanceof LimboProvider) {
			MusicType type = ClientProxy.enceladusType;

			if (currentMusic.get(delegate) != null) {
				if (!type.getMusicLocation().getSoundName().equals(currentMusic.get(delegate).getSoundLocation())) {
					mc.get(delegate).getSoundHandler().stopSound(currentMusic.get(delegate));
					timeUntilNextMusic.set(delegate, MathHelper.getInt(rand.get(delegate), 0, type.getMinDelay() / 2));
				}

				if (!mc.get(delegate).getSoundHandler().isSoundPlaying(currentMusic.get(delegate))) {
					currentMusic.set(delegate, null);
					timeUntilNextMusic.set(delegate, Math.min(MathHelper.getInt(rand.get(delegate), type.getMinDelay(), type.getMaxDelay()), timeUntilNextMusic.get(delegate)));
				}
			}

			timeUntilNextMusic.set(delegate, Math.min(timeUntilNextMusic.get(delegate), type.getMaxDelay()));
			
			if (currentMusic.get(delegate) == null && timeUntilNextMusic.get(delegate) <= 0) {
				delegate.playMusic(type);
			}
			timeUntilNextMusic.set(delegate, timeUntilNextMusic.get(delegate)-1);
		} else {
			delegate.update();
		}
	}

}
