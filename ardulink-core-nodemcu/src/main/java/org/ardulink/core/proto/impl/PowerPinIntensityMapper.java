/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.ardulink.core.proto.impl;

import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.util.Map;

import org.ardulink.util.MapBuilder;

public class PowerPinIntensityMapper implements Mapper {

	@Override
	public Map<String, String> buildMap(Integer pin, Object[] values) {
		checkNotNull(pin, "PIN has to be specified");
		checkNotNull(values, "value has to be specified");
		checkArgument(values.length == 1,
				"Mapper %s accept just a value instead of: %s", values.length);
		checkArgument(values[0] instanceof Integer,
				"Mapper %s accept just an Integer value instead of: %s",
				values[0].getClass().getName());

		return MapBuilder.<String, String> newMapBuilder()
				.put("PIN", pin.toString())
				.put("INTENSITY", getIntensity((Integer) values[0])).build();
	}

	private String getIntensity(Integer integer) {
		return integer.toString();
	}

}
