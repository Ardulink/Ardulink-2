package com.github.pfichtner.beans.finder.impl;

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.pfichtner.beans.Attribute.AttributeReader;

public class ExecReadMethod implements AttributeReader {

	private final Object bean;
	private final Method readMethod;

	public ExecReadMethod(Object bean, Method readMethod) {
		this.bean = bean;
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

	public static boolean isReadMethod(Method method) {
		return method != null && isPublic(method.getModifiers())
				&& method.getParameterTypes().length == 0;
	}

}