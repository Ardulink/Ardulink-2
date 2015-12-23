package org.zu.ardulink.util;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public final class Strings {

	private Strings() {
		super();
	}

	public static boolean nullOrEmpty(String string) {
		return string == null || string.isEmpty();
	}
	
	public static String bytes2String(byte in) {
		return bytes2String(new byte[]{in});
	}

	public static String bytes2String(byte[] in) {
		return new String(in);
	}

	public static int[] string2Ints(String in) {
		byte[] bytes = in.getBytes();
		int[]  retvalue = new int[bytes.length];
		for (int i = 0; i < retvalue.length; i++) {
			retvalue[i] = bytes[i];
		}
		return retvalue;
	}
}
