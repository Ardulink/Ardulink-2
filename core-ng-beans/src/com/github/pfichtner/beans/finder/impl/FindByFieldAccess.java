package com.github.pfichtner.beans.finder.impl;

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.beans.finder.api.AttributeFinder;

public class FindByFieldAccess implements AttributeFinder {

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
