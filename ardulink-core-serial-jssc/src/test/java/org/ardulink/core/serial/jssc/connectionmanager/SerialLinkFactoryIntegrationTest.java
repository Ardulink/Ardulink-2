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

package org.ardulink.core.serial.jssc.connectionmanager;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.ardulink.util.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.util.URIs;
import org.junit.jupiter.api.Disabled;
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

	@Test
	void canConfigureSerialConnectionViaURI() throws Exception {
		String[] portNames = SerialPortList.getPortNames();
		assumeTrue(portNames.length > 0);

		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://serial-jssc?port=" + portNames[0] + "&baudrate=9600&pingprobe=false&waitsecs=1"));
		try (Link link = configurer.newLink()) {
			assertNotNull(link);
		}
	}

	@Test
	void canConfigureSerialConnectionViaConfigurer() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs.newURI("ardulink://serial-jssc"));

		assertThat(configurer.getAttributes()).containsExactly("port", "baudrate", "proto", "qos", "waitsecs",
				"pingprobe");

		ConfigAttribute port = configurer.getAttribute("port");
		ConfigAttribute proto = configurer.getAttribute("proto");
		ConfigAttribute baudrate = configurer.getAttribute("baudrate");

		assertThat(port.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(port.getChoiceValues()).isNotNull();
		assertThat(proto.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(baudrate.hasChoiceValues()).isEqualTo(FALSE);

		port.setValue("anyString");
		baudrate.setValue(115200);
	}

	@Test
	void cantConnectWithoutPort() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs.newURI("ardulink://serial-jssc?baudrate=9600"));
		assertThat(assertThrows(RuntimeException.class, () -> configurer.newLink()))
				.hasMessageContaining("Port name - null");
	}

}
