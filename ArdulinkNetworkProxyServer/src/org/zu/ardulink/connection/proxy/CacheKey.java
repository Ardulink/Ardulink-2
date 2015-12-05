package org.zu.ardulink.connection.proxy;

import java.util.HashMap;
import java.util.Map;

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

public class CacheKey {

	private final Map<String, Object> values = new HashMap<String, Object>();

	public CacheKey(Configurer configurer) throws Exception {
		for (String attribute : configurer.getAttributes()) {
			values.put(attribute, configurer.getAttribute(attribute)
					.getValue());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((values == null) ? 0 : values.hashCode());
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

}