package org.ardulink.mqtt;

import static org.ardulink.core.Pin.digitalPin;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;
import org.ardulink.mqtt.util.AnotherMqttClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MqttOnCamelIntegrationTest {

	private static final String mockURI = "ardulink://mock";

	private Link link;

	@Before
	public void setup() throws URISyntaxException, Exception {
		link = Links.getLink(new URI(mockURI));
	}

	@After
	public void tearDown() throws IOException {
		link.close();
	}

	@Test
	public void xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx() throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(mqtt()).to(mockURI);
			}

		});
		context.start();

		AnotherMqttClient amc = AnotherMqttClient.builder().connect();
		amc.switchPin(Pin.digitalPin(2), true);

		Link mock = getMock(link);
		verify(mock).switchDigitalPin(digitalPin(2), true);
	}

	private Link getMock(Link link) {
		return ((LinkDelegate) link).getDelegate();
	}

	private String mqtt() {
		return "mqtt://.....";
	}

}
