package org.ardulink.core.mqtt;

import java.util.EnumSet;

public enum Qos {

	AT_MOST_ONCE(0), AT_LEAST_ONCE(1), EXACTLY_ONCE(2);

	public static final Qos DEFAULT = AT_MOST_ONCE;
	private final int intValue;

	private Qos(int intValue) {
		this.intValue = intValue;
	}

	public int intValue() {
		return intValue;
	}

	public static Qos forInt(int level) {
		return EnumSet.allOf(Qos.class).stream().filter(q -> q.intValue() == level).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid qos level " + level));
	}

}