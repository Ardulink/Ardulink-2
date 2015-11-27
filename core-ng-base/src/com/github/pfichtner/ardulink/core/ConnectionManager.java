package com.github.pfichtner.ardulink.core;

import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.zu.ardulink.util.Primitive;

import com.github.pfichtner.Connection;
import com.github.pfichtner.ardulink.core.ConnectionConfig.Name;

public abstract class ConnectionManager {

	public static class DefaultConfigurer implements Configurer {

		private final ConnectionConfig connectionConfig;

		public DefaultConfigurer(ConnectionConfig connectionConfig) {
			this.connectionConfig = connectionConfig;
		}

		@Override
		public ConnectionConfig getConfig() {
			return this.connectionConfig;
		}

		@Override
		public void setValue(String key, String value) {
			AttributeSetter attributeSetter = findAttributeSetter(
					connectionConfig, key, value);
			if (attributeSetter == null) {
				throw new IllegalArgumentException("Illegal attribute " + key);
			}
			try {
				attributeSetter.setValue(convert(value,
						attributeSetter.getTargetType()));
			} catch (Exception e) {
				throw new RuntimeException(
						"Cannot set " + key + " to " + value, e);
			}
		}

		private AttributeSetter findAttributeSetter(
				ConnectionConfig connectionConfig, String key, String value) {

			for (AttributeSetterProvider asp : attributeSetterProviders) {
				try {
					AttributeSetter attributeSetter = asp.find(
							connectionConfig, key, value);
					if (attributeSetter != null) {
						return attributeSetter;
					}
				} catch (Exception e) {
					// ignore all
				}
			}
			return null;
		}

		private Object convert(String value, Class<?> targetType) {
			return targetType.isInstance(value) ? value : Primitive.parseAs(
					targetType, value);
		}

	}

	public interface Configurer {
		void setValue(String key, String value);

		ConnectionConfig getConfig();
	}

	private static final String SCHEMA = "ardulink";

	public interface AttributeSetter {
		Class<?> getTargetType();

		void setValue(Object value) throws Exception;
	}

	public interface AttributeSetterProvider {
		AttributeSetter find(ConnectionConfig connectionConfig, String key,
				String value) throws Exception;
	}

	public static class ConfigureViaBeanInfoAnnotation implements
			AttributeSetterProvider {

		@Override
		public AttributeSetter find(final ConnectionConfig connectionConfig,
				String key, String value) throws Exception {
			for (final PropertyDescriptor pd : Introspector.getBeanInfo(
					connectionConfig.getClass()).getPropertyDescriptors()) {
				if (pd.getName().equals(key) && pd.getWriteMethod() != null) {
					return new AttributeSetter() {
						@Override
						public void setValue(Object value)
								throws IllegalArgumentException,
								IllegalAccessException,
								InvocationTargetException {
							pd.getWriteMethod().invoke(connectionConfig, value);
						}

						@Override
						public Class<?> getTargetType() {
							return pd.getPropertyType();
						}
					};
				}
			}
			return null;
		}
	}

	public static class ConfigureViaFieldAnnotation implements
			AttributeSetterProvider {

		@Override
		public AttributeSetter find(final ConnectionConfig connectionConfig,
				String key, String value) throws Exception {
			for (final Field field : connectionConfig.getClass().getFields()) {
				if (field != null && field.isAnnotationPresent(Name.class)
						&& key.equals(field.getAnnotation(Name.class).value())) {
					return new AttributeSetter() {
						@Override
						public void setValue(Object value)
								throws IllegalArgumentException,
								IllegalAccessException {
							field.set(connectionConfig, value);
						}

						@Override
						public Class<?> getTargetType() {
							return field.getType();
						}
					};
				}
			}
			return null;
		}

	}

	public static class ConfigureViaMethodAnnotation implements
			AttributeSetterProvider {

		@Override
		public AttributeSetter find(final ConnectionConfig connectionConfig,
				String key, String value) throws Exception {
			for (final Method method : connectionConfig.getClass().getMethods()) {
				if (method != null && method.isAnnotationPresent(Name.class)
						&& method.getParameterTypes().length == 1
						&& key.equals(method.getAnnotation(Name.class).value())) {
					return new AttributeSetter() {
						@Override
						public void setValue(Object value)
								throws IllegalArgumentException,
								IllegalAccessException,
								InvocationTargetException {
							method.invoke(connectionConfig, value);
						}

						@Override
						public Class<?> getTargetType() {
							return method.getParameterTypes()[0];
						}
					};
				}
			}
			return null;
		}

	}

	private static final List<AttributeSetterProvider> attributeSetterProviders = Arrays
			.asList(new ConfigureViaMethodAnnotation(),
					new ConfigureViaFieldAnnotation(),
					new ConfigureViaBeanInfoAnnotation());

	public static ConnectionManager getInstance() {
		return new ConnectionManager() {

			@Override
			public Connection getConnection(URI uri) {
				checkSchema(uri);
				return getConnection(uri.getHost(),
						uri.getQuery() == null ? new String[0] : uri.getQuery()
								.split("\\&"));
			}

			private void checkSchema(URI uri) {
				checkArgument(SCHEMA.equalsIgnoreCase(uri.getScheme()),
						"schema not %s", SCHEMA);
			}

			private Connection getConnection(String name, String... params) {
				ServiceLoader<ConnectionFactory> loader = ServiceLoader
						.load(ConnectionFactory.class);
				for (Iterator<ConnectionFactory> iterator = loader.iterator(); iterator
						.hasNext();) {
					ConnectionFactory connectionFactory = iterator.next();
					if (connectionFactory.getName().equals(name)) {

						Configurer configurer = getConfigurer(connectionFactory);
						for (String param : params) {
							String[] split = param.split("\\=");
							if (split.length == 2) {
								configurer.setValue(split[0], split[1]);
							}
						}

						return connectionFactory.newConnection(configurer.getConfig());
					}
				}
				return null;
			}

			@Override
			public Configurer getConfigurer(
					ConnectionFactory<?> connectionFactory) {
				return new DefaultConfigurer(
						connectionFactory.newConnectionConfig());
			}

		};
	}

	public abstract Connection getConnection(URI uri);

	public abstract Configurer getConfigurer(
			ConnectionFactory<?> connectionConfig);

}