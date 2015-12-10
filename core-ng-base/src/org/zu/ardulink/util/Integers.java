package org.zu.ardulink.util;

import java.util.Collection;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public final class Integers {
	
	private Integers() {
		super();
	}

	public static Integer tryParse(String string) {
		try {
			return Integer.valueOf(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public static int average(Collection<Integer> values) {
		return sum(values) / values.size();
	}

	public static int sum(Iterable<Integer> values) {
		int sum = 0;
		for (Integer integer : values) {
			sum += integer;
		}
		return sum;
	}

}
