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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.util.URIs;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ProxyLinkFactoryTest {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public ProxyServerDouble proxyServerDouble = new ProxyServerDouble();

	private static final Object[] emptyArray = new Object[0];

	@Test
	public void canConnectWhileConfiguring() {
		proxyServerDouble.setNumberOfPorts(0);
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://proxy?tcphost=localhost&tcpport="
						+ proxyServerDouble.getLocalPort()));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues(), is(emptyArray));
	}

	@Test
	public void canReadAvailablePorts() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://proxy?tcphost=localhost&tcpport="
						+ proxyServerDouble.getLocalPort()));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues(),
				is((Object[]) new String[] { "myPortNr0" }));
		assertThat(proxyServerDouble.getReceived(),
				is(Arrays.asList("ardulink:networkproxyserver:get_port_list")));
	}

	@Test
	public void canConnect() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://proxy?tcphost=localhost&tcpport="
						+ proxyServerDouble.getLocalPort()));
		ConfigAttribute port = configurer.getAttribute("port");
		Object[] values = new String[] { "myPortNr0" };
		assertThat(port.getChoiceValues(), is(values));
		port.setValue(values[0]);

		configurer.newLink().close();
		assertThat(proxyServerDouble.getReceived(), is(Arrays.asList(
				"ardulink:networkproxyserver:get_port_list",
				"ardulink:networkproxyserver:get_port_list",
				"ardulink:networkproxyserver:connect", "myPortNr0", "115200")));
	}

	@Test
	public void canSwitchAnalogPort() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://proxy?tcphost=localhost&tcpport="
						+ proxyServerDouble.getLocalPort()));
		Object[] values = new String[] { "myPortNr0" };
		configurer.getAttribute("port").setValue(values[0]);
		Link newLink = configurer.newLink();

		// sends message to double
		newLink.switchAnalogPin(Pin.analogPin(1), 123);
		assertThat(proxyServerDouble.getReceived(), is(Arrays.asList(
				"ardulink:networkproxyserver:get_port_list",
				"ardulink:networkproxyserver:connect", "myPortNr0", "115200",
				"alp://ppin/1/123")));

		newLink.close();
	}

}
