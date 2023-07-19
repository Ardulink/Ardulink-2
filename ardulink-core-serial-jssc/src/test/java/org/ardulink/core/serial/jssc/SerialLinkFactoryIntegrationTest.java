/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.core.serial.jssc;

import static org.ardulink.util.URIs.newURI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.junit.jupiter.api.Test;

import jssc.SerialPortList;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class SerialLinkFactoryIntegrationTest {

	private static final String PREFIX = "ardulink://" + SerialLinkFactory.NAME;

	@Test
	void canConfigureSerialConnectionViaURI() throws Exception {
		String[] portNames = SerialPortList.getPortNames();
		assumeTrue(portNames.length > 0);

		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager
				.getConfigurer(newURI(PREFIX + "?port=" + portNames[0] + "&baudrate=9600&pingprobe=false&waitsecs=1"));
		try (Link link = configurer.newLink()) {
			assertNotNull(link);
		}
	}

	@Test
	void canConfigureSerialConnectionViaConfigurer() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(newURI(PREFIX));

		assertThat(configurer.getAttributes()).containsExactlyInAnyOrder("port", "baudrate", "proto", "qos", "waitsecs",
				"pingprobe");

		assertThat(attribute(configurer, "port").hasChoiceValues()).isTrue();
		assertThat(attribute(configurer, "proto").hasChoiceValues()).isTrue();
		assertThat(attribute(configurer, "baudrate").hasChoiceValues()).isFalse();
		assertThat(attribute(configurer, "qos").hasChoiceValues()).isFalse();
		assertThat(attribute(configurer, "waitsecs").hasChoiceValues()).isFalse();

		attribute(configurer, "port").setValue(anyString());
		attribute(configurer, "proto").setValue(validProtoName());
		attribute(configurer, "baudrate").setValue(anyInt());
		attribute(configurer, "qos").setValue(anyBoolean());
		attribute(configurer, "waitsecs").setValue(anyInt());
	}

	@Test
	void cantConnectWithoutPort() {
		Configurer configurer = LinkManager.getInstance().getConfigurer(newURI(PREFIX + "?baudrate=9600"));
		assertThat(assertThrows(RuntimeException.class, configurer::newLink)).hasMessageContaining("Port name - null");
	}

	private static String anyString() {
		return "anyString";
	}

	private static int anyInt() {
		return 42;
	}

	private static boolean anyBoolean() {
		return false;
	}

	private static String validProtoName() {
		return ArdulinkProtocol2.NAME;
	}

	private ConfigAttribute attribute(Configurer configurer, String name) {
		return configurer.getAttribute(name);
	}

}
