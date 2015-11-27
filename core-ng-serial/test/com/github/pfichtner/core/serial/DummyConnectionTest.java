package com.github.pfichtner.core.serial;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.github.pfichtner.Connection;
import com.github.pfichtner.StreamConnection;
import com.github.pfichtner.ardulink.core.ConnectionManager;
import com.github.pfichtner.ardulink.core.ConnectionManager.Configurer;
import com.github.pfichtner.core.serial.DummyConnectionFactory.DummyConnection;
import com.github.pfichtner.core.serial.DummyConnectionFactory.DummyConnectionConfig;

public class DummyConnectionTest {

	@Test
	public void returnsNullOnInvalidNames() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://non_registered_and_not_existing_name"));
		assertThat(configurer, nullValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void schemaHasToBeArdulink() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		connectionManager.getConfigurer(new URI("wrongSchema://dummy"));
	}

	@Test
	public void canCreateDummyDonnection() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager.getConfigurer(
				new URI("ardulink://dummy")).newConnection();
		assertThat(connection, is(notNullValue()));
	}

	@Test
	public void canConfigureDummyConnection() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		String aValue = "aValue";
		int bValue = 1;
		String cValue = "cValue";
		int dValue = 42;
		DummyConnection connection = (DummyConnection) connectionManager
				.getConfigurer(
						new URI("ardulink://dummy?a=" + aValue + "&b=" + bValue
								+ "&c=" + cValue + "&d=" + dValue))
				.newConnection();
		DummyConnectionConfig config = connection.getConfig();
		assertThat(config.a, is(aValue));
		assertThat(config.b, is(bValue));
		assertThat(config.c, is(cValue));
		assertThat(config.d, is(dValue));
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnInvalidKey() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		connectionManager.getConfigurer(new URI(
				"ardulink://dummy?nonExistingKey=someValue"));
	}

	@Test
	@Ignore
	public void canLoadClassViaServiceLoader() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager.getConfigurer(
				new URI("ardulink://serial")).newConnection();
		assertThat(connection.getClass().getName(),
				is(StreamConnection.class.getName()));
	}

	@Test
	public void canDefinePossibleValues() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager.getConfigurer(
				new URI("ardulink://dummy")).newConnection();
		assertThat(getPossibleValues(connection), is(Arrays.asList("v1", "v2")));
		fail();
	}

	private List<String> getPossibleValues(Connection connection) {
		// TODO Auto-generated method stub
		return null;
	}

}
