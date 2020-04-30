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

package ardulink.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import static java.lang.Boolean.parseBoolean;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.ServerSockets.freePort;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkRestTest {

	private static final String MOCK_URI = "ardulink://mock";

	private Link link;

	@Before
	public void setup() {
		port = freePort();
		link = Links.getLink(MOCK_URI);
	}

	@After
	public void tearDown() throws Exception {
		link.close();
	}

	@Test
	public void canSwitchDigitalPin() throws Exception {
		Link mock = getMock(link);
		try (CamelContext context = startCamelRest(MOCK_URI)) {
			int pin = 5;
			boolean state = true;
			given().body(state).post("/pin/digital/{pin}", pin).then().statusCode(200);
			verify(mock).switchDigitalPin(digitalPin(pin), state);
			context.stop();
		}
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canSwitchAnalogPin() throws Exception {
		Link mock = getMock(link);
		try (CamelContext context = startCamelRest(MOCK_URI)) {
			int pin = 9;
			int value = 123;
			given().body(value).post("/pin/analog/{pin}", pin).then().statusCode(200);
			verify(mock).switchAnalogPin(analogPin(pin), value);
			context.stop();
		}
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private CamelContext startCamelRest(String out) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				restConfiguration().host("localhost").port(port);
				rest("/pin") //
						.consumes("application/json").produces("application/json") //
						.post("/analog/{pin}").to("direct:switchAnalog") //
						.post("/digital/{pin}").to("direct:switchDigital") //
				;

				from("direct:switchAnalog").process(exchange -> {
					Message message = exchange.getMessage();
					Object pinRaw = message.getHeader("pin");
					String valueRaw = message.getBody(String.class);
					int pin = tryParse(String.valueOf(pinRaw)).getOrThrow("Pin %s not parseable", pinRaw);
					int value = tryParse(valueRaw).getOrThrow("Value %s not parseable", valueRaw);
					message.setBody(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value));
				}).to(out);

				from("direct:switchDigital").process(exchange -> {
					Message message = exchange.getMessage();
					Object pinRaw = message.getHeader("pin");
					String stateRaw = message.getBody(String.class);
					int pin = tryParse(String.valueOf(pinRaw)).getOrThrow("Pin %s not parseable", pinRaw);
					boolean state = parseBoolean(stateRaw);
					message.setBody(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(state));
				}).to(out);
			}
		});
		context.start();
		return context;
	}

}
