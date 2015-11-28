package com.github.pfichtner.beans.finder.impl;

import static com.github.pfichtner.beans.finder.impl.ExecReadMethod.isReadMethod;
import static com.github.pfichtner.beans.finder.impl.ExecWriteMethod.isWriteMethod;
import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;
import com.github.pfichtner.beans.finder.api.AttributeFinder;

public class FindByAnnotation implements AttributeFinder {

	private final Class<? extends Annotation> annotationClass;
	private final Method getAnnotationsAttributeReadMethod;

	private FindByAnnotation(Class<? extends Annotation> annotationClass,
			String annotationAttribute) {
		this.annotationClass = annotationClass;
		this.getAnnotationsAttributeReadMethod = getAttribMethod(
				annotationClass, annotationAttribute);
		Class<?> returnType = this.getAnnotationsAttributeReadMethod
				.getReturnType();
		checkArgument(returnType.equals(String.class),
				"The returntype of %s's %s has to be %s but was %s",
				annotationClass.getName(), annotationAttribute, String.class,
				returnType);
	}

	private Method getAttribMethod(Class<? extends Annotation> annotationClass,
			String annotationAttribute) {
		try {
			return annotationClass.getMethod(annotationAttribute);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(annotationClass.getName()
					+ " has no attribute named " + annotationAttribute);
		}
	}

	public static AttributeFinder methodsAnnotated(
			Class<? extends Annotation> annotationClass) {
		return methodsAnnotated(annotationClass, "value");
	}

	public static AttributeFinder methodsAnnotated(
			Class<? extends Annotation> annotationClass,
			String annotationAttribute) {
		return new FindByAnnotation(annotationClass, annotationAttribute);
	}

	@Override
	public Iterable<? extends AttributeReader> listReaders(Object bean)
			throws Exception {
		List<AttributeReader> readers = new ArrayList<AttributeReader>();
		for (Method method : bean.getClass().getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotationClass)
					&& isReadMethod(method)) {
				readers.add(new ExecReadMethod(bean, annoValue(method
						.getAnnotation(annotationClass)), method));
			}
		}
		return readers;
	}

	@Override
	public Iterable<AttributeWriter> listWriters(Object bean) throws Exception {
		List<AttributeWriter> writers = new ArrayList<AttributeWriter>();
		for (Method method : bean.getClass().getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotationClass)
					&& isWriteMethod(method)) {
				writers.add(new ExecWriteMethod(bean, annoValue(method
						.getAnnotation(annotationClass)), method));
			}
		}
		return writers;
	}

	private String annoValue(Annotation annotation)
			throws IllegalAccessException, InvocationTargetException {
		return (String) getAnnotationsAttributeReadMethod.invoke(annotation);
	}

}
