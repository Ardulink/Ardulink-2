package com.github.pfichtner.ardulink.core;

import static org.zu.ardulink.util.Preconditions.checkArgument;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
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
import com.github.pfichtner.ardulink.core.ConnectionConfig.Named;
import com.github.pfichtner.ardulink.core.ConnectionConfig.PossibleValueFor;

public abstract class ConnectionManager {

	public interface Configurer {

		Configurer configure(String[] params);

		AttributeSetter getAttributeSetter(String key);

		Connection newConnection();

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
			AttributeSetter attributeSetter = getAttributeSetter(key);
			checkArgument(attributeSetter != null, "Illegal attribute %s", key);
			try {
				attributeSetter.setValue(convert(value,
						attributeSetter.getTargetType()));
			} catch (Exception e) {
				throw new RuntimeException(
						"Cannot set " + key + " to " + value, e);
			}
		}

		@Override
		public Connection newConnection() {
			return this.connectionFactory.newConnection(this.connectionConfig);
		}

		public AttributeSetter getAttributeSetter(String key) {

			for (AttributeSetterProvider asp : attributeSetterProviders) {
				try {
					AttributeSetter attributeSetter = asp.find(
							connectionConfig, key);
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

	private static final String SCHEMA = "ardulink";

	public interface AttributeSetter {
		Class<?> getTargetType();

		void setValue(Object value) throws Exception;

		Object[] getPossibleValues() throws Exception;
	}

	public interface AttributeGetter {
		Class<?> getTargetType();

		Object getValue() throws Exception;
	}

	public interface AttributeSetterProvider {
		AttributeSetter find(ConnectionConfig connectionConfig, String key)
				throws Exception;
	}

	public interface AttributeGetterProvider {
		AttributeGetter find(ConnectionConfig connectionConfig, String key)
				throws Exception;
	}

	public static class ConfigureViaBeanInfoAnnotation implements
			AttributeSetterProvider {

		@Override
		public AttributeSetter find(final ConnectionConfig connectionConfig,
				String key) throws Exception {
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

						@Override
						public Object[] getPossibleValues() {
							return null;
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
				String key) throws Exception {
			for (final Field field : connectionConfig.getClass().getFields()) {
				if (field != null && field.isAnnotationPresent(Named.class)
						&& key.equals(field.getAnnotation(Named.class).value())) {
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

						@Override
						public Object[] getPossibleValues() {
							return null;
						}

					};
				}
			}
			return null;
		}

	}

	public static class ConfigureViaMethodAnnotation implements
			AttributeSetterProvider {

		private final Class<? extends Annotation> annotationClass;
		private final String attribute;

		public ConfigureViaMethodAnnotation(
				Class<? extends Annotation> annotationClass, String attribute) {
			this.annotationClass = annotationClass;
			this.attribute = attribute;
		}

		@Override
		public AttributeSetter find(final ConnectionConfig connectionConfig,
				final String key) throws Exception {
			for (final Method method : connectionConfig.getClass().getMethods()) {
				if (method != null
						&& method.isAnnotationPresent(annotationClass)
						&& method.getParameterTypes().length == 1
						&& key.equals(getAnnoAttribute(
								method.getAnnotation(annotationClass),
								attribute))) {
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

						@Override
						public Object[] getPossibleValues() throws Exception {
							ReadViaMethodAnnotation readViaMethodAnnotation = new ReadViaMethodAnnotation(
									PossibleValueFor.class, "value");
							AttributeGetter modifier = readViaMethodAnnotation
									.find(connectionConfig, key);
							Object value = modifier.getValue();
							return modifier == null
									|| !(value instanceof Object[]) ? null
									: (Object[]) value;
						}

					};
				}
			}
			return null;
		}

		private static Object getAnnoAttribute(Annotation annotation,
				String attribName) throws IllegalAccessException,
				InvocationTargetException, NoSuchMethodException {
			return annotation.getClass().getMethod(attribName)
					.invoke(annotation);
		}

	}

	public static class ReadViaMethodAnnotation implements
			AttributeGetterProvider {

		private final Class<? extends Annotation> annotationClass;
		private final String attribute;

		public ReadViaMethodAnnotation(
				Class<? extends Annotation> annotationClass, String attribute) {
			this.annotationClass = annotationClass;
			this.attribute = attribute;
		}

		@Override
		public AttributeGetter find(final ConnectionConfig connectionConfig,
				String key) throws Exception {
			for (final Method method : connectionConfig.getClass().getMethods()) {
				if (method != null
						&& method.isAnnotationPresent(annotationClass)
						&& method.getParameterTypes().length == 0
						&& key.equals(getAnnoAttribute(
								method.getAnnotation(annotationClass),
								attribute))) {
					return new AttributeGetter() {
						@Override
						public Object getValue() throws Exception {
							return method.invoke(connectionConfig);
						}

						@Override
						public Class<?> getTargetType() {
							return method.getReturnType();
						}

					};
				}
			}
			return null;
		}

		private static Object getAnnoAttribute(Annotation annotation,
				String attribName) throws IllegalAccessException,
				InvocationTargetException, NoSuchMethodException {
			return annotation.getClass().getMethod(attribName)
					.invoke(annotation);
		}

	}

	private static final List<AttributeSetterProvider> attributeSetterProviders = Arrays
			.asList(new ConfigureViaMethodAnnotation(Named.class, "value"),
					new ConfigureViaFieldAnnotation(),
					new ConfigureViaBeanInfoAnnotation());

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