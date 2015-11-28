package com.github.pfichtner.beans.finder.impl;

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.pfichtner.beans.Attribute.AttributeReader;

public class ReadMethod implements AttributeReader {

	private final Object bean;
	private final String name;
	private final Method readMethod;

	public ReadMethod(Object bean, String name, Method readMethod) {
		this.bean = bean;
		this.name = name;
		this.readMethod = readMethod;
	}

	@Override
	public Object getValue() throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return readMethod.invoke(bean);
	}

	@Override
	public Class<?> getType() {
		return readMethod.getReturnType();
	}

	@Override
	public String getName() {
		return this.name;
	}

	public static boolean isReadMethod(Method method) {
		return method != null && isPublic(method.getModifiers())
				&& !anObjectsMethod(method)
				&& method.getParameterTypes().length == 0;
	}

	private static boolean anObjectsMethod(Method method) {
		return method.getDeclaringClass().equals(Object.class);
	}

}