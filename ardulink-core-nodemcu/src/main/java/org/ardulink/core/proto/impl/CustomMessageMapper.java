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

import static org.ardulink.core.proto.impl.LuaProtoBuilder.PIN;
import static org.ardulink.core.proto.impl.LuaProtoBuilder.VALUES;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.util.Arrays;
import java.util.Map;

import org.ardulink.util.Joiner;
import org.ardulink.util.MapBuilder;

public class CustomMessageMapper implements Mapper {

	private static final Joiner joiner = Joiner.on(" ");

	@Override
	public Map<String, String> buildMap(Integer pin, Object[] values) {
		checkState(pin == null, PIN + " has to be null");
		checkNotNull(values, "value has to be specified");
		checkArgument(values.length > 0,
				"Mapper %s accepts a least a value instead of: %s",
				values.length);

		return MapBuilder.<String, String> newMapBuilder()
				.put(VALUES, joiner.join(Arrays.asList(values))).build();
	}

}
