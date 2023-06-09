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
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.ardulink.testsupport.mock.TestSupport.fireEvent;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.testsupport.mock.TestSupport.uniqueMockUri;
import static org.ardulink.util.ServerSockets.freePort;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
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
			int pin = 5;
			boolean state = true;
			given().body(state).put("/pin/digital/{pin}", pin).then().statusCode(200);
			verify(getMock(link)).switchDigitalPin(digitalPin(pin), state);
		}
	}

	@Test
	void canSwitchAnalogPin() throws Exception {
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			int pin = 9;
			int value = 123;
			given().body(value).put("/pin/analog/{pin}", pin).then().statusCode(200);
			verify(getMock(link)).switchAnalogPin(analogPin(pin), value);
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
		int pin = 7;
		int value = 456;
		AnalogPinValueChangedEvent analogPinValueChanged = analogPinValueChanged(analogPin(pin), value);
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			fireEvent(link, analogPinValueChanged);
			given().get("/pin/analog/{pin}", pin).then().statusCode(200).body(is(String.valueOf(value)));
		}
	}

	@Test
	void canEnableAndDisableListeningDigitalPin() throws Exception {
		int pin = 5;
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			given().body("listen=true").patch("/pin/digital/{pin}", pin).then().statusCode(200);
			verify(getMock(link)).startListening(digitalPin(pin));
			given().body("listen=false").patch("/pin/digital/{pin}", pin).then().statusCode(200);
			verify(getMock(link)).stopListening(digitalPin(pin));
		}
	}

	@Test
	void canEnableAndDisableListeningAnalogPin() throws Exception {
		int pin = 7;
		try (Link link = Links.getLink(mockUri); RestMain main = runRestComponent(mockUri)) {
			given().body("listen=true").patch("/pin/analog/{pin}", pin).then().statusCode(200);
			verify(getMock(link)).startListening(analogPin(pin));
			given().body("listen=false").patch("/pin/analog/{pin}", pin).then().statusCode(200);
			verify(getMock(link)).stopListening(analogPin(pin));
		}
	}

	private RestMain runRestComponent(String target) throws Exception {
		CommandLineArguments args = new CommandLineArguments();
		args.connection = target;
		args.port = port;
		return new RestMain(args);
	}

}
