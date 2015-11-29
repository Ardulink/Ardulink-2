package com.github.pfichtner.ardulink.core.connectionmanager;

import static com.github.pfichtner.beans.finder.impl.FindByAnnotation.propertyAnnotated;
import static org.zu.ardulink.util.Preconditions.checkArgument;
import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.zu.ardulink.util.Preconditions;
import org.zu.ardulink.util.Primitive;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionConfig.Named;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionConfig.PossibleValueFor;
import com.github.pfichtner.beans.Attribute;
import com.github.pfichtner.beans.BeanProperties;

public abstract class ConnectionManager {

	public interface ConfigAttribute {
		void setValue(Object value) throws Exception;

		boolean hasPossibleValues();

		Object[] getPossibleValues() throws Exception;

	}

	public static class ConfigAttributeAdapter<T extends ConnectionConfig>
			implements ConfigAttribute {

		private final T connectionConfig;
		private final BeanProperties beanProperties;
		private final String key;

		public ConfigAttributeAdapter(T connectionConfig,
				BeanProperties beanProperties, String key) {
			this.connectionConfig = connectionConfig;
			this.beanProperties = beanProperties;
			this.key = key;
		}

		@Override
		public void setValue(Object value) throws Exception {
			Attribute attribute = beanProperties.getAttribute(key);
			attribute.writeValue(value);
		}

		@Override
		public boolean hasPossibleValues() {
			return getPossibleValuesFor() != null;
		}

		@Override
		public Object[] getPossibleValues() throws Exception {
			Object value = checkNotNull(getPossibleValuesFor().readValue(),
					"returntype was null (should be an empty Object[] or empty Collection)");
			if (value instanceof Collection<?>) {
				value = ((Collection<?>) value).toArray(new Object[0]);
			}
			checkState(value instanceof Object[],
					"returntype is not an Object[] but %s", value.getClass());
			return (Object[]) value;
		}

		private Attribute getPossibleValuesFor() {
			return BeanProperties.builder(connectionConfig)
					.using(propertyAnnotated(PossibleValueFor.class)).build()
					.getAttribute(key);
		}

	}

	public interface Configurer {

		Configurer configure(String[] params);

		ConfigAttribute getAttribute(String key);

		Connection newConnection() throws Exception;

		Collection<String> getAttributes();

	}

	public static class DefaultConfigurer<T extends ConnectionConfig>
			implements Configurer {

		private final ConnectionFactory<T> connectionFactory;
		private final T connectionConfig;

		public DefaultConfigurer(ConnectionFactory<T> connectionFactory) {
			this.connectionFactory = connectionFactory;
			this.connectionConfig = connectionFactory.newConnectionConfig();
		}

		@Override
		public Collection<String> getAttributes() {
			try {
				return beanProperties().attributeNames();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public ConfigAttribute getAttribute(String key) {
			return new ConfigAttributeAdapter<T>(connectionConfig,
					beanProperties(), key);
		}

		private BeanProperties beanProperties() {
			return BeanProperties.builder(connectionConfig)
					.using(propertyAnnotated(Named.class)).build();
		}

		@Override
		public Configurer configure(String[] params) {
			for (String param : params) {
				String[] split = param.split("\\=");
				if (split.length == 2) {
					setValue(split[0], split[1]);
				}
			}
			return this;
		}

		private void setValue(String key, String value) {
			Attribute attribute = beanProperties().getAttribute(key);
			checkArgument(attribute != null, "Illegal attribute %s", key);
			try {
				attribute.writeValue(convert(value, attribute.getType()));
			} catch (Exception e) {
				throw new RuntimeException(
						"Cannot set " + key + " to " + value, e);
			}
		}

		@Override
		public Connection newConnection() throws Exception {
			return this.connectionFactory.newConnection(this.connectionConfig);
		}

		private Object convert(String value, Class<?> targetType) {
			return targetType.isInstance(value) ? value : Primitive.parseAs(
					targetType, value);
		}

	}

	private static final String SCHEMA = "ardulink";

	public static ConnectionManager getInstance() {
		return new ConnectionManager() {

			private URI checkSchema(URI uri) {
				checkArgument(SCHEMA.equalsIgnoreCase(uri.getScheme()),
						"schema not %s", SCHEMA);
				return uri;
			}

			private ConnectionFactory getConnectionFactory(URI uri) {
				ServiceLoader<ConnectionFactory> loader = ServiceLoader
						.load(ConnectionFactory.class);
				for (Iterator<ConnectionFactory> iterator = loader.iterator(); iterator
						.hasNext();) {
					ConnectionFactory connectionFactory = iterator.next();
					if (connectionFactory.getName().equals(uri.getHost())) {
						return connectionFactory;
					}
				}
				return null;
			}

			@Override
			public Configurer getConfigurer(URI uri) {
				ConnectionFactory connectionFactory = getConnectionFactory(checkSchema(uri));
				return connectionFactory == null ? null
						: new DefaultConfigurer(connectionFactory)
								.configure(uri.getQuery() == null ? new String[0]
										: uri.getQuery().split("\\&"));
			}

		};
	}

	public abstract Configurer getConfigurer(URI uri);

}