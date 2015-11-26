package com.github.pfichtner.ardulink.core;

import com.github.pfichtner.Connection;

public interface ConnectionFactory<T extends ConnectionConfig> {

	String getName();

	Connection newConnection(T config);

	T newConnectionConfig();

}
