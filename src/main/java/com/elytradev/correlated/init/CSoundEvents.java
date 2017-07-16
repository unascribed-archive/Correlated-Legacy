package com.elytradev.correlated.init;

import java.util.List;
import java.util.Locale;

import com.elytradev.correlated.CLog;
import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class CSoundEvents {

	static final List<SoundEvent> records = Lists.newArrayList();
	
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
	
	@SubscribeEvent
	public static void register(RegistryEvent.Register<SoundEvent> e) {
		register(e.getRegistry(), "weldthrow");
		register(e.getRegistry(), "glitchbgm");
		register(e.getRegistry(), "glitchfloppy");
		register(e.getRegistry(), "glitchboot");
		register(e.getRegistry(), "convert");
		register(e.getRegistry(), "glitchtravel");
		register(e.getRegistry(), "automaton_hurt");
		register(e.getRegistry(), "automaton_idle");
		register(e.getRegistry(), "drive_disassemble");
		register(e.getRegistry(), "data_core_shatter");
		register(e.getRegistry(), "enceladus");
	}
	
	private static void register(IForgeRegistry<SoundEvent> reg, String name) {
		ResourceLocation loc = new ResourceLocation("correlated", name);
		SoundEvent snd = new SoundEvent(loc);
		reg.register(snd.setRegistryName(loc));
		try {
			CSoundEvents.class.getField(name.toUpperCase(Locale.ROOT)).set(null, snd);
		} catch (NoSuchFieldException e) {
			throw new AssertionError("Missing field for sound event "+name);
		} catch (Exception e) {
			CLog.warn("Unexpected error while filling field for sound event {}", name, e);
		}
	}

}
