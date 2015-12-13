package org.zu.ardulink.util;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public final class Longs {

	private Longs() {
		super();
	}

	public static Long tryParse(String string) {
		try {
			return Long.valueOf(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
