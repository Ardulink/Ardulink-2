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

package org.ardulink.util;

import static org.ardulink.util.Preconditions.checkArgument;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public enum Primitive {

	INT(Integer.TYPE, Integer.class) {
		@Override
		public Object parse(String value) {
			return Integer.valueOf(value);
		}
	},
	BYTE(Byte.TYPE, Byte.class) {
		@Override
		public Object parse(String value) {
			return Byte.valueOf(value);
		}
	},
	SHORT(Short.TYPE, Short.class) {
		@Override
		public Object parse(String value) {
			return Short.valueOf(value);
		}
	},
	LONG(Long.TYPE, Long.class) {
		@Override
		public Object parse(String value) {
			return Long.valueOf(value);
		}
	},
	FLOAT(Float.TYPE, Float.class) {
		@Override
		public Object parse(String value) {
			return Float.valueOf(value);
		}
	},
	DOUBLE(Double.TYPE, Double.class) {
		@Override
		public Object parse(String value) {
			return Double.valueOf(value);
		}
	},
	BOOLEAN(Boolean.TYPE, Boolean.class) {
		@Override
		public Object parse(String value) {
			return Boolean.valueOf(value);
		}
	},
	CHAR(Character.TYPE, Character.class) {
		@Override
		public Object parse(String value) {
			checkArgument(value.length() == 0,
					"single character expected but got %s", value);
			return Character.valueOf(value.charAt(0));
		}
	};

	private final Class<?> type;
	private final Class<?> wrapperType;

	private Primitive(Class<?> type, Class<?> wrapperType) {
		this.type = type;
		this.wrapperType = wrapperType;
	}

	public abstract Object parse(String value);

	public Class<?> getWrapperType() {
		return wrapperType;
	}

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

	public static boolean isWrapperType(Class<?> clazz) {
		for (Primitive primitive : values()) {
			if (clazz.equals(primitive.getWrapperType())) {
				return true;
			}
		}
		return false;
	}

	public static Collection<Class<?>> allPrimitiveTypes() {
		Set<Class<?>> primitives = new HashSet<Class<?>>();
		for (Primitive primitive : values()) {
			primitives.add(primitive.getType());
		}
		return primitives;
	}

	public static Class<?> unwrap(Class<?> clazz) {
		for (Primitive primitive : values()) {
			if (clazz.equals(primitive.getWrapperType())) {
				return primitive.getType();
			}
		}
		return clazz;
	}

	public static Class<?> wrap(Class<?> clazz) {
		for (Primitive primitive : values()) {
			if (clazz.equals(primitive.getType())) {
				return primitive.getWrapperType();
			}
		}
		return clazz;
	}

}