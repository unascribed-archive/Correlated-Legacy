package com.elytradev.correlated.helper;

import java.text.NumberFormat;

import net.minecraft.util.text.translation.I18n;

public class Numbers {
	public static final long EXBIBYTE = 1024L*1024L*1024L*1024L*1024L*1024L;
	public static final long PEBIBYTE = 1024L*1024L*1024L*1024L*1024L;
	public static final long TEBIBYTE = 1024L*1024L*1024L*1024L;
	public static final long GIBIBYTE = 1024L*1024L*1024L;
	public static final long MEBIBYTE = 1024L*1024L;
	public static final long KIBIBYTE = 1024L;
	public static String humanReadableBytes(long bytes) {
		if (bytes == 1) return I18n.translateToLocal("numbers.correlated.byte");
		if (bytes >= EXBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.exbibytes", bytes/EXBIBYTE);
		} else if (bytes >= PEBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.pebibytes", bytes/PEBIBYTE);
		} else if (bytes >= TEBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.tebibytes", bytes/TEBIBYTE);
		} else if (bytes >= GIBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.gibibytes", bytes/GIBIBYTE);
		} else if (bytes >= MEBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.mebibytes", bytes/MEBIBYTE);
		} else if (bytes >= KIBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.kibibytes", bytes/KIBIBYTE);
		}
		return I18n.translateToLocalFormatted("numbers.correlated.bytes", bytes);
	}

	public static final int GIGA = 1_000_000_000;
	public static final int MEGA = 1_000_000;
	public static final int KILO = 1_000;

	private static final NumberFormat formatter = NumberFormat.getNumberInstance();
	private static boolean formatterInitialized = false;
	public static String humanReadableItemCount(int count) {
		if (!formatterInitialized) {
			formatterInitialized = true;
			formatter.setMinimumFractionDigits(1);
			formatter.setMaximumFractionDigits(1);
		}
		if (count >= GIGA) {
			return I18n.translateToLocalFormatted("numbers.correlated.giga", formatter.format((double)count/GIGA));
		} else if (count >= MEGA) {
			return I18n.translateToLocalFormatted("numbers.correlated.mega", formatter.format((double)count/MEGA));
		} else if (count >= 10_000) {
			return I18n.translateToLocalFormatted("numbers.correlated.kilo", formatter.format((double)count/KILO));
		}
		return I18n.translateToLocalFormatted("numbers.correlated.normal", count);
	}
}
