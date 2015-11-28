package com.github.pfichtner.beans.finder.impl;

import static com.github.pfichtner.beans.finder.impl.ExecReadMethod.isReadMethod;
import static com.github.pfichtner.beans.finder.impl.ExecWriteMethod.isWriteMethod;
import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.pfichtner.beans.Attribute.AttributeReader;
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
	public AttributeReader findReader(Object bean, String name)
			throws Exception {
		for (Method method : bean.getClass().getDeclaredMethods()) {
			if (isReadMethod(method) && annotationMatches(name, method)) {
				return new ExecReadMethod(bean, method);
			}
		}
		return null;
	}

	@Override
	public ExecWriteMethod findWriter(Object bean, String name)
			throws Exception {
		for (Method method : bean.getClass().getDeclaredMethods()) {
			if (isWriteMethod(method) && annotationMatches(name, method)) {
				return new ExecWriteMethod(bean, method);
			}
		}
		return null;
	}

	private boolean annotationMatches(String name, Method method)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		return method.isAnnotationPresent(annotationClass)
				&& annoValueIsEqualTo(method.getAnnotation(annotationClass),
						name);
	}

	private boolean annoValueIsEqualTo(Annotation annotation, String name)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		return getAnnotationsAttributeReadMethod.invoke(annotation)
				.equals(name);
	}

}
