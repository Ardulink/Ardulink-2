package com.github.pfichtner.core.proxy;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

public class ProxyLinkFactoryTest {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public ProxyServerDouble proxyServerDouble = new ProxyServerDouble();

	@Test
	public void canConnectWhileConfiguring() throws Exception {
		proxyServerDouble.setNumberOfPorts(0);
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?tcphost=localhost&tcpport="
						+ proxyServerDouble.getLocalPort()));
		ConfigAttribute port = configurer.getAttribute("port");
		assertThat(port.getChoiceValues(), is(new Object[0]));
	}

	@Test
	public void canReadAvailablePorts() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?tcphost=localhost&tcpport="
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
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?tcphost=localhost&tcpport="
						+ proxyServerDouble.getLocalPort()));
		ConfigAttribute port = configurer.getAttribute("port");
		Object[] values = (Object[]) new String[] { "myPortNr0" };
		assertThat(port.getChoiceValues(), is(values));
		port.setValue(values[0]);

		configurer.newLink().close();
		assertThat(proxyServerDouble.getReceived(), is(Arrays.asList(
				"ardulink:networkproxyserver:get_port_list",
				"ardulink:networkproxyserver:connect", "myPortNr0", "115200")));
	}

	@Test
	public void canSwitchAnalogPort() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://proxy?tcphost=localhost&tcpport="
						+ proxyServerDouble.getLocalPort()));
		Object[] values = (Object[]) new String[] { "myPortNr0" };
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
