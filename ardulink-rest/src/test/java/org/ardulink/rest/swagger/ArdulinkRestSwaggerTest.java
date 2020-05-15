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

package org.ardulink.rest.swagger;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import static io.restassured.http.ContentType.HTML;
import static io.restassured.http.ContentType.JSON;
import static org.ardulink.util.ServerSockets.freePort;

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
public class ArdulinkRestSwaggerTest {

	@Before
	public void setup() {
		port = freePort();
	}

	@Test
	public void canAccesApiDoc() throws Exception {
		try (RestMain main = runRestComponent()) {
			given().get("/api-docs").then().contentType(JSON).statusCode(200);
		}
	}

	@Test
	public void canAccesApiUi() throws Exception {
		try (RestMain main = runRestComponent()) {
			given().get("/api-browser").then().contentType(HTML).statusCode(200);
		}
	}

	private RestMain runRestComponent() throws Exception {
		CommandLineArguments args = new CommandLineArguments();
		args.connection = "direct:noop";
		args.port = port;
		return new RestMain(args);
	}

}
