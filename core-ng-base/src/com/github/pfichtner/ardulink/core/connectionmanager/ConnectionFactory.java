package com.github.pfichtner.ardulink.core.connectionmanager;

import com.github.pfichtner.ardulink.core.Connection;

public interface ConnectionFactory<T extends ConnectionConfig> {

	String getName();

	Connection newConnection(T config) throws Exception;

	T newConnectionConfig();

}
