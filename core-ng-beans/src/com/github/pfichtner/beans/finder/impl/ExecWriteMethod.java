package com.github.pfichtner.beans.finder.impl;

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.pfichtner.beans.Attribute.AttributeWriter;

public class ExecWriteMethod implements AttributeWriter {

	private final Object bean;
	private final Method writeMethod;

	public ExecWriteMethod(Object bean, Method writeMethod) {
		this.bean = bean;
		this.writeMethod = writeMethod;
	}

	@Override
	public void setValue(Object value) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		writeMethod.invoke(bean, value);
	}

	@Override
	public Class<?> getType() {
		return writeMethod.getParameterTypes()[0];
	}

	public static boolean isWriteMethod(Method method) {
		return method != null && isPublic(method.getModifiers())
				&& method.getParameterTypes().length == 1;
	}

}