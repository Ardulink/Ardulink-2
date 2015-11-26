package com.github.pfichtner.core.serial;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.pfichtner.Connection;
import com.github.pfichtner.StreamConnection;
import com.github.pfichtner.ardulink.core.ConnectionFactory;

public class SerialConnectionFactory implements
		ConnectionFactory<SerialConnectionConfig> {

	@Override
	public String getName() {
		return "serial";
	}

	@Override
	public Connection newConnection(SerialConnectionConfig config) {
		InputStream is = null;
		OutputStream os = null;
		return new StreamConnection(is, os);
	}

	@Override
	public SerialConnectionConfig newConnectionConfig() {
		return new SerialConnectionConfig();
	}

}
