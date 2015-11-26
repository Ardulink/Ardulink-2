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
							boolean configured = configureViaMethodAnnotation(
									connectionConfig, param);
							if (!configured) {
								configured = configureViaFieldAnnotation(
										connectionConfig, param);
								if (!configured) {
									configured = configureViaBeanInfoAnnotation(
											connectionConfig, param);
									if (!configured) {
										// TODO this is key AND value!
										throw new IllegalArgumentException(
												"Illegal attribute " + param);
									}

								}
							}
						}

						return connectionFactory
								.newConnection(connectionConfig);
					}
				}
				return null;
			}

			private boolean configureViaMethodAnnotation(
					ConnectionConfig connectionConfig, String param) {
				String[] split = param.split("\\=");
				if (split.length == 2) {
					String key = split[0];
					String value = split[1];
					try {

						for (Method method : connectionConfig.getClass()
								.getMethods()) {
							if (method != null
									&& method.isAnnotationPresent(Name.class)
									&& method.getParameterTypes().length == 1
									&& key.equals(method.getAnnotation(
											Name.class).value())) {
								setViaMethod(connectionConfig, method, key,
										value);
								return true;
							}
						}
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					}
				}
				return false;
			}

			private boolean configureViaFieldAnnotation(
					ConnectionConfig connectionConfig, String param) {
				String[] split = param.split("\\=");
				if (split.length == 2) {
					String key = split[0];
					String value = split[1];
					try {

						for (Field field : connectionConfig.getClass()
								.getFields()) {
							if (field != null
									&& field.isAnnotationPresent(Name.class)
									&& key.equals(field.getAnnotation(
											Name.class).value())) {
								setViaField(connectionConfig, field, key, value);
								return true;
							}
						}
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					}
				}
				return false;
			}

			private boolean configureViaBeanInfoAnnotation(
					ConnectionConfig connectionConfig, String param) {
				String[] split = param.split("\\=");
				if (split.length == 2) {
					String key = split[0];
					String value = split[1];
					try {
						BeanInfo beanInfo = Introspector
								.getBeanInfo(connectionConfig.getClass());
						PropertyDescriptor[] propertyDescriptors = beanInfo
								.getPropertyDescriptors();
						for (PropertyDescriptor pd : propertyDescriptors) {
							if (pd.getName().equals(key)
									&& pd.getWriteMethod() != null) {
								setValue(connectionConfig, pd, key, value);
								return true;
							}
						}
					} catch (IntrospectionException e) {
						throw new RuntimeException(e);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					}
				}
				return false;
			}

			private void setViaMethod(ConnectionConfig config, Method method,
					String key, String value) {
				try {
					Class<?> type = method.getParameterTypes()[0];
					Object valueToSet = type.isInstance(value) ? value
							: Primitive.parseAs(type, value);
					method.invoke(config, valueToSet);
				} catch (IllegalArgumentException e) {
					throw err(key, value, e);
				} catch (IllegalAccessException e) {
					throw err(key, value, e);
				} catch (InvocationTargetException e) {
					throw err(key, value, e);
				}
			}

			private void setViaField(ConnectionConfig config, Field field,
					String key, String value) {
				try {
					Class<?> type = field.getType();
					Object valueToSet = type.isInstance(value) ? value
							: Primitive.parseAs(type, value);
					field.set(config, valueToSet);
				} catch (IllegalArgumentException e) {
					throw err(key, value, e);
				} catch (IllegalAccessException e) {
					throw err(key, value, e);
				}
			}

			private void setValue(ConnectionConfig config,
					PropertyDescriptor pd, String key, String value) {
				try {
					Object valueToSet = pd.getPropertyType().isInstance(value) ? value
							: Primitive.parseAs(pd.getPropertyType(), value);
					pd.getWriteMethod().invoke(config, valueToSet);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("Cannot set " + key + " to "
							+ value);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Cannot set " + key + " to "
							+ value);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("Cannot set " + key + " to "
							+ value);
				}
			}

			private RuntimeException err(String key, String value, Exception e) {
				return new RuntimeException("Cannot set " + key + " to "
						+ value, e);
			}

		};
	}

	public abstract Connection getConnection(String name, String... params);

}
