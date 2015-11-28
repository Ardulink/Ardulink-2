package com.github.pfichtner.ardulink.core.connectionmanager;

import static com.github.pfichtner.beans.finder.impl.FindByAnnotation.propertyAnnotated;
import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.zu.ardulink.util.Primitive;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionConfig.Named;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionConfig.PossibleValueFor;
import com.github.pfichtner.beans.Attribute;
import com.github.pfichtner.beans.BeanProperties;

public abstract class ConnectionManager {

	public interface Configurer {

		Configurer configure(String[] params);

		Attribute getAttribute(String key);

		Object[] possibleValues(String key) throws Exception;

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

		@Override
		public Object[] possibleValues(String key) throws Exception {
			return (Object[]) BeanProperties.builder(connectionConfig)
					.using(propertyAnnotated(PossibleValueFor.class)).build()
					.getAttribute(key).readValue();
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

		public Attribute getAttribute(String key) {
			return beanProperties().getAttribute(key);
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