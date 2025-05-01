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
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.testsupport.mock.TestSupport.uniqueMockUri;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.ServerSockets.freePort;
import static org.ardulink.util.SystemProperties.isPropertySet;
import static org.ardulink.util.SystemProperties.systemProperty;
import static org.assertj.core.api.Assertions.assertThatNoException;
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

import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RecordVideoSize;

import io.restassured.RestAssured;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@UsePlaywright(ArdulinkRestSwaggerTest.CustomOptions.class)
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
	void canAccesApiUi_GotoApiDocs(Page page) throws Exception {
		assertThatNoException().isThrownBy(() -> {
			try (RestMain main = runRestComponent()) {
				page.navigate(format("http://localhost:%d/api-browser", RestAssured.port));
				page.waitForPopup(
						page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("/api-docs"))::click);
			}
		});
	}

	@Test
	void canAccesApiUi_ExecPutRequestViaApiBrowser(Page page) throws Exception {
		int pin = 13;
		int value = 42;
		try (RestMain main = runRestComponent()) {
			page.navigate(format("http://localhost:%d/api-browser", RestAssured.port));
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("PUT /pin/analog/{pin}").setExact(true))
					.click();
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Try it out")).click();

			Locator pinLocator = page.getByPlaceholder("pin");
			pinLocator.click();
			pinLocator.fill(String.valueOf(pin));

			Locator editValueLocator = page.getByLabel("Edit Value").getByText("string");
			editValueLocator.click();
			editValueLocator.press("ControlOrMeta+a");
			editValueLocator.fill(String.valueOf(value));
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Execute")).click();

			try (Link mock = getMock(Links.getLink(MOCK_URI))) {
				verify(mock, timeout(TIMEOUT)).switchAnalogPin(analogPin(pin), value);
			}

			// do a click into the result field (for the video)
			page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName("200")).first().click();
			page.locator("pre")
					.filter(new Locator.FilterOptions().setHasText(format("alp://ared/%d/%d=OK", pin, value))).click();
		}
	}

	private RestMain runRestComponent() throws Exception {
		CommandLineArguments args = new CommandLineArguments();
		args.connection = MOCK_URI;
		args.port = RestAssured.port;
		return new RestMain(args);
	}

	public static class CustomOptions implements OptionsFactory {

		@Override
		public Options getOptions() {
			return options();
		}

		private static Options options() {
			Options options = new Options().setHeadless(headless());
			return systemProperty(SYS_PROP_PREFIX + "playwright.video.path") //
					.map(p -> options.setContextOptions(new NewContextOptions().setRecordVideoDir(Paths.get(p))
							.setRecordVideoSize(recordVideoSize()))) //
					.orElse(options);
		}

		private static RecordVideoSize recordVideoSize() {
			return systemProperty(SYS_PROP_PREFIX + "playwright.video.size") //
					.map(s -> {
						String[] values = s.split("[ ,x*]");
						checkArgument(values.length == 2, "Cannot split %s into two parts (got %s)", s, values.length);
						return new RecordVideoSize(parseInt(values[0]), parseInt(values[1]));
					}) //
					.orElseGet(() -> new RecordVideoSize(1024, 800));
		}

		private static boolean headless() {
			return isHeadless() || !isPropertySet(SYS_PROP_PREFIX + "playwright.showbrowser");
		}

	}
}
