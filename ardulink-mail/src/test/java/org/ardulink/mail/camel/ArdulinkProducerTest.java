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

package org.ardulink.mail.camel;

import static java.util.Collections.singletonList;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_SWITCH;
import static org.ardulink.mail.camel.FromValidator.validateFromHeader;
import static org.ardulink.mail.camel.ScenarioProcessor.processScenario;
import static org.ardulink.mail.test.CauseMatcher.exceptionWithMessage;
import static org.ardulink.util.Lists.newArrayList;
import static org.ardulink.util.Throwables.propagate;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.rules.ExpectedException.none;

import java.util.Collections;

import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultMessage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkProducerTest {

	private void setup(FromValidator fromValidator, ScenarioProcessor scenarioProcessor) {
		try {
			CamelContext context = new DefaultCamelContext();
			context.addRoutes(new RouteBuilder() {
				@Override
				public void configure() {
					from(IN).process(fromValidator).process(scenarioProcessor).split(body()).to(OUT);
				}

			});
			context.start();
			ArdulinkProducerTest.this.context = context;
			ArdulinkProducerTest.this.message = new DefaultMessage(context);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	@Rule
	public ExpectedException expectedException = none();

	private static final String IN = "direct:in";
	private static final String OUT = "mock:result";

	private Message message;
	private CamelContext context;

	public void tearDown() throws Exception {
		context.stop();
	}

	@Test
	public void doesNotAcceptMessagesWithUnknownFromAddresses() throws Exception {
		setup(validateFromHeader(Collections.<String>emptyList()), processScenario());
		setFrom("anyuser");

		MockEndpoint mockEndpoint = getMockEndpoint();
		mockEndpoint.expectedMessageCount(0);

		expectedException.expect(RuntimeException.class);
		expectedException.expectCause(
				exceptionWithMessage(IllegalStateException.class, containsString("not a valid from address")));
		process();
		mockEndpoint.assertIsSatisfied();
	}

	@Test
	public void doesNotAcceptMeesagesWithEmptyBody() throws Exception {
		setup(validateFromHeader(singletonList("aValidUser")), processScenario());
		setFrom("aValidUser");

		MockEndpoint mockEndpoint = getMockEndpoint();
		mockEndpoint.expectedMessageCount(0);

		expectedException.expect(RuntimeException.class);
		expectedException
				.expectCause(exceptionWithMessage(IllegalStateException.class, containsString("body is empty")));
		process();
		mockEndpoint.assertIsSatisfied();
	}

	@Test
	public void doesNotAcceptMessagesWithNullOrEmptyFromAddress() throws Exception {
		setup(validateFromHeader(singletonList("anyuser")), processScenario());

		MockEndpoint mockEndpoint = getMockEndpoint();
		mockEndpoint.expectedMessageCount(0);

		expectedException.expect(RuntimeException.class);
		expectedException.expectCause(exceptionWithMessage(IllegalStateException.class, containsString("No from")));
		process();
		mockEndpoint.assertIsSatisfied();
	}

	@Test
	public void doesNotAcceptMessagesWhereScenarioNameIsNotKnown() throws Exception {
		String anyUser = "anyuser";
		setup(validateFromHeader(singletonList(anyUser)), processScenario());

		setFrom(anyUser);
		setBody("unknown command name");

		MockEndpoint mockEndpoint = getMockEndpoint();
		mockEndpoint.expectedMessageCount(0);

		expectedException.expect(RuntimeException.class);
		expectedException.expectCause(exceptionWithMessage(IllegalStateException.class, containsString("not known")));

		process();
		mockEndpoint.assertIsSatisfied();
	}

	@Test
	public void doesProcessDigitalPinMessages() throws Exception {
		String switchDigital7 = alpProtocolMessage(POWER_PIN_SWITCH).forPin(7).withState(true);

		String anyUser = "anyuser";
		String commandName = "scenario 1";

		setup(validateFromHeader(singletonList(anyUser)),
				processScenario().withCommand(commandName, singletonList(switchDigital7)));

		setFrom(anyUser);
		setBody(commandName);

		MockEndpoint mockEndpoint = getMockEndpoint();
		mockEndpoint.expectedBodiesReceived(switchDigital7);

		process();
		mockEndpoint.assertIsSatisfied();
	}

	@Test
	public void doesProcessDigitalAndAnalogPinMessages() throws Exception {
		String digital7 = alpProtocolMessage(POWER_PIN_SWITCH).forPin(7).withState(true);
		String analog8 = alpProtocolMessage(POWER_PIN_INTENSITY).forPin(8).withValue(123);

		String anyUser = "anyuser";
		String commandName = "scenario 2";
		setup(validateFromHeader(singletonList(anyUser)),
				processScenario().withCommand(commandName, newArrayList(digital7, analog8)));

		setFrom(anyUser);
		setBody(commandName);

		MockEndpoint mockEndpoint = getMockEndpoint();

		mockEndpoint.expectedBodiesReceived(digital7, analog8);
		process();
		mockEndpoint.assertIsSatisfied();
	}

	private void setBody(String body) {
		message.setBody(body);
	}

	private void setFrom(String from) {
		message.setHeader("From", from);
	}

	private void process() throws Exception {
		context.createProducerTemplate().sendBodyAndHeaders(IN, message.getBody(), message.getHeaders());
	}

	private MockEndpoint getMockEndpoint() {
		return context.getEndpoint(OUT, MockEndpoint.class);
	}

}