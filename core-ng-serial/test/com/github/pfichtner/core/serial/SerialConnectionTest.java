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
		String aValue = "aValue";
		int bValue = 1;
		DummyConnection connection = (DummyConnection) connectionManager
				.getConnection("dummy", "a=" + aValue, "b=" + bValue);
		DummyConnectionConfig config = connection.getConfig();
		assertThat(config.a, is(aValue));
		assertThat(config.b, is(bValue));
	}

	@Test
	@Ignore
	public void canLoadClassViaServiceLoader() {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager.getConnection("serial");
		assertThat(connection.getClass().getName(),
				is(StreamConnection.class.getName()));
	}

}
