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
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.CUSTOM_EVENT;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.util.Throwables.propagate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
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
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
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
	@AutoClose
	Link link;
	@AutoClose("stop")
	CamelContext context;

	@BeforeEach
	void setup(@MockUri String mockUri) throws Exception {
		this.mockUri = mockUri;
		this.context = camelContext(in, mockUri);
		this.link = Links.getLink(mockUri);
	}

	@ParameterizedTest
	@CsvSource({ //
			"1,true", //
			"2,false" //
	})
	void canSwitchDigitalPin(int pin, boolean value) throws Exception {
		assertOk(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(value));
		Link mock = getMock(link);
		verify(mock).switchDigitalPin(digitalPin(pin), value);
		verifyNoMoreInteractions(mock);
	}

	@ParameterizedTest
	@CsvSource({ //
			"3,123", //
			"4,456" //
	})
	void canSwitchAnalogPin(int pin, int value) throws Exception {
		assertOk(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value));
		Link mock = getMock(link);
		verify(mock).switchAnalogPin(analogPin(pin), value);
		verifyNoMoreInteractions(mock);
	}

	@Test
	@ExpectedToFail("clarify who should filter it")
	void ignoresNegativeValues() {
		assertOk(alpProtocolMessage(ANALOG_PIN_READ).forPin(5).withValue(-1));
		Link mock = getMock(link);
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canEnableAnalogListening() throws Exception {
		Pin pin = analogPin(6);
		assertOk(alpProtocolMessage(START_LISTENING_ANALOG).forPin(pin.pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(pin);
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canEnableDigitalListening() throws Exception {
		Pin pin = digitalPin(7);
		assertOk(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(pin.pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(pin);
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canDisableAnalogListening() throws Exception {
		Pin pin = analogPin(8);
		assertOk(alpProtocolMessage(START_LISTENING_ANALOG).forPin(pin.pinNum()).withoutValue());
		Link mock = getMock(link);
		reset(mock);
		assertOk(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(pin.pinNum()).withoutValue());
		verify(mock).stopListening(pin);
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canDisableDigitalListening() throws Exception {
		Pin pin = digitalPin(9);
		assertOk(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(pin.pinNum()).withoutValue());
		assertOk(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(pin.pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(pin);
		verify(mock).stopListening(pin);
		verifyNoMoreInteractions(mock);
	}

	@Test
	void throwsAnExceptionIfLinkThrowsException() throws IOException {
		IOException ioe = new IOException("my io exception");
		when(this.link.switchAnalogPin(any(AnalogPin.class), anyInt())).thenThrow(ioe);
		when(this.link.switchDigitalPin(any(DigitalPin.class), anyBoolean())).thenThrow(ioe);

		assertSoftly(s -> {
			s.assertThatRuntimeException()
					.isThrownBy(() -> send(alpProtocolMessage(ANALOG_PIN_READ).forPin(1).withValue(2)))
					.havingRootCause().isSameAs(ioe);
			s.assertThatRuntimeException()
					.isThrownBy(() -> send(alpProtocolMessage(DIGITAL_PIN_READ).forPin(1).withState(true)))
					.havingRootCause().isSameAs(ioe);
		});

	}

	@ParameterizedTest
	@NullAndEmptySource
	void respondWithNokOnEmptyMessage(String message) {
		assertNok(message);
	}

	@Test
	void respondWithNokOnUnknownCommands() {
		assertNok(alpProtocolMessage(CUSTOM_EVENT).forPin(9).withoutValue());
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

	void assertOk(String message) {
		sendAndAssertReceived(message, "OK");
	}

	void assertNok(String message) {
		sendAndAssertReceived(message, "NOK");
	}

	private void sendAndAssertReceived(String message, String rc) {
		assertThat(send(message)).isEqualTo(format("%s=%s", message, rc));
	}

	private Object send(String message) {
		try {
			return context.createProducerTemplate().asyncRequestBody(mockUri, message).get();
		} catch (InterruptedException | ExecutionException e) {
			throw propagate(e);
		}
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
		String aValue = "foo";
		TimeUnit bValue = HOURS;
		String name = "factoryName-" + randomUUID();

		TestLinkConfig config = new TestLinkConfig();
		LinkFactory<TestLinkConfig> linkFactory = new TestLinkFactory(name, newArrayList(config).iterator());
		withRegistered(linkFactory).execute(
				() -> context = camelContext(format("ardulink://%s?a=%s&b=%s", name, aValue, bValue), mockUri));

		assertSoftly(s -> {
			s.assertThat(config.a).isEqualTo(aValue);
			s.assertThat(config.b).isEqualTo(bValue);
		});
	}

}
