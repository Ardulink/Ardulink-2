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
import static org.ardulink.rest.RestRouteBuilder.VAR_PORT;
import static org.ardulink.rest.RestRouteBuilder.VAR_TARGET;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.util.MapBuilder.newMapBuilder;
import static org.ardulink.util.ServerSockets.freePort;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Properties;

import org.apache.camel.main.Main;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.rest.RestRouteBuilder;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

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
	public void canStartMainWithArgs() throws IOException, CmdLineException {
		CommandLineArguments args = parse("-connection", "ardulink://mock", "-port", String.valueOf(port));
		Main main = main(args);
		main.start();
		try (Link mock = getMock(Links.getLink("ardulink://mock"))) {
			int pin = 5;
			boolean state = true;
			given().body(state).post("/pin/digital/{pin}", pin).then().statusCode(200);
			verify(mock).switchDigitalPin(digitalPin(pin), state);
		}
		main.stop();
	}

	private Main main(CommandLineArguments args) {
		Main main = new Main();
		main.setInitialProperties(toCamelProperties(args));
		main.addRoutesBuilder(new RestRouteBuilder());
		return main;
	}

	private Properties toCamelProperties(CommandLineArguments args) {
		return newMapBuilder().put(VAR_TARGET, args.connection).put(VAR_PORT, port).asProperties();
	}

	private CommandLineArguments parse(String... args) throws CmdLineException {
		CommandLineArguments cmdLineArgs = new CommandLineArguments();
		CmdLineParser cmdLineParser = new CmdLineParser(cmdLineArgs);
		try {
			cmdLineParser.parseArgument(args);
			return cmdLineArgs;
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage(System.err);
			throw e;
		}
	}
}
