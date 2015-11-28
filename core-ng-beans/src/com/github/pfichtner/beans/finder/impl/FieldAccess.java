package com.github.pfichtner.beans.finder.impl;

import java.lang.reflect.Field;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;

public class FieldAccess implements AttributeReader, AttributeWriter {

	private final Object bean;
	private final String name;
	private final Field field;

	public FieldAccess(Object bean, String name, Field field) {
		this.bean = bean;
		this.name = name;
		this.field = field;
	}

	@Override
	public Object getValue() throws IllegalArgumentException,
			IllegalAccessException {
		return field.get(bean);
	}

	@Override
	public void setValue(Object value) throws IllegalArgumentException,
			IllegalAccessException {
		field.set(bean, value);
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	@Override
	public String getName() {
		return this.name;
	}

}