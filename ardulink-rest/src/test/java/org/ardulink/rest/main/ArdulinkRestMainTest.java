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
import static org.ardulink.testsupport.mock.TestSupport.uniqueMockUri;
import static org.ardulink.util.ServerSockets.freePort;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.args4j.CmdLineException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ArdulinkRestMainTest {

	@BeforeEach
	void setup() {
		port = freePort();
	}

	@Test
	void canParseArgs() throws IOException, CmdLineException {
		CommandLineArguments args = RestMain
				.tryParse("-connection", "ardulink://abc", "-bind", "someHost", "-port", "123").orElseThrow();
		assertThat(args.connection).isEqualTo("ardulink://abc");
		assertThat(args.bind).isEqualTo("someHost");
		assertThat(args.port).isEqualTo(123);
	}

	@Test
	void canStartMainWithArgs() throws IOException, CmdLineException {
		CommandLineArguments args = args();
		try (Link link = Links.getLink(args.connection); RestMain restMain = new RestMain(args)) {
			int pin = 5;
			boolean state = true;
			given().body(state).put("/pin/digital/{pin}", pin).then().statusCode(200);
			verify(getMock(link)).switchDigitalPin(digitalPin(pin), state);
		}
	}

	private CommandLineArguments args() {
		CommandLineArguments args = new CommandLineArguments();
		args.connection = uniqueMockUri();
		args.port = port;
		return args;
	}

}
