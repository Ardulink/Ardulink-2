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

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import static io.restassured.http.ContentType.JSON;
import static java.awt.GraphicsEnvironment.isHeadless;
import static org.ardulink.util.ServerSockets.freePort;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import org.ardulink.rest.main.CommandLineArguments;
import org.ardulink.rest.main.RestMain;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;

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
			given().port(port).get("/api-docs").then().assertThat().statusCode(200).contentType(JSON) //
					.body("info.title", equalTo("User API")) //
					.body("paths", hasKey("/pin/analog/{pin}")) //
					.body("paths", hasKey("/pin/digital/{pin}")) //
			;
		}
	}

	@Test
	public void canAccesApiUi() throws Exception {
		try (RestMain main = runRestComponent()) {
			try (Playwright playwright = Playwright.create()) {
				Browser browser = playwright.chromium()
						.launch(new BrowserType.LaunchOptions().setHeadless(isHeadless()));
				BrowserContext context = browser.newContext();

				Page page = context.newPage();

				page.navigate("http://localhost:" + port + "/api-browser");

				page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("pin")).click();
				assertThat(page).hasURL("http://localhost:" + port + "/swagger-ui/4.14.0/index.html#/");

				Page page1 = page.waitForPopup(() -> {
					page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("/api-docs")).click();
				});
//				assertThat(page).hasURL("http://localhost:8080/swagger-ui/4.14.0/index.html#/");
			}
		}
	}

	private RestMain runRestComponent() throws Exception {
		CommandLineArguments args = new CommandLineArguments();
		args.connection = "direct:noop";
		args.port = port;
		return new RestMain(args);
	}

}
