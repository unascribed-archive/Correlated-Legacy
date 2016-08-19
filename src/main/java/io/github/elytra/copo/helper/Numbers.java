package io.github.elytra.copo.helper;

import java.text.NumberFormat;

import net.minecraft.client.resources.I18n;

public class Numbers {
	public static final int GIBIBYTE = 1024*1024*1024;
	public static final int MIBIBYTE = 1024*1024;
	public static final int KIBIBYTE = 1024;
	public static String humanReadableBytes(long bytes) {
		if (bytes == 1) return I18n.format("numbers.correlatedpotentialistics.byte");
		if (bytes >= GIBIBYTE) {
			return I18n.format("numbers.correlatedpotentialistics.gibibytes", bytes/GIBIBYTE);
		} else if (bytes >= MIBIBYTE) {
			return I18n.format("numbers.correlatedpotentialistics.mibibytes", bytes/MIBIBYTE);
		} else if (bytes >= KIBIBYTE) {
			return I18n.format("numbers.correlatedpotentialistics.kibibytes", bytes/KIBIBYTE);
		}
		return I18n.format("numbers.correlatedpotentialistics.bytes", bytes);
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
			return I18n.format("numbers.correlatedpotentialistics.giga", formatter.format((double)count/GIGA));
		} else if (count >= MEGA) {
			return I18n.format("numbers.correlatedpotentialistics.mega", formatter.format((double)count/MEGA));
		} else if (count >= 10_000) {
			return I18n.format("numbers.correlatedpotentialistics.kilo", formatter.format((double)count/KILO));
		}
		return I18n.format("numbers.correlatedpotentialistics.normal", count);
	}
}
