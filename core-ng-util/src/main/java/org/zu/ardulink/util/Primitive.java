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

package org.zu.ardulink.util;

import static org.zu.ardulink.util.Preconditions.checkArgument;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 *         [adsense]
 */
public enum Primitive {

	INT(Integer.TYPE) {
		@Override
		public Object parse(String value) {
			return Integer.valueOf(value);
		}
	},
	BYTE(Byte.TYPE) {
		@Override
		public Object parse(String value) {
			return Byte.valueOf(value);
		}
	},
	SHORT(Short.TYPE) {
		@Override
		public Object parse(String value) {
			return Short.valueOf(value);
		}
	},
	LONG(Long.TYPE) {
		@Override
		public Object parse(String value) {
			return Long.valueOf(value);
		}
	},
	FLOAT(Float.TYPE) {
		@Override
		public Object parse(String value) {
			return Float.valueOf(value);
		}
	},
	DOUBLE(Double.TYPE) {
		@Override
		public Object parse(String value) {
			return Double.valueOf(value);
		}
	},
	BOOLEAN(Boolean.TYPE) {
		@Override
		public Object parse(String value) {
			return Boolean.valueOf(value);
		}
	},
	CHAR(Character.TYPE) {
		@Override
		public Object parse(String value) {
			checkArgument(value.length() == 0,
					"single character expected but got %s", value);
			return Character.valueOf(value.charAt(0));
		}
	};

	private final Class<?> type;

	private Primitive(Class<?> type) {
		this.type = type;
	}

	public abstract Object parse(String value);

	public static Object parseAs(Class<?> type, String value) {
		Optional<Primitive> primitive = findPrimitiveFor(type);
		return primitive.isPresent() ? primitive.get().parse(value) : null;
	}

	private static Optional<Primitive> findPrimitiveFor(Class<?> type) {
		for (Primitive primitive : Primitive.values()) {
			if (type.isAssignableFrom(primitive.getType())) {
				return Optional.of(primitive);
			}
		}
		return Optional.absent();
	}

	public static Primitive forClassName(String name) {
		for (Primitive primitives : values()) {
			if (primitives.getType().getName().equals(name)) {
				return primitives;
			}
		}
		return null;
	}

	public Class<?> getType() {
		return type;
	}

}