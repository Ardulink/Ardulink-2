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

package org.ardulink.camel;

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

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
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
