package org.ardulink.camel.test;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.testsupport.mock.TestSupport.fireEvent;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.testsupport.mock.junit5.MockUri;
import org.junit.jupiter.api.Test;

class ArdulinkConsumerIntegrationTest {

	String camelMockOut = "mock:result";

	@Test
	void messageIsSentOnAnalogPinChange(@MockUri String mockUri) throws Exception {
		int pin = 2;
		int value = 42;
		try (Link link = Links.getLink(mockUri); CamelContext context = camelContext(mockUri)) {
			fireEvent(link, analogPinValueChanged(analogPin(pin), value));
			MockEndpoint out = getMockEndpoint(context);
			out.expectedBodiesReceived(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value));
			out.assertIsSatisfied();
		}
	}

	@Test
	void messageIsSentOnDigitalPinChange(@MockUri String mockUri) throws Exception {
		int pin = 3;
		boolean state = true;
		try (Link link = Links.getLink(mockUri); CamelContext context = camelContext(mockUri)) {
			fireEvent(link, digitalPinValueChanged(digitalPin(pin), state));
			MockEndpoint out = getMockEndpoint(context);
			out.expectedBodiesReceived(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(state));
			out.assertIsSatisfied();
		}
	}

	private CamelContext camelContext(String from) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(from).to(camelMockOut);
			}
		});
		context.start();
		return context;
	}

	private MockEndpoint getMockEndpoint(CamelContext context) {
		return context.getEndpoint(camelMockOut, MockEndpoint.class);
	}

}
