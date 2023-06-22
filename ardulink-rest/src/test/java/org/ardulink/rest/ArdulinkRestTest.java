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

package org.ardulink.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.testsupport.mock.TestSupport.fireEvent;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.testsupport.mock.TestSupport.uniqueMockUri;
import static org.ardulink.util.ServerSockets.freePort;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;

import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.convenience.Links;
import org.ardulink.rest.main.CommandLineArguments;
import org.ardulink.rest.main.RestMain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ArdulinkRestTest {

	String mockUri = uniqueMockUri();

	@BeforeEach
	void setup() {
		port = freePort();
	}

	@Test
	void canSwitchDigitalPin() throws Exception {
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			DigitalPin pin = digitalPin(5);
			boolean state = true;
			given().body(state).put("/pin/digital/{pin}", pin.pinNum()).then().statusCode(200).and().body(
					is(okResponseWith(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin.pinNum()).withState(state))));
			verify(getMock(link)).switchDigitalPin(pin, state);
		}
	}

	@Test
	void canSwitchAnalogPin() throws Exception {
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			AnalogPin pin = analogPin(9);
			int value = 123;
			given().body(value).put("/pin/analog/{pin}", pin.pinNum()).then().statusCode(200).and().body(
					is(okResponseWith(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin.pinNum()).withValue(value))));
			verify(getMock(link)).switchAnalogPin(pin, value);
		}
	}

	@Test
	void linkGetsClosedByRestMain() throws Exception {
		try (Link link = Links.getLink(mockUri)) {
			try (RestMain main = runRestComponent(mockUri)) {
			}
			verify(getMock(link)).close();
		}
	}

	@Test
	void canReadDigitalPin() throws Exception {
		int pin = 5;
		boolean state = true;
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			fireEvent(link, digitalPinValueChanged(digitalPin(pin), state));
			given().get("/pin/digital/{pin}", pin).then().statusCode(200).body(is(String.valueOf(state)));
		}
	}

	@Test
	void canReadAnalogPin() throws Exception {
		AnalogPin pin = analogPin(7);
		int value = 456;
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			fireEvent(link, analogPinValueChanged(pin, value));
			given().get("/pin/analog/{pin}", pin.pinNum()).then().statusCode(200).body(is(String.valueOf(value)));
		}
	}

	@Test
	void timeoutWhenWaitingForDigitalMessageWithoutMatchingDigitalMessage() throws Exception {
		AnalogPin pin = analogPin(7);
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			fireEvent(link, analogPinValueChanged(pin, 456));
			forAllPinsNotEqualTo(pin, p -> fireEvent(link, digitalPinValueChanged(digitalPin(p), true)));
			given().get("/pin/digital/{pin}", pin.pinNum()).then().statusCode(500).and()
					.body(containsString("Timeout"));
		}
	}

	@Test
	void timeoutWhenWaitingForAnalogMessageWithoutMatchingAnalogMessage() throws Exception {
		DigitalPin pin = digitalPin(7);
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			fireEvent(link, digitalPinValueChanged(pin, true));
			forAllPinsNotEqualTo(pin, p -> fireEvent(link, analogPinValueChanged(analogPin(p), 456)));
			given().get("/pin/analog/{pin}", pin.pinNum()).then().statusCode(500).and().body(containsString("Timeout"));
		}
	}

	@Test
	void queueDoesNotExplode() throws Exception {
		AnalogPin pin = analogPin(7);
		int lastValue = 1023;
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			range(0, 200).forEach(__ -> rangeClosed(0, lastValue).forEach(v -> {
				fireEvent(link, analogPinValueChanged(pin, v));
			}));
			given().get("/pin/analog/{pin}", pin.pinNum()).then().statusCode(200).body(is(String.valueOf(lastValue)));
		}
	}

	@Test
	void canEnableAndDisableListeningDigitalPin() throws Exception {
		DigitalPin pin = digitalPin(5);
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			given().body("listen=true").patch("/pin/digital/{pin}", pin.pinNum()).then().statusCode(200).and().body(is(
					okResponseWith(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(pin.pinNum()).withoutValue())));
			verify(getMock(link)).startListening(pin);

			given().body("listen=false").patch("/pin/digital/{pin}", pin.pinNum()).then().statusCode(200).and().body(
					is(okResponseWith(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(pin.pinNum()).withoutValue())));
			verify(getMock(link)).stopListening(pin);
		}
	}

	@Test
	void canEnableAndDisableListeningAnalogPin() throws Exception {
		AnalogPin pin = analogPin(7);
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			given().body("listen=true").patch("/pin/analog/{pin}", pin.pinNum()).then().statusCode(200).and().body(
					is(okResponseWith(alpProtocolMessage(START_LISTENING_ANALOG).forPin(pin.pinNum()).withoutValue())));
			verify(getMock(link)).startListening(pin);

			given().body("listen=false").patch("/pin/analog/{pin}", pin.pinNum()).then().statusCode(200).and().body(
					is(okResponseWith(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(pin.pinNum()).withoutValue())));
			verify(getMock(link)).stopListening(pin);
		}
	}

	private RestMain runRestComponent(String target) throws Exception {
		CommandLineArguments args = new CommandLineArguments();
		args.connection = target;
		args.port = port;
		return new RestMain(args);
	}

	private static void forAllPinsNotEqualTo(Pin pin, IntConsumer action) {
		allPinsNotEqualTo(pin).forEach(action);
	}

	private static IntStream allPinsNotEqualTo(Pin pin) {
		return range(0, 100).filter(p -> pin.pinNum() != p);
	}

	private static String okResponseWith(String string) {
		return string + "=OK";
	}

}
