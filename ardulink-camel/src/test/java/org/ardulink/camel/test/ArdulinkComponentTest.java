package org.ardulink.camel.test;

import static java.util.UUID.randomUUID;
import static org.apache.camel.ShutdownRunningTask.CompleteAllTasks;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.linkmanager.providers.LinkFactoriesProvider4Test.withRegistered;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ArdulinkComponentTest {

	private static final String IN = "direct:in";

	private static final String MOCK_URI = "ardulink://mock";

	private Link link;

	private CamelContext context;

	@Before
	public void setup() throws Exception {
		context = camelContext(IN, MOCK_URI);
		link = Links.getLink(MOCK_URI);
	}

	@After
	public void tearDown() throws Exception {
		link.close();
		context.stop();
	}

	@Test
	public void canSwitchDigitalPin2On() throws Exception {
		testDigital(digitalPin(2), true);
	}

	@Test
	public void canSwitchDigitalPin2Off() throws Exception {
		testDigital(digitalPin(2), false);
	}

	@Test
	public void canSwitchDigitalPin3() throws Exception {
		testDigital(digitalPin(3), true);
	}

	@Test
	public void canSwitchAnalogPin3() throws Exception {
		testAnalog(analogPin(5), 123);
	}

	@Test
	@Ignore
	public void ignoresNegativeValues() {
		send(alpProtocolMessage(ANALOG_PIN_READ).forPin(analogPin(6).pinNum()).withValue(-1));
		Link mock = getMock(link);
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canEnableAnalogListening() throws Exception {
		send(alpProtocolMessage(START_LISTENING_ANALOG).forPin(analogPin(6).pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(analogPin(6));
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canEnableDigitalListening() throws Exception {
		send(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(digitalPin(7).pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(digitalPin(7));
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canDisableAnalogListening() throws Exception {
		send(alpProtocolMessage(START_LISTENING_ANALOG).forPin(6).withoutValue());
		send(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(6).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(analogPin(6));
		verify(mock).stopListening(analogPin(6));
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canDisableDigitalListening() throws Exception {
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
		context.createProducerTemplate().sendBody(MOCK_URI, message);
	}

	public static class TestLinkFactory implements LinkFactory<TestLinkConfig> {

		private final String name;

		public TestLinkFactory(String name) {
			this.name = name;
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
			return new TestLinkConfig();
		}

	}

	public static class TestLinkConfig implements LinkConfig {
		@Named("a")
		public String a;

		@Named("b")
		public TimeUnit b;
	}

	@Test
	public void canSetLinkParameters() throws Exception {
		String a = "foo";
		String b = "HOURS";
		String name = "factoryName-" + randomUUID();
		LinkFactory<TestLinkConfig> linkFactorySpy = spy(new TestLinkFactory(name));
		withRegistered(linkFactorySpy).execute(() -> {
			context = camelContext("ardulink://" + name + "?a=" + a + "&b=" + b, MOCK_URI);
		});

		TestLinkConfig value = getConfig(linkFactorySpy);
		assertThat(value.a, is(a));
		assertThat(value.b, is(TimeUnit.valueOf(b)));
	}

	private static TestLinkConfig getConfig(LinkFactory<TestLinkConfig> linkFactorySpy) throws Exception {
		ArgumentCaptor<TestLinkConfig> captor = forClass(TestLinkConfig.class);
		verify(linkFactorySpy).newLink(captor.capture());
		return captor.getValue();
	}

}
