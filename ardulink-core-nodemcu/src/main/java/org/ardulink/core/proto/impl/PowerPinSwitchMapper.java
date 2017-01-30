package org.ardulink.core.proto.impl;

import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

public class PowerPinSwitchMapper implements Mapper {

	@Override
	public Map<String, String> buildMap(Integer pin, Object[] values) {
		
		checkNotNull(pin, "PIN has to be specified");
		checkNotNull(values, "value has to be specified");
		checkArgument(values.length == 1, "Mapper %s accept just a value instead of: %s", values.length);
		checkArgument(values[0] instanceof Boolean, "Mapper %s accept just a Boolean value instead of: %s", values[0].getClass().getName());
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("PIN", pin.toString());
		map.put("STATE", getState((Boolean)values[0]));
		return map;
	}

	private String getState(Boolean state) {
		return (state) ? "HIGH" : "LOW";
	}

}
