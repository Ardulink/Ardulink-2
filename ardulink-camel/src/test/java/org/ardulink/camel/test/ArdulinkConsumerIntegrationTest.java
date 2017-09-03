package org.ardulink.camel.test;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import org.ardulink.util.URIs;
import org.junit.After;
import org.junit.Test;

public class ArdulinkConsumerIntegrationTest {

	// why not a mock? The tests wants to assure that ArdulinkConsumer's
	// listener will work correctly. To do so, we need a Link where
	// ArdulinkConsumer can register it's listener. Because of that, Link cannot
	// be a Mock.
	private static final String MOCK_URI = "ardulink://connectionBasedMockLink";

	private static final String OUT = "mock:result";

	private final Link link = Links.getLink(URIs.newURI(MOCK_URI));

	private CamelContext context;

	@After
	public void tearDown() throws Exception {
		link.close();
		context.stop();
	}

	@Test
	public void mqttMessageIsSentOnAnalogPinChange() throws Exception {
		int pin = 2;
		int value = 42;
		context = camelContext();
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(ANALOG_PIN_READ).forPin(
				pin).withValue(value));
		getDelegate().fireStateChanged(
				new DefaultAnalogPinValueChangedEvent(analogPin(pin), value));
		out.assertIsSatisfied();
	}

	@Test
	public void mqttMessageIsSentOnDigitalPinChange() throws Exception {
		int pin = 3;
		boolean state = true;
		context = camelContext();
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(DIGITAL_PIN_READ).forPin(
				pin).withState(state));
		getDelegate().fireStateChanged(
				new DefaultDigitalPinValueChangedEvent(digitalPin(pin), state));
		out.assertIsSatisfied();
	}

	private CamelContext camelContext() throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(MOCK_URI).to(OUT);
			}
		});
		context.start();
		return context;
	}

	private MockEndpoint getMockEndpoint() {
		return context.getEndpoint(OUT, MockEndpoint.class);
	}

	private AbstractListenerLink getDelegate() {
		return (AbstractListenerLink) ((LinkDelegate) link).getDelegate();
	}

}
