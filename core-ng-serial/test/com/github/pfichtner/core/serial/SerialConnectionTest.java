package com.github.pfichtner.core.serial;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.github.pfichtner.Connection;
import com.github.pfichtner.StreamConnection;
import com.github.pfichtner.ardulink.core.ConnectionManager;
import com.github.pfichtner.core.serial.DummyConnectionFactory.DummyConnection;
import com.github.pfichtner.core.serial.DummyConnectionFactory.DummyConnectionConfig;

public class SerialConnectionTest {

	@Test
	public void returnsNullOnInvalidNames() {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager
				.getConnection("--- non registered (and not existing) name---");
		assertThat(connection, nullValue());
	}

	@Test
	public void canFindDummyConnection() {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		DummyConnection connection = (DummyConnection) connectionManager
				.getConnection("dummy", "port=/dev/ttyUSB", "speed=115200");
		DummyConnectionConfig config = connection.getConfig();
		assertThat(config.getPort(), is("/dev/ttyUSB"));
		assertThat(config.getSpeed(), is(115200));
	}

	@Test
	@Ignore
	public void canLoadClassViaServiceLoader() {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager.getConnection("serial",
				"port=/dev/ttyUSB", "speed=115200");
		assertThat(connection.getClass().getName(),
				is(StreamConnection.class.getName()));
	}

}
