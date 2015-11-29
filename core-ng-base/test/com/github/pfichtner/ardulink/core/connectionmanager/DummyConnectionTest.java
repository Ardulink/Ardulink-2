package com.github.pfichtner.ardulink.core.connectionmanager;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionManager.Configurer;
import com.github.pfichtner.ardulink.core.connectionmanager.DummyConnectionFactory.DummyConnection;
import com.github.pfichtner.ardulink.core.connectionmanager.DummyConnectionFactory.DummyConnectionConfig;

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
	public void canCreateDummyDonnection() throws Exception {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager.getConfigurer(
				new URI("ardulink://dummy")).newConnection();
		assertThat(connection, is(notNullValue()));
	}

	@Test
	public void canConfigureDummyConnection() throws Exception {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		String aValue = "aValue";
		int bValue = 1;
		String cValue = "cValue";
		DummyConnection connection = (DummyConnection) connectionManager
				.getConfigurer(
						new URI("ardulink://dummy?a=" + aValue + "&b=" + bValue
								+ "&c=" + cValue)).newConnection();
		DummyConnectionConfig config = connection.getConfig();
		assertThat(config.a, is(aValue));
		assertThat(config.b, is(bValue));
		assertThat(config.c, is(cValue));
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnInvalidKey() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		connectionManager.getConfigurer(new URI(
				"ardulink://dummy?nonExistingKey=someValue"));
	}

	@Test
	public void canDefinePossibleValues() throws Exception {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://dummy"));
		ConfigAttribute a = configurer.getAttribute("a");
		assertThat(a.hasPossibleValues(), is(TRUE));
		assertThat(a.getPossibleValues(), is(new Object[] { "aVal1", "aVal2" }));

		assertThat(configurer.getAttribute("b").hasPossibleValues(), is(FALSE));
		assertThat(configurer.getAttribute("c").hasPossibleValues(), is(FALSE));
	}

	@Test
	public void canIterateRegisteredFactories() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		assertThat(connectionManager.listURIs(),
				is(Arrays.asList(new URI("ardulink://dummy"))));
	}

}
