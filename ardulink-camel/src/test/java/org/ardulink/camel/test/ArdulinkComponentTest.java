package org.ardulink.camel.test;

import static java.util.UUID.randomUUID;
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
import static org.ardulink.testsupport.mock.TestSupport.uniqueMockUri;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Link;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ArdulinkComponentTest {

	String in = "direct:in";
	String mockUri = uniqueMockUri();

	Link link;
	CamelContext context;

	@BeforeEach
	void setup() throws Exception {
		context = camelContext(in, mockUri);
		link = Links.getLink(mockUri);
	}

	@AfterEach
	void tearDown() throws Exception {
		link.close();
		context.stop();
	}

	@Test
	void canSwitchDigitalPin2On() throws Exception {
		testDigital(digitalPin(2), true);
	}

	@Test
	void canSwitchDigitalPin2Off() throws Exception {
		testDigital(digitalPin(2), false);
	}

	@Test
	void canSwitchDigitalPin3() throws Exception {
		testDigital(digitalPin(3), true);
	}

	@Test
	void canSwitchAnalogPin3() throws Exception {
		testAnalog(analogPin(5), 123);
	}

	@Test
	@Disabled("clarify who should filter it")
	void ignoresNegativeValues() {
		send(alpProtocolMessage(ANALOG_PIN_READ).forPin(analogPin(6).pinNum()).withValue(-1));
		Link mock = getMock(link);
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canEnableAnalogListening() throws Exception {
		send(alpProtocolMessage(START_LISTENING_ANALOG).forPin(analogPin(6).pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(analogPin(6));
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canEnableDigitalListening() throws Exception {
		send(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(digitalPin(7).pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(digitalPin(7));
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canDisableAnalogListening() throws Exception {
		send(alpProtocolMessage(START_LISTENING_ANALOG).forPin(6).withoutValue());
		send(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(6).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(analogPin(6));
		verify(mock).stopListening(analogPin(6));
		verifyNoMoreInteractions(mock);
	}

	@Test
	void canDisableDigitalListening() throws Exception {
		send(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(7).withoutValue());
		send(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(7).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(digitalPin(7));
		verify(mock).stopListening(digitalPin(7));
		verifyNoMoreInteractions(mock);
	}

	private void testDigital(DigitalPin pin, boolean state) throws Exception {
		send(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin.pinNum()).withState(state));
		Link mock = getMock(link);
		verify(mock).switchDigitalPin(pin, state);
		verifyNoMoreInteractions(mock);
	}

	private void testAnalog(AnalogPin pin, int value) throws Exception {
		send(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin.pinNum()).withValue(value));
		Link mock = getMock(link);
		verify(mock).switchAnalogPin(pin, value);
		verifyNoMoreInteractions(mock);
	}

	private CamelContext camelContext(String in, String to) throws Exception {
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

	private void send(String message) {
		context.createProducerTemplate().sendBody(mockUri, message);
	}

	private static class TestLinkFactory implements LinkFactory<TestLinkConfig> {

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
		String b = "HOURS";
		String name = "factoryName-" + randomUUID();

		TestLinkConfig config = new TestLinkConfig();
		LinkFactory<TestLinkConfig> linkFactory = new TestLinkFactory(name, newArrayList(config).iterator());
		withRegistered(linkFactory)
				.execute(() -> context = camelContext(String.format("ardulink://%s?a=%s&b=%s", name, a, b), mockUri));

		assertThat(config.a).isEqualTo(a);
		assertThat(config.b).isEqualTo(TimeUnit.valueOf(b));
	}

}
