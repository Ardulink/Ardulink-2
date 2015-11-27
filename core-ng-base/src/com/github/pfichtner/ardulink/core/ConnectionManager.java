package com.github.pfichtner.ardulink.core;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.zu.ardulink.util.Primitive;

import com.github.pfichtner.Connection;
import com.github.pfichtner.ardulink.core.ConnectionConfig.Name;

public abstract class ConnectionManager {

	public interface AttributeSetter {
		void setValue(String value) throws Exception;
	}

	public static ConnectionManager getInstance() {
		return new ConnectionManager() {
			@Override
			public Connection getConnection(String name, String... params) {
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

								AttributeSetter as = findAttributeSetter();

								AttributeSetter configured = configureViaMethodAnnotation(
										connectionConfig, key, value);
								if (configured == null) {
									configured = configureViaFieldAnnotation(
											connectionConfig, key, value);
									if (configured == null) {
										configured = configureViaBeanInfoAnnotation(
												connectionConfig, key, value);
										if (configured == null) {
											throw new IllegalArgumentException(
													"Illegal attribute " + key);
										}

									}
								}
								try {
									configured.setValue(value);
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

			private AttributeSetter findAttributeSetter() {
				// TODO Auto-generated method stub
				return null;
			}

			private AttributeSetter configureViaMethodAnnotation(
					final ConnectionConfig connectionConfig, final String key,
					String value) {
				try {

					for (final Method method : connectionConfig.getClass()
							.getMethods()) {
						if (method != null
								&& method.isAnnotationPresent(Name.class)
								&& method.getParameterTypes().length == 1
								&& key.equals(method.getAnnotation(Name.class)
										.value())) {
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
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
				return null;
			}

			private AttributeSetter configureViaFieldAnnotation(
					final ConnectionConfig connectionConfig, final String key,
					String value) {
				try {

					for (final Field field : connectionConfig.getClass()
							.getFields()) {
						if (field != null
								&& field.isAnnotationPresent(Name.class)
								&& key.equals(field.getAnnotation(Name.class)
										.value())) {
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
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
				return null;
			}

			private AttributeSetter configureViaBeanInfoAnnotation(
					final ConnectionConfig connectionConfig, final String key,
					String value) {
				try {
					for (final PropertyDescriptor pd : Introspector
							.getBeanInfo(connectionConfig.getClass())
							.getPropertyDescriptors()) {
						if (pd.getName().equals(key)
								&& pd.getWriteMethod() != null) {
							return new AttributeSetter() {
								@Override
								public void setValue(String value)
										throws IllegalArgumentException,
										IllegalAccessException,
										InvocationTargetException {
									Object valueToSet = pd.getPropertyType()
											.isInstance(value) ? value
											: Primitive
													.parseAs(pd
															.getPropertyType(),
															value);
									pd.getWriteMethod().invoke(
											connectionConfig, valueToSet);
								}
							};
						}
					}
				} catch (IntrospectionException e) {
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
				return null;
			}

		};
	}

	public abstract Connection getConnection(String name, String... params);

}
