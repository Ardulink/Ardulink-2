/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.github.pfichtner.beans.finder.impl;

import static com.github.pfichtner.beans.finder.impl.ReadMethod.isReadMethod;
import static com.github.pfichtner.beans.finder.impl.WriteMethod.isWriteMethod;
import static java.lang.reflect.Modifier.isPublic;
import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.zu.ardulink.util.Optional;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;
import com.github.pfichtner.beans.finder.api.AttributeFinder;

public class FindByAnnotation implements AttributeFinder {

	public static class AttributeReaderDelegate implements AttributeReader {

		private final AttributeReader delegate;
		private final String name;
		private final Field annoFoundOn;

		public AttributeReaderDelegate(AttributeReader delegate, String name,
				Field annoFoundOn) {
			this.delegate = delegate;
			this.name = name;
			this.annoFoundOn = annoFoundOn;
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

		@Override
		public void addAnnotations(Collection<Annotation> annotations) {
			Collections.addAll(annotations, this.annoFoundOn.getAnnotations());
		}

	}

	public static class AttributeWriterDelegate implements AttributeWriter {

		private final AttributeWriter delegate;
		private final String name;
		private final Field annoFoundOn;

		public AttributeWriterDelegate(AttributeWriter delegate, String name,
				Field annoFoundOn) {
			this.delegate = delegate;
			this.name = name;
			this.annoFoundOn = annoFoundOn;
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

		@Override
		public void addAnnotations(Collection<Annotation> annotations) {
			Collections.addAll(annotations, this.annoFoundOn.getAnnotations());
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
				Optional<AttributeReader> readMethodForAttribute = readMethodForAttribute(
						bean, field.getName());
				if (readMethodForAttribute.isPresent()) {
					readers.add(new AttributeReaderDelegate(
							readMethodForAttribute.get(), annoValue(field
									.getAnnotation(annotationClass)), field));
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
				Optional<AttributeWriter> writeMethodForAttribute = writeMethodForAttribute(
						bean, field.getName());
				if (writeMethodForAttribute.isPresent()) {
					writers.add(new AttributeWriterDelegate(
							writeMethodForAttribute.get(), annoValue(field
									.getAnnotation(annotationClass)), field));
				} else if (isPublic(field.getModifiers())) {
					writers.add(new FieldAccess(bean, annoValue(field
							.getAnnotation(annotationClass)), field));
				}

			}
		}
		return writers;
	}

	private Optional<AttributeReader> readMethodForAttribute(Object bean,
			final String name) throws Exception {
		for (AttributeReader reader : FindByIntrospection.beanAttributes()
				.listReaders(bean)) {
			if (reader.getName().equals(name)) {
				return Optional.of(reader);
			}
		}
		return Optional.absent();

	}

	private Optional<AttributeWriter> writeMethodForAttribute(Object bean,
			final String name) throws Exception {
		for (AttributeWriter writer : FindByIntrospection.beanAttributes()
				.listWriters(bean)) {
			if (writer.getName().equals(name)) {
				return Optional.of(writer);
			}
		}
		return Optional.absent();

	}

	private String annoValue(Annotation annotation)
			throws IllegalAccessException, InvocationTargetException {
		return (String) getAnnotationsAttributeReadMethod.invoke(annotation);
	}

}
