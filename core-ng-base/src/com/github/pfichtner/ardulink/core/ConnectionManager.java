package com.github.pfichtner.ardulink.core;

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
							setValue(connectionConfig, param);

						}

						return connectionFactory
								.newConnection(connectionConfig);
					}
				}
				return null;
			}

			private void setValue(ConnectionConfig connectionConfig,
					String param) {
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
								setValue(connectionConfig, method, key, value);
							}
						}
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					}
				}
			}

			private void setValue(ConnectionConfig config, Method method,
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

			private RuntimeException err(String key, String value, Exception e) {
				return new RuntimeException("Cannot set " + key + " to "
						+ value, e);
			}

		};
	}

	public abstract Connection getConnection(String name, String... params);

}
