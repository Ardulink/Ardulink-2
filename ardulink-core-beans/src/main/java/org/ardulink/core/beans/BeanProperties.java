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

package org.ardulink.core.beans;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Stream.concat;
import static org.ardulink.core.beans.finder.api.AttributeFinder.beanAttributes;
import static org.ardulink.util.Iterables.stream;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Suppliers.memoize;
import static org.ardulink.util.Throwables.propagate;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
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
public class BeanProperties {

	public static class Builder {

		private final Object bean;
		private AttributeFinder[] finders;

		public Builder(Object bean) {
			this.bean = bean;
		}

		public BeanProperties build() {
			return new BeanProperties(this);
		}

		public Builder using(AttributeFinder... finders) {
			this.finders = finders.clone();
			return this;
		}

	}

	public static class DefaultAttribute implements Attribute {

		private final String name;
		private final Class<?> type;
		private final AttributeReader reader;
		private final AttributeWriter writer;
		private final Supplier<Annotation[]> annotations;

		public DefaultAttribute(String name, Class<?> type, AttributeReader reader, AttributeWriter writer) {
			this.name = name;
			this.type = type;
			this.reader = reader;
			this.writer = writer;
			this.annotations = memoize(this::computeAnnotations);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class<?> getType() {
			return type;
		}

		@Override
		public boolean canRead() {
			return reader != null;
		}

		@Override
		public Object readValue() throws Exception {
			checkState(canRead(), "cannot read %s", name);
			return reader.getValue();
		}

		@Override
		public boolean canWrite() {
			return writer != null;
		}

		@Override
		public void writeValue(Object value) throws Exception {
			checkState(canWrite(), "cannot write");
			writer.setValue(value);
		}

		private Annotation[] computeAnnotations() {
			Set<Annotation> annos = new LinkedHashSet<>();
			Stream.of(reader, writer).filter(Objects::nonNull).forEach(p -> p.addAnnotations(annos));
			return annos.toArray(new Annotation[annos.size()]);
		}

		@Override
		public Annotation[] getAnnotations() {
			return annotations.get();
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return stream(getAnnotations()) //
					.filter(a -> a.annotationType().equals(annotationClass)) //
					.findFirst() //
					.map(annotationClass::cast) //
					.orElse(null);
		}

	}

	private final Object bean;
	private final AttributeFinder[] finders;

	private BeanProperties(Builder builder) {
		this.bean = builder.bean;
		this.finders = builder.finders.clone();
	}

	public static BeanProperties forBean(Object bean) {
		return builder(bean).using(beanAttributes()).build();
	}

	public static BeanProperties.Builder builder(Object bean) {
		return new BeanProperties.Builder(bean);
	}

	public Attribute getAttribute(Object name) {
		return getAttribute(name == null ? null : String.valueOf(name));
	}

	public Attribute getAttribute(String name) {
		try {
			Optional<AttributeReader> reader = findReader(name);
			Optional<AttributeWriter> writer = findWriter(name);
			return determineType(reader, writer)
					.map(t -> new DefaultAttribute(name, t, reader.orElse(null), writer.orElse(null))).orElse(null);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private static Optional<Class<?>> determineType(Optional<AttributeReader> reader,
			Optional<AttributeWriter> writer) {
		Optional<Class<?>> readerType = reader.map(TypedAttributeProvider::getType);
		Optional<Class<?>> writerType = writer.map(TypedAttributeProvider::getType);
		if (!readerType.isPresent()) {
			return writerType;
		}
		if (!writerType.isPresent() || readerType.orElseThrow().isAssignableFrom(writerType.orElseThrow())) {
			return readerType;
		}
		if (writerType.orElseThrow().isAssignableFrom(readerType.orElseThrow())) {
			return writerType;
		}
		return Optional.empty();
	}

	private Optional<AttributeReader> findReader(String name) throws Exception {
		return firstWithName(name, stream(finders).map(a -> stream(a.listReaders(bean))).flatMap(identity()));
	}

	private Optional<AttributeWriter> findWriter(String name) throws Exception {
		return firstWithName(name, stream(finders).map(a -> stream(a.listWriters(bean))).flatMap(identity()));
	}

	private <T extends TypedAttributeProvider> Optional<T> firstWithName(String name, Stream<T> writers) {
		return writers.filter(r -> name.equals(r.getName())).findFirst();
	}

	public Collection<String> attributeNames() {
		return stream(finders).map(f -> concat(namesOf(f.listReaders(bean)), namesOf(f.listWriters(bean))))
				.flatMap(identity()).collect(toCollection(LinkedHashSet::new));
	}

	private Stream<String> namesOf(Iterable<? extends TypedAttributeProvider> readers) {
		return stream(readers).map(TypedAttributeProvider::getName);
	}

	@Override
	public String toString() {
		return "BeanProperties [bean=" + bean + "]";
	}

}
