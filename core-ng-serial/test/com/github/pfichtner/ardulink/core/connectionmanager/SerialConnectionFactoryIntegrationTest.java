package com.github.pfichtner.ardulink.core.connectionmanager;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.junit.Test;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.StreamConnection;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionManager.Configurer;

public class SerialConnectionFactoryIntegrationTest {

	@Test
	public void canLoadClassViaServiceLoader() throws Exception {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		// TODO OS dependent!? Windows/Linux
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://serial?port=/dev/ttyUSB0&speed=115200"));

		configurer.getAttributeSetter("speed").setValue(115200);
		Object[] possibleValues = configurer.getAttributeSetter("port")
				.getPossibleValues();
		assertThat(possibleValues, is(notNullValue()));

		Connection connection = configurer.newConnection();
		assertThat(connection.getClass().getName(),
				is(StreamConnection.class.getName()));
	}

}
