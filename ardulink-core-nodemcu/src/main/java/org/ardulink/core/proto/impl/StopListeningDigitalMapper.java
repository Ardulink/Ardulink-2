package org.ardulink.core.proto.impl;

import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkNull;

import java.util.HashMap;
import java.util.Map;

public class StopListeningDigitalMapper implements Mapper {

	@Override
	public Map<String, String> buildMap(Integer pin, Object[] values) {

		checkNotNull(pin, "PIN has to be specified");
		checkNull(values, "value hasn't to be specified");
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("PIN", pin.toString());
		return map;
	
	}

}
