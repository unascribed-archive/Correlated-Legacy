package com.elytradev.correlated.helper;

import java.text.NumberFormat;

import com.elytradev.correlated.C28n;

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
		if (bits == 1) return C28n.format("numbers.correlated.bit");
		if (magnitude < 8) {
			return C28n.format("numbers.correlated.bits", bits);
		}
		return humanReadableBytes(bits/8);
	}
	public static String humanReadableBytes(long bytes) {
		long magnitude = Math.abs(bytes);
		if (bytes == 1) return C28n.format("numbers.correlated.byte");
		if (magnitude >= EXBIBYTE) {
			return C28n.format("numbers.correlated.exbibytes", bytes/EXBIBYTE);
		} else if (magnitude >= PEBIBYTE) {
			return C28n.format("numbers.correlated.pebibytes", bytes/PEBIBYTE);
		} else if (magnitude >= TEBIBYTE) {
			return C28n.format("numbers.correlated.tebibytes", bytes/TEBIBYTE);
		} else if (magnitude >= GIBIBYTE) {
			return C28n.format("numbers.correlated.gibibytes", bytes/GIBIBYTE);
		} else if (magnitude >= MEBIBYTE) {
			return C28n.format("numbers.correlated.mebibytes", bytes/MEBIBYTE);
		} else if (magnitude >= KIBIBYTE) {
			return C28n.format("numbers.correlated.kibibytes", bytes/KIBIBYTE);
		}
		return C28n.format("numbers.correlated.bytes", bytes);
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
			return C28n.format("numbers.correlated.giga", formatter.format((double)count/GIGA));
		} else if (count >= MEGA) {
			return C28n.format("numbers.correlated.mega", formatter.format((double)count/MEGA));
		} else if (count >= 10_000) {
			return C28n.format("numbers.correlated.kilo", formatter.format((double)count/KILO));
		}
		return C28n.format("numbers.correlated.normal", count);
	}
}
