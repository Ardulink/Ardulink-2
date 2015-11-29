package com.github.pfichtner.ardulink.core.connectionmanager;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionManager.Configurer;

public class SerialConnectionFactoryIntegrationTest {

	@Test
	public void canConfigureSerialConnectionViaURI() throws Exception {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://serial?port=anyString&speed=9600"));
		try {
			assertNotNull(configurer.newConnection());
		} catch (Exception e) {
			if (!"gnu.io.NoSuchPortException".equals(e.getClass().getName())) {
				throw e;
			}
		}
	}

	@Test
	public void canConfigureSerialConnectionViaConfigurer() throws Exception {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://serial"));

		assertThat(new ArrayList<String>(configurer.getAttributes()),
				is(Arrays.asList("port", "speed")));

		ConfigAttribute port = configurer.getAttribute("port");
		ConfigAttribute speed = configurer.getAttribute("speed");

		assertThat(port.hasPossibleValues(), is(TRUE));
		assertThat(speed.hasPossibleValues(), is(FALSE));

		assertThat(port.getPossibleValues(), is(notNullValue()));

		port.setValue("anyString");
		speed.setValue(115200);
	}

}
