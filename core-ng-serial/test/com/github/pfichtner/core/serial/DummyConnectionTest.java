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

public class DummyConnectionTest {

	@Test
	public void returnsNullOnInvalidNames() {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager
				.getConnection("--- non registered (and not existing) name---");
		assertThat(connection, nullValue());
	}

	@Test
	public void canConfigureDummyDonnection() {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		String aValue = "aValue";
		int bValue = 1;
		String cValue = "cValue";
		int dValue = 42;
		DummyConnection connection = (DummyConnection) connectionManager
				.getConnection("dummy", "a=" + aValue, "b=" + bValue, "c="
						+ cValue, "d=" + dValue);
		DummyConnectionConfig config = connection.getConfig();
		assertThat(config.a, is(aValue));
		assertThat(config.b, is(bValue));
		assertThat(config.c, is(cValue));
		assertThat(config.d, is(dValue));
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnInvalidKey() {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		connectionManager.getConnection("dummy", "nonExistingKey=someValue");
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
