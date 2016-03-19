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

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static java.util.Collections.singletonList;
import static org.apache.camel.ExchangePattern.InOut;
import static org.ardulink.mail.Commands.switchAnalogPin;
import static org.ardulink.mail.Commands.switchDigitalPin;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.URI;
import java.util.Collections;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkProducerTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private ArdulinkProducer producer = new ArdulinkProducer(
			mock(Endpoint.class), "mock", null);

	private Message message = new DefaultMessage();

	private final String uri = "ardulink://mock";
	private Link link = getLink(uri);

	@After
	public void tearDown() throws Exception {
		link.close();
		producer.stop();
	}

	@Test
	public void doesNotAcceptMessagesWithUnknownFromAddresses()
			throws Exception {
		message.setHeader("From", "userA");
		expectedException.expect(IllegalStateException.class);
		expectedException
				.expectMessage(containsString("user userA not a valid from"));
		process();
	}

	@Test
	public void doesNotAcceptMeesagesWithEmptyBody() throws Exception {
		message.setHeader("From", "aValidUser");
		producer.setValidFroms(singletonList("aValidUser"));
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage(containsString("Body not a String"));
		process();
	}

	@Test
	public void doesNotAcceptMessagesWithNullOrEmptyFromAddress()
			throws Exception {
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage(containsString("No from"));
		process();
	}

	@Test
	public void returnsNullIfCommandIsNotKnown() throws Exception {
		String anyUser = "anyuser";
		message.setHeader("From", anyUser);
		String commandName = "unknown command name";
		message.setBody(commandName);
		producer.setValidFroms(singletonList(anyUser));
		Exchange exchange = process();

		verifyNoMoreInteractions(getMock());
		assertThat(exchange.getOut().getBody(), nullValue());
	}

	@Test
	public void doesProcessDigitalPinMessages() throws Exception {
		String anyUser = "anyuser";
		message.setHeader("From", anyUser);
		String commandName = "scenario 1";
		message.setBody(commandName);
		producer.setValidFroms(Collections.singletonList(anyUser));
		producer.setCommands(commandName,
				singletonList(switchDigitalPin(7, true)));
		Exchange exchange = process();
		assertThat(exchange.getOut().getBody(), is(not(nullValue())));

		Link mock = getMock();
		verify(mock).switchDigitalPin(digitalPin(7), true);
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void doesProcessDigitalAndAnalogPinMessages() throws Exception {
		String anyUser = "anyuser";
		message.setHeader("From", anyUser);
		String commandName = "scenario 2";
		message.setBody(commandName);
		producer.setValidFroms(Collections.singletonList(anyUser));
		producer.setCommands(commandName, switchDigitalPin(7, true),
				switchAnalogPin(4, 123));
		process();

		Link mock = getMock();
		verify(mock).switchDigitalPin(digitalPin(7), true);
		verify(mock).switchAnalogPin(analogPin(4), 123);
		verifyNoMoreInteractions(mock);
	}

	private Exchange process() throws Exception {
		Exchange exchange = exchange();
		producer.process(exchange);
		return exchange;
	}

	private Exchange exchange() {
		Exchange exchange = new DefaultExchange(new DefaultCamelContext());
		exchange.setPattern(InOut);
		exchange.setIn(message);
		return exchange;
	}

	private Link getMock() {
		return ((LinkDelegate) link).getDelegate();
	}

	private Link getLink(String uri) {
		try {
			return Links.getLink(new URI(uri));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
