package com.github.pfichtner.ardulink.core.convenience;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

class CacheKey {

	private final Map<String, Object> values;

	public CacheKey(Configurer configurer) throws Exception {
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
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CacheKey [values=" + values + "]";
	}

}