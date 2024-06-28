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
import static io.restassured.http.ContentType.JSON;
import static java.awt.GraphicsEnvironment.isHeadless;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.testsupport.mock.TestSupport.uniqueMockUri;
import static org.ardulink.util.ServerSockets.freePort;
import static org.ardulink.util.Strings.nullOrEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.nio.file.Paths;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.rest.main.CommandLineArguments;
import org.ardulink.rest.main.RestMain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;

import io.restassured.RestAssured;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ArdulinkRestSwaggerTest {

	private static final long TIMEOUT = SECONDS.toMillis(5);
	private static final String SYS_PROP_PREFIX = "ardulink.test.";
	private static final String MOCK_URI = uniqueMockUri();

	@BeforeEach
	void setup() {
		RestAssured.port = freePort();
	}

	@Test
	void canAccesApiDoc() throws Exception {
		try (RestMain main = runRestComponent()) {
			given().port(RestAssured.port).get("/api-docs").then().assertThat().statusCode(200).contentType(JSON) //
					.body("info.title", equalTo("User API")) //
					.body("paths", hasKey("/pin/analog/{pin}")) //
					.body("paths", hasKey("/pin/digital/{pin}")) //
			;
		}
	}

	@Test
	void canAccesApiUi_GotoApiDocs() throws Exception {
		try (RestMain main = runRestComponent()) {
			try (Playwright playwright = Playwright.create()) {
				Browser browser = browser(playwright.chromium());
				BrowserContext context = browserContext(browser);

				Page page = context.newPage();

				page.navigate(format("http://localhost:%d/api-browser", RestAssured.port));

				Page page1 = page.waitForPopup(() -> {
					page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("/api-docs")).click();
				});
			}
		}
	}

	@Test
	void canAccesApiUi_ExecPutRequestViaApiBrowser() throws Exception {
		int pin = 13;
		int value = 42;
		try (RestMain main = runRestComponent()) {
			try (Playwright playwright = Playwright.create(); //
					Browser browser = browser(playwright.chromium()); //
					BrowserContext context = browserContext(browser); //
					Page page = context.newPage() //
			) {
				page.navigate(format("http://localhost:%d/api-browser", RestAssured.port));
				page.getByRole(AriaRole.BUTTON,
						new Page.GetByRoleOptions().setName("PUT /pin/analog/{pin}").setExact(true)).click();
				page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Try it out")).click();
				page.getByPlaceholder("pin").click();
				page.getByPlaceholder("pin").fill("13");
				page.getByLabel("Edit Value").getByText("string").click();
				page.getByLabel("Edit Value").getByText("string").press("ControlOrMeta+a");
				page.getByLabel("Edit Value").getByText("string").fill("42");
				page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Execute")).click();

				try (Link mock = getMock(Links.getLink(MOCK_URI))) {
					verify(mock, timeout(TIMEOUT)).switchAnalogPin(analogPin(pin), value);
				}

				// do a click into the result field (for the video)
				page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName("200")).first().click();
				page.locator("pre")
						.filter(new Locator.FilterOptions().setHasText(format("alp://ared/%d/%d=OK", pin, value)))
						.click();

			}
		}
	}

	private static Browser browser(BrowserType browserType) {
		return browserType.launch(new BrowserType.LaunchOptions().setHeadless(!showBrowser()));
	}

	private static boolean showBrowser() {
		return !isHeadless() && parseBoolean(System.getProperty(SYS_PROP_PREFIX + "playwright.showbrowser"));
	}

	private static BrowserContext browserContext(Browser browser) {
		Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions();
		String videoPath = System.getProperty(SYS_PROP_PREFIX + "playwright.video.path");
		return browser.newContext(nullOrEmpty(videoPath) //
				? newContextOptions //
				: newContextOptions.setRecordVideoDir(Paths.get(videoPath)).setRecordVideoSize(1024, 800));
	}

	private RestMain runRestComponent() throws Exception {
		CommandLineArguments args = new CommandLineArguments();
		args.connection = MOCK_URI;
		args.port = RestAssured.port;
		return new RestMain(args);
	}

}
