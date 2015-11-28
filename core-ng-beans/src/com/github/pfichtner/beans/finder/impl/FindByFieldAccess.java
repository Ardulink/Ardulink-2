package com.github.pfichtner.beans.finder.impl;

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;
import com.github.pfichtner.beans.finder.api.AttributeFinder;

public class FindByFieldAccess implements AttributeFinder {

	public static class FieldAccess implements AttributeReader, AttributeWriter {

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

	private FindByFieldAccess() {
		super();
	}

	public static AttributeFinder directFieldAccess() {
		return new FindByFieldAccess();
	}

	@Override
	public Iterable<FieldAccess> listReaders(Object bean) throws Exception {
		return find(bean);
	}

	@Override
	public Iterable<FieldAccess> listWriters(Object bean) throws Exception {
		return find(bean);
	}

	private Iterable<FieldAccess> find(Object bean) {
		List<FieldAccess> accessors = new ArrayList<FieldAccess>();
		for (Field field : bean.getClass().getDeclaredFields()) {
			if (isPublic(field.getModifiers())) {
				accessors.add(new FieldAccess(bean, field.getName(), field));
			}
		}
		return accessors;
	}

}
