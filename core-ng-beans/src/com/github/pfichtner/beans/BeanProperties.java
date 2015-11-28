package com.github.pfichtner.beans;

import static com.github.pfichtner.beans.finder.impl.FindByIntrospection.beanAttributes;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;
import com.github.pfichtner.beans.Attribute.TypedAttributeProvider;
import com.github.pfichtner.beans.finder.api.AttributeFinder;

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
			checkState(canRead(), "cannot read");
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
			AttributeReader reader = findReader(name);
			AttributeWriter writer = findWriter(name);
			Class<?> type = determineType(reader, writer);
			return (reader == null && writer == null) || type == null ? null
					: new DefaultAttribute(name, type, reader, writer);
		} catch (final Exception e) {
			return null;
		}
	}

	private static Class<?> determineType(AttributeReader reader,
			AttributeWriter writer) {
		Class<?> readerType = reader == null ? null : reader.getType();
		Class<?> writerType = writer == null ? null : writer.getType();
		if (readerType == null) {
			return writerType;
		}
		if (writerType == null) {
			return readerType;
		}
		if (readerType.isAssignableFrom(writerType)) {
			return readerType;
		}
		if (writerType.isAssignableFrom(readerType)) {
			return writerType;
		}
		return null;
	}

	private AttributeReader findReader(final String name) throws Exception {
		for (AttributeFinder finder : finders) {
			for (AttributeReader reader : finder.listReaders(bean)) {
				if (name.equals(reader.getName())) {
					return reader;
				}
			}
		}
		return null;
	}

	private AttributeWriter findWriter(final String name) throws Exception {
		for (AttributeFinder finder : finders) {
			for (AttributeWriter writer : finder.listWriters(bean)) {
				if (name.equals(writer.getName())) {
					return writer;
				}
			}
		}
		return null;
	}

	public Collection<String> attributeNames() throws Exception {
		Set<String> attributeNames = new LinkedHashSet<String>();
		for (AttributeFinder finder : finders) {
			attributeNames.addAll(namesOf(finder.listReaders(bean)));
			attributeNames.addAll(namesOf(finder.listWriters(bean)));
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

}
