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

package org.ardulink.core.serial.rxtx;

import static java.net.URI.create;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.List;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.proto.ardulink.ArdulinkProtocol2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import gnu.io.NoSuchPortException;

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
	@Disabled("Link#close hangs since StreamReader calls read and this native method doesn't get interrupted even if the InputStream gets closed. That's the reason why RXTX's close does not get a writeLock since the lock remains locked")
	void canConfigureSerialConnectionViaURI() throws Exception {
		List<String> portNames = new SerialLinkConfig().listPorts().collect(toList());
		assumeFalse(portNames.isEmpty());

		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(
				create(PREFIX + "?port=" + portNames.get(0) + "&baudrate=9600&pingprobe=false&waitsecs=1"));
		try (Link link = configurer.newLink()) {
			assertThat(link).isNotNull();
		}
	}

	@Test
	void canConfigureSerialConnectionViaConfigurer() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(create(PREFIX));

		assertThat(configurer.getAttributes()).containsExactlyInAnyOrder( //
				"port", "baudrate", "proto", "qos", "waitsecs", "pingprobe");

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
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(create(PREFIX + "?baudrate=9600"));
		assertThatRuntimeException().isThrownBy(() -> {
			try (Link link = configurer.newLink()) {
			}
		}).withCauseInstanceOf(NoSuchPortException.class);
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
