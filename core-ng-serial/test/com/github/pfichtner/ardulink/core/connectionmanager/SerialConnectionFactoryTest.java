package com.github.pfichtner.ardulink.core.connectionmanager;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.StreamConnection;

public class SerialConnectionFactoryTest {

	@Test
	public void canLoadClassViaServiceLoader() throws URISyntaxException {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Connection connection = connectionManager.getConfigurer(
				new URI("ardulink://serial")).newConnection();
		assertThat(connection.getClass().getName(),
				is(StreamConnection.class.getName()));
	}

}
