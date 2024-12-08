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

package org.ardulink.core.beans.finder.impl;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.stream;
import static java.util.Collections.addAll;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Stream.concat;
import static org.ardulink.core.beans.finder.api.AttributeFinders.beanAttributes;
import static org.ardulink.util.Iterables.stream;
import static org.ardulink.util.Predicates.attribute;
import static org.ardulink.util.Streams.iterable;
import static org.ardulink.util.Throwables.propagate;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.ardulink.core.beans.Attribute.AttributeReader;
import org.ardulink.core.beans.Attribute.AttributeWriter;
import org.ardulink.core.beans.Attribute.TypedAttributeProvider;
import org.ardulink.core.beans.finder.api.AttributeFinder;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class FindByAnnotation implements AttributeFinder {

	private static class AttributeReaderDelegate implements AttributeReader {

		private final AttributeReader delegate;
		private final String name;
		private final Field annoFoundOn;

		public static AttributeReader attributeReaderDelegate(AttributeReader delegate, String name,
				Field annoFoundOn) {
			return new AttributeReaderDelegate(delegate, name, annoFoundOn);
		}

		private AttributeReaderDelegate(AttributeReader delegate, String name, Field annoFoundOn) {
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
			addAll(annotations, this.annoFoundOn.getAnnotations());
		}

	}

	private static class AttributeWriterDelegate implements AttributeWriter {

		private final AttributeWriter delegate;
		private final String name;
		private final Field annoFoundOn;

		public static AttributeWriter attributeWriterDelegate(AttributeWriter delegate, String name,
				Field annoFoundOn) {
			return new AttributeWriterDelegate(delegate, name, annoFoundOn);
		}

		private AttributeWriterDelegate(AttributeWriter delegate, String name, Field annoFoundOn) {
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
			addAll(annotations, this.annoFoundOn.getAnnotations());
		}

	}

	private final Class<? extends Annotation> annotationClass;
	private final Method getAnnotationsAttributeReadMethod;

	public <T extends Annotation> FindByAnnotation(Class<T> annotationClass, Method getAnnotationsAttributeReadMethod) {
		this.annotationClass = annotationClass;
		this.getAnnotationsAttributeReadMethod = getAnnotationsAttributeReadMethod;
	}

	public static Method toMethod(Class<? extends Annotation> annotationClass, String annotationAttribute) {
		return stream(annotationClass.getMethods()) //
				.filter(attribute(Method::getName, isEqual(annotationAttribute))) //
				.findFirst() //
				.orElseThrow(() -> new IllegalArgumentException(
						format("%s has no attribute named %s", annotationClass.getName(), annotationAttribute)));
	}

	@Override
	public Iterable<AttributeReader> listReaders(Object bean) {
		try {
			Stream<ReadMethod> methods = annotatedMethods(bean) //
					.filter(ReadMethod::isReadMethod) //
					.map(m -> readMethod(bean, m));
			Stream<AttributeReader> fields = stream(bean.getClass().getDeclaredFields()) //
					.filter(f -> f.isAnnotationPresent(annotationClass)) //
					.map(f -> readMethodForAttribute(bean, f.getName()) //
							.map(r -> AttributeReaderDelegate.attributeReaderDelegate(r, annoValue(f), f))
							.orElseGet(() -> fieldAccess(bean, f)));
			return iterable(concat(methods, fields));
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	@Override
	public Iterable<AttributeWriter> listWriters(Object bean) {
		try {
			Stream<WriteMethod> methods = annotatedMethods(bean) //
					.filter(WriteMethod::isWriteMethod) //
					.map(m -> writeMethod(bean, m));
			Stream<AttributeWriter> fields = stream(bean.getClass().getDeclaredFields())
					.filter(f -> f.isAnnotationPresent(annotationClass)) //
					.map(f -> writeMethodForAttribute(bean, f.getName()) //
							.map(w -> AttributeWriterDelegate.attributeWriterDelegate(w, annoValue(f), f))
							.orElseGet(() -> fieldAccess(bean, f)));
			return iterable(concat(methods, fields));
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private Stream<Method> annotatedMethods(Object bean) {
		return Stream.of(bean.getClass().getDeclaredMethods()).filter(m -> m.isAnnotationPresent(annotationClass));
	}

	private FieldAccess fieldAccess(Object bean, Field field) {
		return isPublic(field.getModifiers()) ? new FieldAccess(bean, annoValue(field), field) : null;
	}

	private ReadMethod readMethod(Object bean, Method method) {
		return new ReadMethod(bean, annoValue(method), method);
	}

	private WriteMethod writeMethod(Object bean, Method method) {
		return new WriteMethod(bean, annoValue(method), method);
	}

	private String annoValue(AnnotatedElement annotatedElement) {
		return annoValue(annotatedElement.getAnnotation(annotationClass));
	}

	private String annoValue(Annotation annotation) {
		try {
			Object result = getAnnotationsAttributeReadMethod.invoke(annotation);
			return result == null ? null : String.valueOf(result);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw propagate(e);
		}
	}

	private Optional<AttributeReader> readMethodForAttribute(Object bean, String name) {
		return findWithName(name, stream(beanAttributes().listReaders(bean)));
	}

	private Optional<AttributeWriter> writeMethodForAttribute(Object bean, String name) {
		return findWithName(name, stream(beanAttributes().listWriters(bean)));
	}

	private <T extends TypedAttributeProvider> Optional<T> findWithName(String name, Stream<T> stream) {
		return stream.filter(hasName(name)).findFirst();
	}

	private Predicate<TypedAttributeProvider> hasName(String name) {
		return p -> p.getName().equals(name);
	}

}
