package com.elytradev.correlated.init;

import java.util.Locale;

import com.elytradev.correlated.CLog;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CSoundEvents {

	public static SoundEvent WELDTHROW;
	public static SoundEvent GLITCHBGM;
	public static SoundEvent GLITCHFLOPPY;
	public static SoundEvent GLITCHBOOT;
	public static SoundEvent CONVERT;
	public static SoundEvent GLITCHTRAVEL;
	public static SoundEvent AUTOMATON_IDLE;
	public static SoundEvent AUTOMATON_HURT;
	public static SoundEvent DRIVE_DISASSEMBLE;
	public static SoundEvent DATA_CORE_SHATTER;
	public static SoundEvent ENCELADUS;
	
	public static void register() {
		register("weldthrow");
		register("glitchbgm");
		register("glitchfloppy");
		register("glitchboot");
		register("convert");
		register("glitchtravel");
		register("automaton_hurt");
		register("automaton_idle");
		register("drive_disassemble");
		register("data_core_shatter");
		register("enceladus");
	}
	
	private static void register(String name) {
		ResourceLocation loc = new ResourceLocation("correlated", name);
		SoundEvent snd = new SoundEvent(loc);
		GameRegistry.register(snd, loc);
		try {
			CSoundEvents.class.getField(name.toUpperCase(Locale.ROOT)).set(null, snd);
		} catch (NoSuchFieldException e) {
			throw new AssertionError("Missing field for sound event "+name);
		} catch (Exception e) {
			CLog.warn("Unexpected error while filling field for sound event {}", name, e);
		}
	}

}
