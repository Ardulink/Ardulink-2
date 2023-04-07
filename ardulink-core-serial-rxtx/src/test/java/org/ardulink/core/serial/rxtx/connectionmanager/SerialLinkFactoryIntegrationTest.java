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

package org.ardulink.core.serial.rxtx.connectionmanager;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.ardulink.util.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.util.URIs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
// because JNI dependent
@Disabled
class SerialLinkFactoryIntegrationTest {

	@Test
	void canConfigureSerialConnectionViaURI() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager
				.getConfigurer(URIs.newURI("ardulink://serial?port=anyString&baudrate=9600"));
		assertNotNull(configurer.newLink());
	}

	@Test
	void canConfigureSerialConnectionViaConfigurer() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs.newURI("ardulink://serial"));

		assertThat(newArrayList(configurer.getAttributes())).isEqualTo(newArrayList("port", "baudrate", "proto", "qos", "waitsecs", "pingprobe", "searchport"));

		ConfigAttribute port = configurer.getAttribute("port");
		ConfigAttribute proto = configurer.getAttribute("proto");
		ConfigAttribute baudrate = configurer.getAttribute("baudrate");
		ConfigAttribute searchport = configurer.getAttribute("searchport");
		assertThat(searchport.hasChoiceValues()).isEqualTo(FALSE);

		assertThat(port.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(proto.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(baudrate.hasChoiceValues()).isEqualTo(FALSE);

		assertThat(port.getChoiceValues()).isNotNull();

		port.setValue("anyString");
		baudrate.setValue(115200);
	}

	@Test
	void cantConnectWithoutPort() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs.newURI("ardulink://serial?baudrate=9600"));

		ConfigAttribute searchport = configurer.getAttribute("searchport");
		assertEquals(searchport.getValue(), FALSE);

		assertThrows(IllegalStateException.class, () -> configurer.newLink());
	}

	@Test
	void canConnectWithoutPortButSearchEnabled() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager
				.getConfigurer(URIs.newURI("ardulink://serial?searchport=true&baudrate=9600&pingprobe=false"));

		ConfigAttribute searchport = configurer.getAttribute("searchport");
		assertEquals(searchport.getValue(), TRUE);

		// if there aren't devices connected an exception is thrown
		ConfigAttribute port = configurer.getAttribute("port");
		if (port.getChoiceValues().length == 0) {
			assertThat(assertThrows(RuntimeException.class, () -> configurer.newLink()).getMessage()).isEqualTo("no port found");
		} else {
			assertNotNull(configurer.newLink());
		}
	}
}
