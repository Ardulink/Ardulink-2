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

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

public class SerialLinkFactoryIntegrationTest {

	@Test
	public void canConfigureSerialConnectionViaURI() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://serial?port=anyString&speed=9600"));
		try {
			assertNotNull(configurer.newLink());
		} catch (Exception e) {
			if (!"gnu.io.NoSuchPortException".equals(e.getClass().getName())) {
				throw e;
			}
		}
	}

	@Test
	public void canConfigureSerialConnectionViaConfigurer() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://serial"));

		assertThat(new ArrayList<String>(configurer.getAttributes()),
				is(Arrays.asList("port", "proto", "speed")));

		ConfigAttribute port = configurer.getAttribute("port");
		ConfigAttribute proto = configurer.getAttribute("proto");
		ConfigAttribute speed = configurer.getAttribute("speed");

		assertThat(port.hasPossibleValues(), is(TRUE));
		assertThat(proto.hasPossibleValues(), is(FALSE));
		assertThat(speed.hasPossibleValues(), is(FALSE));

		assertThat(port.getPossibleValues(), is(notNullValue()));

		port.setValue("anyString");
		speed.setValue(115200);
	}

}
