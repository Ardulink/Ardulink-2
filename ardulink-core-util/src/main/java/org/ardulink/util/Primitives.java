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
import static org.ardulink.util.anno.LapsedWith.JDK8;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public enum Primitives {

	INT(Integer.TYPE, Integer.class) {
		@Override
		public Integer parse(String value) {
			return Integer.valueOf(value);
		}
	},
	BYTE(Byte.TYPE, Byte.class) {
		@Override
		public Byte parse(String value) {
			return Byte.valueOf(value);
		}
	},
	SHORT(Short.TYPE, Short.class) {
		@Override
		public Short parse(String value) {
			return Short.valueOf(value);
		}
	},
	LONG(Long.TYPE, Long.class) {
		@Override
		public Long parse(String value) {
			return Long.valueOf(value);
		}
	},
	FLOAT(Float.TYPE, Float.class) {
		@Override
		public Float parse(String value) {
			return Float.valueOf(value);
		}
	},
	DOUBLE(Double.TYPE, Double.class) {
		@Override
		public Double parse(String value) {
			return Double.valueOf(value);
		}
	},
	BOOLEAN(Boolean.TYPE, Boolean.class) {
		@Override
		public Boolean parse(String value) {
			return Boolean.valueOf(value);
		}
	},
	CHAR(Character.TYPE, Character.class) {
		@Override
		public Character parse(String value) {
			checkArgument(value.length() == 1,
					"single character expected but got %s", value);
			return Character.valueOf(value.charAt(0));
		}
	};

	private final Class<?> type;
	private final Class<?> wrapperType;

	private Primitives(Class<?> type, Class<?> wrapperType) {
		this.type = type;
		this.wrapperType = wrapperType;
	}

	public abstract Object parse(String value);

	public Class<?> getWrapperType() {
		return wrapperType;
	}

	@LapsedWith(value = JDK8, module = "Optional#map")
	public static Object parseAs(Class<?> type, String value) {
		Optional<Primitives> primitive = findPrimitiveFor(type);
		return primitive.isPresent() ? primitive.get().parse(value) : null;
	}

	@LapsedWith(value = JDK8, module = "Streams")
	private static Optional<Primitives> findPrimitiveFor(Class<?> type) {
		for (Primitives primitive : Primitives.values()) {
			if (type.isAssignableFrom(primitive.getType())) {
				return Optional.of(primitive);
			}
		}
		return Optional.absent();
	}

	@LapsedWith(value = JDK8, module = "Streams")
	public static Primitives forClassName(String name) {
		for (Primitives primitives : values()) {
			if (primitives.getType().getName().equals(name)) {
				return primitives;
			}
		}
		return null;
	}

	public Class<?> getType() {
		return type;
	}

	@LapsedWith(value = JDK8, module = "Streams")
	public static boolean isWrapperType(Class<?> clazz) {
		for (Primitives primitive : values()) {
			if (clazz.equals(primitive.getWrapperType())) {
				return true;
			}
		}
		return false;
	}

	@LapsedWith(value = JDK8, module = "Streams")
	public static Collection<Class<?>> allPrimitiveTypes() {
		Set<Class<?>> primitives = new HashSet<Class<?>>();
		for (Primitives primitive : values()) {
			primitives.add(primitive.getType());
		}
		return primitives;
	}

	@LapsedWith(value = JDK8, module = "Streams")
	public static Class<?> unwrap(Class<?> clazz) {
		for (Primitives primitive : values()) {
			if (clazz.equals(primitive.getWrapperType())) {
				return primitive.getType();
			}
		}
		return clazz;
	}

	@LapsedWith(value = JDK8, module = "Streams")
	public static Class<?> wrap(Class<?> clazz) {
		for (Primitives primitive : values()) {
			if (clazz.equals(primitive.getType())) {
				return primitive.getWrapperType();
			}
		}
		return clazz;
	}

}