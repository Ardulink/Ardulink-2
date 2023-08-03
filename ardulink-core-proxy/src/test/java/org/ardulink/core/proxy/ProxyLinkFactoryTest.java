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

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.proxy.ProxyServerDouble.portName;
import static org.ardulink.util.URIs.newURI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;

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
@Timeout(value = 5, unit = SECONDS)
class ProxyLinkFactoryTest {

	@RegisterExtension
	ProxyServerDouble proxyServerDouble = new ProxyServerDouble();

	@Test
	void canConnectWhileConfiguring() {
		proxyServerDouble.setNumberOfPorts(0);
		Configurer configurer = configurerForProxy();
		assertThat(configurer.getAttribute("port").getChoiceValues()).isEmpty();
	}

	@Test
	void canReadAvailablePorts() {
		proxyServerDouble.setNumberOfPorts(1);
		Configurer configurer = configurerForProxy();
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues()).containsExactly(portName(0));
		assertThatProxyServerReceived(proxyMessage("get_port_list"));
	}

	@Test
	void canConnect() throws IOException {
		proxyServerDouble.setNumberOfPorts(3);
		Configurer configurer = configurerForProxy();
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues()).containsExactly(portName(0), portName(1), portName(2));
		port.setValue(portName(2));

		try (Link newLink = configurer.newLink()) {
			assertThatProxyServerReceived( //
					proxyMessage("get_port_list"), //
					proxyMessage("get_port_list"), //
					proxyMessage("connect"), //
					portName(2), //
					"115200" //
			);
		}
	}

	@Test
	void canSwitchAnalogPort() throws IOException {
		proxyServerDouble.setNumberOfPorts(1);
		Configurer configurer = configurerForProxy();
		configurer.getAttribute("port").setValue(portName(0));
		try (Link newLink = configurer.newLink()) {
			// sends message to double
			newLink.switchAnalogPin(analogPin(1), 123);
			assertThatProxyServerReceived( //
					proxyMessage("get_port_list"), //
					proxyMessage("connect"), //
					portName(0), //
					"115200", //
					"alp://ppin/1/123" //
			);
		}
	}

	private Configurer configurerForProxy() {
		return configurerForProxy("localhost", proxyServerDouble.getLocalPort());
	}

	private static Configurer configurerForProxy(String host, int port) {
		return LinkManager.getInstance()
				.getConfigurer(newURI(format("ardulink://proxy?tcphost=%s&tcpport=%d", host, port)));
	}

	private static String proxyMessage(String message) {
		return "ardulink:networkproxyserver:" + message;
	}

	private void assertThatProxyServerReceived(String... expected) {
		await().forever().untilAsserted(() -> assertThat(proxyServerDouble.received()).containsExactly(expected));
	}

}
