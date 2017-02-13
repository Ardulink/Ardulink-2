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
import static org.ardulink.util.Preconditions.checkNull;

import java.util.HashMap;
import java.util.Map;

public class CustomMessageMapper implements Mapper {

	private static final String separator = " ";
	
	@Override
	public Map<String, String> buildMap(Integer pin, Object[] values) {
		checkNull(pin, "PIN has to be null");
		checkNotNull(values, "value has to be specified");
		checkArgument(values.length > 0, "Mapper %s accepts a least a value instead of: %s", values.length);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("VALUES", getValues(values));
		return map;
	}

	private String getValues(Object[] values) {
		StringBuilder builder = new StringBuilder(values[0].toString());
		for (int i = 1; i < values.length; i++) {
			builder.append(separator);
			builder.append(values[i].toString());
		}
		return builder.toString();
	}

}
