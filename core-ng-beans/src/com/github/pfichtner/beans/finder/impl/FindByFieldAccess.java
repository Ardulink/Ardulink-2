package com.github.pfichtner.beans.finder.impl;

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.Field;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;
import com.github.pfichtner.beans.finder.api.AttributeFinder;

public class FindByFieldAccess implements AttributeFinder {

	public static class FieldAccess implements AttributeReader, AttributeWriter {

		private final Object bean;
		private final Field field;

		public FieldAccess(Object bean, Field field) {
			this.bean = bean;
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

	}

	private FindByFieldAccess() {
		super();
	}

	public static AttributeFinder directFieldAccess() {
		return new FindByFieldAccess();
	}

	@Override
	public AttributeReader findReader(Object bean, String name)
			throws Exception {
		return find(bean, name);
	}

	@Override
	public FieldAccess findWriter(Object bean, String name) throws Exception {
		return find(bean, name);
	}

	private FieldAccess find(Object bean, String name) {
		for (Field field : bean.getClass().getDeclaredFields()) {
			if (name.equals(field.getName()) && isPublic(field.getModifiers())) {
				return new FieldAccess(bean, field);
			}
		}
		return null;
	}

}
