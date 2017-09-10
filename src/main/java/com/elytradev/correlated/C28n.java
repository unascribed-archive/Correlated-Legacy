package com.elytradev.correlated;

import java.util.List;

import com.google.common.base.Optional;
import com.ibm.icu.text.PluralRules;

/**
 * CorrelatedInternationalization
 */
public class C28n {

	public static String format(String key, Object... format) {
		return Correlated.proxy.i18nFormat(key, format);
	}
	
	public static boolean contains(String key) {
		return Correlated.proxy.i18nContains(key);
	}
	
	public static Optional<String> formatOptional(String key, Object... format) {
		return contains(key) ? Optional.of(Correlated.proxy.i18nFormat(key, format)) : Optional.absent();
	}
	
	public static String formatPlural(String key, long amount) {
		PluralRules pr = Correlated.proxy.getPluralRules();
		String s = pr.select(amount);
		if (contains(key+"."+s)) {
			return format(key+"."+s, amount);
		}
		return format(key+".other", amount);
	}

	public static void formatList(List<String> out, String key, Object... format) {
		int i = 0;
		while (contains(key + "." + i)) {
			out.add(C28n.format(key + "." + i));
			i++;
		}
	}
	
}
