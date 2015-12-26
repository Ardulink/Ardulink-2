package org.zu.ardulink.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class Joiner {

	public static class MapJoiner {

		private final String separator;
		private final String kvSeparator;

		public MapJoiner(String separator, String kvSeparator) {
			this.separator = separator;
			this.kvSeparator = kvSeparator;
		}

		public String join(Map<?, ?> map) {
			Set<?> keySet = map.entrySet();
			Iterator<Entry<Object, Object>> it = (Iterator<Entry<Object, Object>>) keySet
					.iterator();
			if (!it.hasNext()) {
				return "";
			}
			StringBuilder sb = append(it, new StringBuilder());
			while (it.hasNext()) {
				sb = append(it, sb.append(separator));
			}
			return sb.toString();
		}

		private StringBuilder append(Iterator<Entry<Object, Object>> iterator,
				StringBuilder sb) {
			Entry<Object, Object> entry = iterator.next();
			return sb.append(entry.getKey()).append(kvSeparator)
					.append(entry.getValue());
		}

	}

	private final String separator;

	private Joiner(String separator) {
		this.separator = separator;
	}

	public static Joiner on(String separator) {
		return new Joiner(separator);
	}

	public String join(Iterable<? extends Object> values) {
		Iterator<? extends Object> iterator = values.iterator();
		if (!iterator.hasNext()) {
			return "";
		}
		StringBuilder sb = new StringBuilder().append(iterator.next());
		while (iterator.hasNext()) {
			sb = sb.append(separator).append(iterator.next());
		}
		return sb.toString();
	}

	public MapJoiner withKeyValueSeparator(String kvSeparator) {
		return new MapJoiner(this.separator, kvSeparator);
	}

}
