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

package org.ardulink.core.proxy;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.proxy.ProxyServerDouble.portName;
import static org.ardulink.util.URIs.newURI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Timeout(5)
class ProxyLinkFactoryTest {

	@RegisterExtension
	ProxyServerDouble proxyServerDouble = new ProxyServerDouble();
	LinkManager linkManager = LinkManager.getInstance();

	@Test
	void canConnectWhileConfiguring() {
		proxyServerDouble.setNumberOfPorts(0);
		Configurer configurer = linkManager.getConfigurer(
				newURI("ardulink://proxy?tcphost=localhost&tcpport=" + proxyServerDouble.getLocalPort()));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues()).isEmpty();
	}

	@Test
	void canReadAvailablePorts() {
		proxyServerDouble.setNumberOfPorts(1);
		Configurer configurer = linkManager.getConfigurer(
				newURI("ardulink://proxy?tcphost=localhost&tcpport=" + proxyServerDouble.getLocalPort()));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues()).containsExactly(portName(0));
		await().untilAsserted(() -> assertThat(proxyServerDouble.received()).singleElement()
				.isEqualTo("ardulink:networkproxyserver:get_port_list"));
	}

	@Test
	void canConnect() throws Exception {
		proxyServerDouble.setNumberOfPorts(1);
		Configurer configurer = linkManager.getConfigurer(
				newURI("ardulink://proxy?tcphost=localhost&tcpport=" + proxyServerDouble.getLocalPort()));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues()).containsExactly(portName(0));
		port.setValue(portName(0));

		try (Link newLink = configurer.newLink()) {
			await().untilAsserted(() -> assertThat(proxyServerDouble.received()).containsExactly(
					"ardulink:networkproxyserver:get_port_list", "ardulink:networkproxyserver:get_port_list",
					"ardulink:networkproxyserver:connect", portName(0), "115200"));
		}
	}

	@Test
	void canSwitchAnalogPort() throws Exception {
		proxyServerDouble.setNumberOfPorts(1);
		Configurer configurer = linkManager.getConfigurer(
				newURI("ardulink://proxy?tcphost=localhost&tcpport=" + proxyServerDouble.getLocalPort()));
		configurer.getAttribute("port").setValue(portName(0));
		try (Link newLink = configurer.newLink()) {
			// sends message to double
			newLink.switchAnalogPin(analogPin(1), 123);
			await().untilAsserted(() -> assertThat(proxyServerDouble.received()).containsExactly(
					"ardulink:networkproxyserver:get_port_list", "ardulink:networkproxyserver:connect", portName(0),
					"115200", "alp://ppin/1/123"));
		}
	}

}
