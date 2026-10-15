package org.ardulink.core.mqtt;

import static org.ardulink.util.Enums.mapInstances;
import static org.ardulink.util.Preconditions.checkArgument;

import java.util.Map;

public enum Qos {

	AT_MOST_ONCE(0), AT_LEAST_ONCE(1), EXACTLY_ONCE(2);

	public static final Qos DEFAULT = AT_MOST_ONCE;
	private final int intValue;

	private static final Map<Integer, Qos> byIntValue = mapInstances(Qos.class, Qos::intValue);

	Qos(int intValue) {
		this.intValue = intValue;
	}

	public int intValue() {
		return intValue;
	}

	public static Qos forInt(int level) {
		Qos qos = byIntValue.get(level);
		checkArgument(qos != null, "Invalid qos level %s", level);
		return qos;
	}

}