package org.ardulink.core.proto.impl;

import static org.ardulink.core.proto.impl.LuaProtoBuilder.PIN;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.util.Map;

import org.ardulink.util.MapBuilder;

public class StopListeningDigitalMapper implements Mapper {

	@Override
	public Map<String, String> buildMap(Integer pin, Object[] values) {
		checkNotNull(pin, PIN + " has to be specified");
		checkState(values == null, "value hasn't to be specified");

		return MapBuilder.<String, String> newMapBuilder()
				.put(PIN, String.valueOf(pin)).build();
	}

}
