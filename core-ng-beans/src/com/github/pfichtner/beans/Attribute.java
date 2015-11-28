package com.github.pfichtner.beans;

public interface Attribute {

	public interface TypedAttributeProvider {
		String getName();

		Class<?> getType();
	}

	public interface AttributeReader extends TypedAttributeProvider {
		Object getValue() throws Exception;
	}

	public interface AttributeWriter extends TypedAttributeProvider {
		void setValue(Object value) throws Exception;
	}

	String getName();

	Class<?> getType();

	boolean canRead();

	Object readValue() throws Exception;

	boolean canWrite();

	void writeValue(Object value) throws Exception;

}
