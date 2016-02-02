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