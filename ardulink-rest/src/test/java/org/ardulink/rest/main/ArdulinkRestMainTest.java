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

package org.ardulink.rest.main;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.util.ServerSockets.freePort;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkRestMainTest {

	@Before
	public void setup() {
		port = freePort();
	}

	@Test
	public void canParseArgs() throws IOException, CmdLineException {
		CommandLineArguments args = RestMain
				.tryParse("-connection", "ardulink://abc", "-bind", "someHost", "-port", "123").get();
		assertThat(args.connection, is("ardulink://abc"));
		assertThat(args.bind, is("someHost"));
		assertThat(args.port, is(123));
	}

	@Test
	public void canStartMainWithArgs() throws IOException, CmdLineException {
		CommandLineArguments args = new CommandLineArguments();
		args.connection = "ardulink://mock";
		args.port = port;
		RestMain restMain = new RestMain(args);
		try (Link mock = getMock(Links.getLink(args.connection))) {
			int pin = 5;
			boolean state = true;
			given().body(state).post("/pin/digital/{pin}", pin).then().statusCode(200);
			verify(mock).switchDigitalPin(digitalPin(pin), state);
		}
		restMain.stop();
	}

}
