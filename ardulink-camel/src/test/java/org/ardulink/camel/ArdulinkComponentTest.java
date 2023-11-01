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

package org.ardulink.camel;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.apache.camel.ShutdownRunningTask.CompleteAllTasks;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.linkmanager.providers.DynamicLinkFactoriesProvider.withRegistered;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.testsupport.mock.junit5.MockUri;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ArdulinkComponentTest {

	String in = "direct:in";

	String mockUri;
	Link link;
	CamelContext context;

	@BeforeEach
	void setup(@MockUri String mockUri) throws Exception {
		this.mockUri = mockUri;
		this.context = camelContext(in, mockUri);
		this.link = Links.getLink(mockUri);
	}

	@AfterEach
	void tearDown() throws Exception {
		this.link.close();
		this.context.stop();
	}

	@Test
	void canSwitchDigitalPin() throws Exception {
		assertAll( //
				() -> testDigital(digitalPin(1), true), //
				() -> testDigital(digitalPin(2), false) //
		);
	}

	@Test
	void canSwitchAnalogPin() throws Exception {
		assertAll( //
				() -> testAnalog(analogPin(3), 123), //
				() -> testAnalog(analogPin(4), 456) //
		);
	}

	@Test
	@ExpectedToFail("clarify who should filter it")
	void ignoresNegativeValues() {
		Pin pin = analogPin(5);
		send(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin.pinNum()).withValue(-1));
		Link mock = getMock(link);
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canEnableAnalogListening() throws Exception {
		Pin pin = analogPin(6);
		send(alpProtocolMessage(START_LISTENING_ANALOG).forPin(pin.pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(pin);
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canEnableDigitalListening() throws Exception {
		Pin pin = digitalPin(7);
		send(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(pin.pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(pin);
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canDisableAnalogListening() throws Exception {
		Pin pin = analogPin(8);
		send(alpProtocolMessage(START_LISTENING_ANALOG).forPin(pin.pinNum()).withoutValue());
		send(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(pin.pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(pin);
		verify(mock).stopListening(pin);
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canDisableDigitalListening() throws Exception {
		Pin pin = digitalPin(9);
		send(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(pin.pinNum()).withoutValue());
		send(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(pin.pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(pin);
		verify(mock).stopListening(pin);
		verifyNoMoreInteractions(mock);
	}

	void testDigital(DigitalPin pin, boolean state) throws Exception {
		send(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin.pinNum()).withState(state));
		Link mock = getMock(link);
		verify(mock).switchDigitalPin(pin, state);
		verifyNoMoreInteractions(mock);
	}

	void testAnalog(AnalogPin pin, int value) throws Exception {
		send(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin.pinNum()).withValue(value));
		Link mock = getMock(link);
		verify(mock).switchAnalogPin(pin, value);
		verifyNoMoreInteractions(mock);
	}

	CamelContext camelContext(String in, String to) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(in).to(to).shutdownRunningTask(CompleteAllTasks);
			}
		});
		context.start();
		return context;
	}

	void send(String message) {
		context.createProducerTemplate().sendBody(mockUri, message);
	}

	static class TestLinkFactory implements LinkFactory<TestLinkConfig> {

		private final String name;
		private final Iterator<TestLinkConfig> configProvider;

		public TestLinkFactory(String name, Iterator<TestLinkConfig> configProvider) {
			this.name = name;
			this.configProvider = configProvider;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Link newLink(TestLinkConfig config) {
			return mock(Link.class);
		}

		@Override
		public TestLinkConfig newLinkConfig() {
			return configProvider.next();
		}

	}

	public static class TestLinkConfig implements LinkConfig {
		@Named("a")
		public String a;

		@Named("b")
		public TimeUnit b;
	}

	@Test
	void canSetLinkParameters() throws Throwable {
		String a = "foo";
		TimeUnit b = HOURS;
		String name = "factoryName-" + randomUUID();

		TestLinkConfig config = new TestLinkConfig();
		LinkFactory<TestLinkConfig> linkFactory = new TestLinkFactory(name, newArrayList(config).iterator());
		withRegistered(linkFactory)
				.execute(() -> context = camelContext(format("ardulink://%s?a=%s&b=%s", name, a, b), mockUri));

		assertThat(config.a).isEqualTo(a);
		assertThat(config.b).isEqualTo(b);
	}

}
