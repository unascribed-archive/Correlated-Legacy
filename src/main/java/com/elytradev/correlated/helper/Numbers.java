package com.elytradev.correlated.helper;

import java.text.NumberFormat;

import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class Numbers {
	public static final long EXBIBYTE = 1024L*1024L*1024L*1024L*1024L*1024L;
	public static final long PEBIBYTE = 1024L*1024L*1024L*1024L*1024L;
	public static final long TEBIBYTE = 1024L*1024L*1024L*1024L;
	public static final long GIBIBYTE = 1024L*1024L*1024L;
	public static final long MEBIBYTE = 1024L*1024L;
	public static final long KIBIBYTE = 1024L;
	public static String humanReadableBits(long bits) {
		long magnitude = Math.abs(bits);
		if (bits == 1) return I18n.translateToLocal("numbers.correlated.bit");
		if (magnitude < 8) {
			return I18n.translateToLocalFormatted("numbers.correlated.bits", bits);
		}
		return humanReadableBytes(bits/8);
	}
	public static String humanReadableBytes(long bytes) {
		long magnitude = Math.abs(bytes);
		if (bytes == 1) return I18n.translateToLocal("numbers.correlated.byte");
		if (magnitude >= EXBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.exbibytes", bytes/EXBIBYTE);
		} else if (magnitude >= PEBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.pebibytes", bytes/PEBIBYTE);
		} else if (magnitude >= TEBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.tebibytes", bytes/TEBIBYTE);
		} else if (magnitude >= GIBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.gibibytes", bytes/GIBIBYTE);
		} else if (magnitude >= MEBIBYTE) {
			return I18n.translateToLocalFormatted("numbers.correlated.mebibytes", bytes/MEBIBYTE);
		} else if (magnitude >= KIBIBYTE) {
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
