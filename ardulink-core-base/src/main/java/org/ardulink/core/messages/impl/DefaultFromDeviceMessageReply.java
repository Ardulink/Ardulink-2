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

package org.ardulink.core.messages.impl;

import java.util.HashMap;
import java.util.Map;

import org.ardulink.core.messages.api.FromDeviceMessageReply;
import static java.util.Collections.unmodifiableMap;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DefaultFromDeviceMessageReply implements FromDeviceMessageReply {

	private final boolean ok;
	private final long id;
	private final Map<String, ? extends Object> parameters;

	public DefaultFromDeviceMessageReply(boolean ok, long id,
			Map<String, ? extends Object> parameters) {
		this.ok = ok;
		this.id = id;
		this.parameters = unmodifiableMap(new HashMap<String, Object>(
				parameters));
	}

	public boolean isOk() {
		return ok;
	}

	public long getId() {
		return id;
	}

	@Override
	public Map<String, ? extends Object> getParameters() {
		return parameters;
	}

}
