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

package org.ardulink.core.convenience;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ardulink.core.linkmanager.LinkManager.Configurer;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class CacheKey {

	private final Class<? extends Configurer> configurerType;
	private final Map<String, Object> values;

	public CacheKey(Configurer configurer) throws Exception {
		this.configurerType = configurer.getClass();
		this.values = Collections.unmodifiableMap(extractData(configurer));
	}

	private Map<String, Object> extractData(Configurer configurer) {
		Map<String, Object> values = new HashMap<String, Object>();
		for (String attribute : configurer.getAttributes()) {
			values.put(attribute, configurer.getAttribute(attribute).getValue());
		}
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((configurerType == null) ? 0 : configurerType.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CacheKey other = (CacheKey) obj;
		if (configurerType == null) {
			if (other.configurerType != null)
				return false;
		} else if (!configurerType.equals(other.configurerType))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CacheKey [configurerType=" + configurerType + ", values="
				+ values + "]";
	}

}