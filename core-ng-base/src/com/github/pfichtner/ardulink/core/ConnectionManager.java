package com.github.pfichtner.ardulink.core;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.zu.ardulink.util.Primitive;

import com.github.pfichtner.Connection;

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
						BeanInfo beanInfo = Introspector
								.getBeanInfo(connectionConfig.getClass());
						PropertyDescriptor[] propertyDescriptors = beanInfo
								.getPropertyDescriptors();
						for (PropertyDescriptor pd : propertyDescriptors) {
							if (pd.getName().equals(key)
									&& pd.getWriteMethod() != null) {
								setValue(connectionConfig, pd, key, value);
							}
						}
					} catch (IntrospectionException e) {
						throw new RuntimeException(e);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					}
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

		};
	}

	public abstract Connection getConnection(String name, String... params);

}
