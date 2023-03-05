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
import static org.ardulink.core.beans.finder.impl.FindByIntrospection.beanAttributes;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.anno.LapsedWith.JDK8;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.ardulink.core.beans.Attribute.AttributeReader;
import org.ardulink.core.beans.Attribute.AttributeWriter;
import org.ardulink.core.beans.Attribute.TypedAttributeProvider;
import org.ardulink.core.beans.finder.api.AttributeFinder;
import org.ardulink.util.Throwables;
import org.ardulink.util.anno.LapsedWith;

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

		public DefaultAttribute(String name, Class<?> type, AttributeReader reader, AttributeWriter writer) {
			this.name = name;
			this.type = type;
			this.reader = reader;
			this.writer = writer;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Class<?> getType() {
			return this.type;
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

		@Override
		public Annotation[] getAnnotations() {
			Set<Annotation> annos = new LinkedHashSet<>();
			if (reader != null) {
				reader.addAnnotations(annos);
			}
			if (writer != null) {
				writer.addAnnotations(annos);
			}
			return annos.toArray(new Annotation[annos.size()]);
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return stream(getAnnotations()).filter(a -> a.annotationType().equals(annotationClass)).findFirst()
					.map(annotationClass::cast).orElse(null);
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

	public Attribute getAttribute(String name) {
		try {
			Optional<AttributeReader> reader = findReader(name);
			Optional<AttributeWriter> writer = findWriter(name);
			return determineType(reader, writer)
					.map(t -> new DefaultAttribute(name, t, reader.orElse(null), writer.orElse(null))).orElse(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Optional<Class<?>> determineType(Optional<AttributeReader> reader,
			Optional<AttributeWriter> writer) {
		Optional<Class<?>> readerType = reader.map(TypedAttributeProvider::getType);
		Optional<Class<?>> writerType = writer.map(TypedAttributeProvider::getType);
		if (!readerType.isPresent()) {
			return writerType;
		}
		if (!writerType.isPresent() || readerType.get().isAssignableFrom(writerType.get())) {
			return readerType;
		}
		if (writerType.get().isAssignableFrom(readerType.get())) {
			return writerType;
		}
		return Optional.empty();
	}

	@LapsedWith(module = JDK8, value = "Streams")
	private Optional<AttributeReader> findReader(String name) throws Exception {
		for (AttributeFinder finder : finders) {
			for (AttributeReader reader : finder.listReaders(bean)) {
				if (name.equals(reader.getName())) {
					return Optional.of(reader);
				}
			}
		}
		return Optional.empty();
	}

	@LapsedWith(module = JDK8, value = "Streams")
	private Optional<AttributeWriter> findWriter(String name) throws Exception {
		for (AttributeFinder finder : finders) {
			for (AttributeWriter writer : finder.listWriters(bean)) {
				if (name.equals(writer.getName())) {
					return Optional.of(writer);
				}
			}
		}
		return Optional.empty();
	}

	public Collection<String> attributeNames() {
		Set<String> attributeNames = new LinkedHashSet<>();
		try {
			for (AttributeFinder finder : finders) {
				attributeNames.addAll(namesOf(finder.listReaders(bean)));
				attributeNames.addAll(namesOf(finder.listWriters(bean)));
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		return new ArrayList<>(attributeNames);
	}

	@LapsedWith(module = JDK8, value = "Streams")
	private Collection<String> namesOf(Iterable<? extends TypedAttributeProvider> readers) {
		List<String> names = new ArrayList<>();
		for (TypedAttributeProvider reader : readers) {
			names.add(reader.getName());
		}
		return names;
	}

	@Override
	public String toString() {
		return "BeanProperties [bean=" + bean + "]";
	}

}
