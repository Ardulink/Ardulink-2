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

	private static final String SCHEMA = "ardulink";

	public interface AttributeSetter {
		void setValue(String value) throws Exception;
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
						public void setValue(String value)
								throws IllegalArgumentException,
								IllegalAccessException,
								InvocationTargetException {
							Object valueToSet = pd.getPropertyType()
									.isInstance(value) ? value : Primitive
									.parseAs(pd.getPropertyType(), value);
							pd.getWriteMethod().invoke(connectionConfig,
									valueToSet);
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
						public void setValue(String value)
								throws IllegalArgumentException,
								IllegalAccessException {
							Class<?> type = field.getType();
							Object valueToSet = type.isInstance(value) ? value
									: Primitive.parseAs(type, value);
							field.set(connectionConfig, valueToSet);
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
						public void setValue(String value)
								throws IllegalArgumentException,
								IllegalAccessException,
								InvocationTargetException {
							Class<?> type = method.getParameterTypes()[0];
							Object valueToSet = type.isInstance(value) ? value
									: Primitive.parseAs(type, value);
							method.invoke(connectionConfig, valueToSet);
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
						ConnectionConfig connectionConfig = connectionFactory
								.newConnectionConfig();

						for (String param : params) {

							String[] split = param.split("\\=");
							if (split.length == 2) {
								String key = split[0];
								String value = split[1];

								AttributeSetter attributeSetter = findAttributeSetter(
										connectionConfig, key, value);
								if (attributeSetter == null) {
									throw new IllegalArgumentException(
											"Illegal attribute " + key);
								}
								try {
									attributeSetter.setValue(value);
								} catch (Exception e) {
									throw new RuntimeException("Cannot set "
											+ key + " to " + value, e);
								}
							}
						}

						return connectionFactory
								.newConnection(connectionConfig);
					}
				}
				return null;
			}

		};
	}

	public abstract Connection getConnection(URI uri);

}
