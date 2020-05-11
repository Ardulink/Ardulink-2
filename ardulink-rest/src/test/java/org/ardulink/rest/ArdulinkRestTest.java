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
import static org.ardulink.testsupport.mock.StaticRegisterLinkFactory.ardulinkUri;
import static org.ardulink.testsupport.mock.StaticRegisterLinkFactory.register;
import static org.ardulink.testsupport.mock.TestSupport.createAbstractListenerLink;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.util.ServerSockets.freePort;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.rest.main.CommandLineArguments;
import org.ardulink.rest.main.RestMain;
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

	@Before
	public void setup() {
		port = freePort();
	}

	@Test
	public void canSwitchDigitalPin() throws Exception {
		try (Link link = Links.getLink("ardulink://mock")) {
			Link mock = getMock(link);
			try (RestMain main = startCamelRest("ardulink://mock")) {
				int pin = 5;
				boolean state = true;
				given().body(state).post("/pin/digital/{pin}", pin).then().statusCode(200);
				verify(mock).switchDigitalPin(digitalPin(pin), state);
			}
			verify(mock).close();
		}
	}

	@Test
	public void canSwitchAnalogPin() throws Exception {
		try (Link link = Links.getLink("ardulink://mock")) {
			Link mock = getMock(link);
			try (RestMain main = startCamelRest("ardulink://mock")) {
				int pin = 9;
				int value = 123;
				given().body(value).post("/pin/analog/{pin}", pin).then().statusCode(200);
				verify(mock).switchAnalogPin(analogPin(pin), value);
			}
			verify(mock).close();
		}
	}

	@Test
	public void canReadDigitalPin() throws Exception {
		int pin = 5;
		boolean state = true;
		try (AbstractListenerLink link = createAbstractListenerLink(digitalPinValueChanged(digitalPin(pin), state));
				RestMain main = startCamelRest(ardulinkUri(register(link)))) {
			given().get("/pin/digital/{pin}", pin).then().statusCode(200).body(is(String.valueOf(state)));
		}
	}

	@Test
	public void canReadAnalogPin() throws Exception {
		int pin = 7;
		int value = 456;
		try (AbstractListenerLink link = createAbstractListenerLink(analogPinValueChanged(analogPin(pin), value));
				RestMain main = startCamelRest(ardulinkUri(register(link)))) {
			given().get("/pin/analog/{pin}", pin).then().statusCode(200).body(is(String.valueOf(value)));
		}
	}

	@Test
	public void canEnableAndDisableListeningDigitalPin() throws Exception {
		int pin = 5;
		try (Link link = Links.getLink("ardulink://mock")) {
			Link mock = getMock(link);
			try (RestMain main = startCamelRest("ardulink://mock")) {
				given().body("listen=true").patch("/pin/digital/{pin}", pin).then().statusCode(200);
				verify(mock).startListening(digitalPin(pin));
				given().body("listen=false").patch("/pin/digital/{pin}", pin).then().statusCode(200);
				verify(mock).stopListening(digitalPin(pin));
			}
			verify(mock).close();
		}
	}

	@Test
	public void canEnableAndDisableListeningAnalogPin() throws Exception {
		int pin = 7;
		try (Link link = Links.getLink("ardulink://mock")) {
			Link mock = getMock(link);
			try (RestMain main = startCamelRest("ardulink://mock")) {
				given().body("listen=true").patch("/pin/analog/{pin}", pin).then().statusCode(200);
				verify(mock).startListening(analogPin(pin));
				given().body("listen=false").patch("/pin/analog/{pin}", pin).then().statusCode(200);
				verify(mock).stopListening(analogPin(pin));
			}
			verify(mock).close();
		}
	}

	private RestMain startCamelRest(String target) throws Exception {
		CommandLineArguments args = new CommandLineArguments();
		args.connection = target;
		args.port = port;
		return new RestMain(args);
	}

}
