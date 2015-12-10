package org.zu.ardulink.util;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public enum Primitive {

	INT(Integer.TYPE) {
		@Override
		public Object parse(String value) {
			return Integer.parseInt(value);
		}
	},
	BYTE(Byte.TYPE) {
		@Override
		public Object parse(String value) {
			return Byte.parseByte(value);
		}
	},
	SHORT(Short.TYPE) {
		@Override
		public Object parse(String value) {
			return Short.parseShort(value);
		}
	},
	LONG(Long.TYPE) {
		@Override
		public Object parse(String value) {
			return Long.parseLong(value);
		}
	},
	FLOAT(Float.TYPE) {
		@Override
		public Object parse(String value) {
			return Float.parseFloat(value);
		}
	},
	DOUBLE(Double.TYPE) {
		@Override
		public Object parse(String value) {
			return Double.parseDouble(value);
		}
	},
	BOOLEAN(Boolean.TYPE) {
		@Override
		public Object parse(String value) {
			return Boolean.parseBoolean(value);
		}
	},
	CHAR(Character.TYPE) {
		@Override
		public Object parse(String value) {
			return value.length() == 0 ? null : Character.valueOf(value
					.charAt(0));
		}
	};

	private final Class<?> type;

	private Primitive(Class<?> type) {
		this.type = type;
	}

	public abstract Object parse(String value);

	public static Object parseAs(Class<?> type, String value) {
		for (Primitive primitive : Primitive.values()) {
			if (type.isAssignableFrom(primitive.getType())) {
				return primitive.parse(value);
			}
		}
		return null;
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