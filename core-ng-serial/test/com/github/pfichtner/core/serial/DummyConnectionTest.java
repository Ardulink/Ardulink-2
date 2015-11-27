package com.github.pfichtner.core.serial;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Ignore;
import org.junit.Test;

import com.github.pfichtner.Connection;
import com.github.pfichtner.StreamConnection;
import com.github.pfichtner.ardulink.core.ConnectionManager;
import com.github.pfichtner.core.serial.DummyConnectionFactory.DummyConnection;
import com.github.pfichtner.core.serial.DummyConnectionFactory.DummyConnectionConfig;

public class DummyConnectionTest {

	@Test
	public void returnsNullOnInvalidNames() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager.getConnection(new URI(
				"ardulink://non_registered_and_not_existing_name"));
		assertThat(connection, nullValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void schemaHasToBeArdulink() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		connectionManager.getConnection(new URI("wrongSchema://dummy"));
	}

	@Test
	public void canCreateDummyDonnection() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager.getConnection(new URI(
				"ardulink://dummy"));
		assertThat(connection, is(notNullValue()));
	}

	@Test
	public void canConfigureDummyDonnection() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		String aValue = "aValue";
		int bValue = 1;
		String cValue = "cValue";
		int dValue = 42;
		DummyConnection connection = (DummyConnection) connectionManager
				.getConnection(new URI("ardulink://dummy?a=" + aValue + "&b="
						+ bValue + "&c=" + cValue + "&d=" + dValue));
		DummyConnectionConfig config = connection.getConfig();
		assertThat(config.a, is(aValue));
		assertThat(config.b, is(bValue));
		assertThat(config.c, is(cValue));
		assertThat(config.d, is(dValue));
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnInvalidKey() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		connectionManager.getConnection(new URI(
				"ardulink://dummy?nonExistingKey=someValue"));
	}

	@Test
	@Ignore
	public void canLoadClassViaServiceLoader() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager.getConnection(new URI(
				"ardulink://serial"));
		assertThat(connection.getClass().getName(),
				is(StreamConnection.class.getName()));
	}

}
