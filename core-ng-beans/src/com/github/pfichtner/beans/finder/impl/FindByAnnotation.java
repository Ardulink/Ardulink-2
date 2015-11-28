package com.github.pfichtner.beans.finder.impl;

import static com.github.pfichtner.beans.finder.impl.ReadMethod.isReadMethod;
import static com.github.pfichtner.beans.finder.impl.WriteMethod.isWriteMethod;
import static java.lang.reflect.Modifier.isPublic;
import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;
import com.github.pfichtner.beans.finder.api.AttributeFinder;

public class FindByAnnotation implements AttributeFinder {

	public static class AttributeReaderDelegate implements AttributeReader {

		private final AttributeReader delegate;
		private final String name;

		public AttributeReaderDelegate(AttributeReader delegate, String name) {
			this.delegate = delegate;
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class<?> getType() {
			return delegate.getType();
		}

		@Override
		public Object getValue() throws Exception {
			return delegate.getValue();
		}

	}

	public static class AttributeWriterDelegate implements AttributeWriter {

		private final AttributeWriter delegate;
		private final String name;

		public AttributeWriterDelegate(AttributeWriter delegate, String name) {
			this.delegate = delegate;
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class<?> getType() {
			return delegate.getType();
		}

		@Override
		public void setValue(Object value) throws Exception {
			delegate.setValue(value);
		}

	}

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

	public static AttributeFinder propertyAnnotated(
			Class<? extends Annotation> annotationClass) {
		return propertyAnnotated(annotationClass, "value");
	}

	public static AttributeFinder propertyAnnotated(
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
				readers.add(new ReadMethod(bean, annoValue(method
						.getAnnotation(annotationClass)), method));
			}
		}

		for (Field field : bean.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(annotationClass)) {
				AttributeReader readMethodForAttribute = readMethodForAttribute(
						bean, field.getName());
				if (readMethodForAttribute != null) {
					readers.add(new AttributeReaderDelegate(
							readMethodForAttribute, annoValue(field
									.getAnnotation(annotationClass))));
				} else if (isPublic(field.getModifiers())) {
					readers.add(new FieldAccess(bean, annoValue(field
							.getAnnotation(annotationClass)), field));
				}
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
				writers.add(new WriteMethod(bean, annoValue(method
						.getAnnotation(annotationClass)), method));
			}
		}

		for (Field field : bean.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(annotationClass)) {
				AttributeWriter writeMethodForAttribute = writeMethodForAttribute(
						bean, field.getName());
				if (writeMethodForAttribute != null) {
					writers.add(new AttributeWriterDelegate(
							writeMethodForAttribute, annoValue(field
									.getAnnotation(annotationClass))));
				} else if (isPublic(field.getModifiers())) {
					writers.add(new FieldAccess(bean, annoValue(field
							.getAnnotation(annotationClass)), field));
				}

			}
		}
		return writers;
	}

	private AttributeReader readMethodForAttribute(Object bean,
			final String name) throws Exception {
		for (AttributeReader reader : FindByIntrospection.beanAttributes()
				.listReaders(bean)) {
			if (reader.getName().equals(name)) {
				return reader;
			}
		}
		return null;

	}

	private AttributeWriter writeMethodForAttribute(Object bean,
			final String name) throws Exception {
		for (AttributeWriter writer : FindByIntrospection.beanAttributes()
				.listWriters(bean)) {
			if (writer.getName().equals(name)) {
				return writer;
			}
		}
		return null;

	}

	private String annoValue(Annotation annotation)
			throws IllegalAccessException, InvocationTargetException {
		return (String) getAnnotationsAttributeReadMethod.invoke(annotation);
	}

}
