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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ardulink.core.proto.api.Protocol.FromArduino;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class FromArduinoReply implements FromArduino {

	private final boolean ok;
	private final long id;
	private Map<String, Object> parameters;

	public FromArduinoReply(boolean ok, long id, Map<String, Object> parameters) {
		this.ok = ok;
		this.id = id;
		this.parameters = Collections
				.unmodifiableMap(new HashMap<String, Object>(parameters));
	}

	public boolean isOk() {
		return ok;
	}

	public long getId() {
		return id;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

}
