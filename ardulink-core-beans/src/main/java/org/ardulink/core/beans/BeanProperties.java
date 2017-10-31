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

import static org.ardulink.core.beans.finder.impl.FindByIntrospection.beanAttributes;
import static org.ardulink.util.Preconditions.checkState;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.ardulink.util.Optional;
import org.ardulink.util.Throwables;
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

		public DefaultAttribute(String name, Class<?> type,
				AttributeReader reader, AttributeWriter writer) {
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
			Set<Annotation> annos = new LinkedHashSet<Annotation>();
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
			for (Annotation annotation : getAnnotations()) {
				if (annotation.annotationType().equals(annotationClass)) {
					return annotationClass.cast(annotation);
				}
			}
			return null;
		}

	}

	private final Object bean;
	private final AttributeFinder[] finders;

	private BeanProperties(final Builder builder) {
		this.bean = builder.bean;
		this.finders = builder.finders.clone();
	}

	public static BeanProperties forBean(final Object bean) {
		return builder(bean).using(beanAttributes()).build();
	}

	public static BeanProperties.Builder builder(final Object bean) {
		return new BeanProperties.Builder(bean);
	}

	public Attribute getAttribute(final String name) {
		try {
			Optional<AttributeReader> reader = findReader(name);
			Optional<AttributeWriter> writer = findWriter(name);
			Optional<Class<?>> type = determineType(reader, writer);
			return (reader == null && writer == null) || !type.isPresent() ? null
					: new DefaultAttribute(name, type.orNull(),
							reader.orNull(), writer.orNull());
		} catch (Exception e) {
			return null;
		}
	}

	private static Optional<Class<?>> determineType(
			Optional<AttributeReader> reader, Optional<AttributeWriter> writer) {
		Optional<Class<?>> readerType = reader.isPresent() ? Optional
				.<Class<?>> of(reader.get().getType()) : Optional
				.<Class<?>> absent();
		Optional<Class<?>> writerType = writer.isPresent() ? Optional
				.<Class<?>> of(writer.get().getType()) : Optional
				.<Class<?>> absent();
		if (!readerType.isPresent()) {
			return writerType;
		}
		if (!writerType.isPresent()) {
			return readerType;
		}
		if (readerType.get().isAssignableFrom(writerType.get())) {
			return readerType;
		}
		if (writerType.get().isAssignableFrom(readerType.get())) {
			return writerType;
		}
		return Optional.absent();
	}

	private Optional<AttributeReader> findReader(final String name)
			throws Exception {
		for (AttributeFinder finder : finders) {
			for (AttributeReader reader : finder.listReaders(bean)) {
				if (name.equals(reader.getName())) {
					return Optional.of(reader);
				}
			}
		}
		return Optional.absent();
	}

	private Optional<AttributeWriter> findWriter(final String name)
			throws Exception {
		for (AttributeFinder finder : finders) {
			for (AttributeWriter writer : finder.listWriters(bean)) {
				if (name.equals(writer.getName())) {
					return Optional.of(writer);
				}
			}
		}
		return Optional.absent();
	}

	public Collection<String> attributeNames() {
		Set<String> attributeNames = new LinkedHashSet<String>();
		try {
			for (AttributeFinder finder : finders) {
				attributeNames.addAll(namesOf(finder.listReaders(bean)));
				attributeNames.addAll(namesOf(finder.listWriters(bean)));
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		return new ArrayList<String>(attributeNames);
	}

	private Collection<? extends String> namesOf(
			Iterable<? extends TypedAttributeProvider> readers) {
		List<String> names = new ArrayList<String>();
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
