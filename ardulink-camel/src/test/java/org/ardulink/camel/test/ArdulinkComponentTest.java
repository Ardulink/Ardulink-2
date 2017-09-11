package org.ardulink.camel.test;

import static org.apache.camel.ShutdownRunningTask.CompleteAllTasks;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.util.Iterables.getFirst;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.camel.ArdulinkEndpoint;
import org.ardulink.core.Link;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;
import org.ardulink.util.URIs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ArdulinkComponentTest {

	private static final String IN = "direct:in";

	private static final String MOCK_URI = "ardulink://mock";

	private Link link;

	private CamelContext context;

	@Before
	public void setup() throws Exception {
		context = camelContext(IN, MOCK_URI);
		link = Links.getLink(URIs.newURI(MOCK_URI));
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
	public void ignoresNegativeValues() throws Exception {
		send(alpProtocolMessage(ANALOG_PIN_READ).forPin(analogPin(6).pinNum())
				.withValue(-1));
		Link mock = getMock(link);
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canEnableAnalogListening() throws Exception {
		send(alpProtocolMessage(START_LISTENING_ANALOG).forPin(
				analogPin(6).pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(analogPin(6));
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canEnableDigitalListening() throws Exception {
		send(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(
				digitalPin(7).pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).startListening(digitalPin(7));
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canDisableAnalogListening() throws Exception {
		send(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(
				analogPin(6).pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).stopListening(analogPin(6));
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canDisableDigitalListening() throws Exception {
		send(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(
				digitalPin(7).pinNum()).withoutValue());
		Link mock = getMock(link);
		verify(mock).stopListening(digitalPin(7));
		verifyNoMoreInteractions(mock);
	}

	private void testDigital(DigitalPin pin, boolean state) throws Exception {
		send(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin.pinNum())
				.withState(state));
		Link mock = getMock(link);
		verify(mock).switchDigitalPin(pin, state);
		verifyNoMoreInteractions(mock);
	}

	private void testAnalog(AnalogPin pin, int value) throws Exception {
		send(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin.pinNum())
				.withValue(value));
		Link mock = getMock(link);
		verify(mock).switchAnalogPin(pin, value);
		verifyNoMoreInteractions(mock);
	}

	private CamelContext camelContext(final String in, final String to)
			throws Exception {
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

	private static Link getMock(Link link) {
		return extractDelegated(link);
	}

	private static Link extractDelegated(Link link) {
		return ((LinkDelegate) link).getDelegate();
	}

	private void send(String message) {
		context.createProducerTemplate().sendBody(MOCK_URI, message);
	}

	@Test
	public void canSetLinkParameters() throws Exception {
		String a = "foo";
		String b = "HOURS";
		context = camelContext("ardulink://testlink?a=" + a + "&b=" + b,
				MOCK_URI);

		Route route = getFirst(context.getRoutes()).getOrThrow(
				"Context %s has no routes", context);
		ArdulinkEndpoint endpoint = (ArdulinkEndpoint) route.getEndpoint();
		TestLink link = (TestLink) extractDelegated(endpoint.getLink());
		TestLinkConfig config = link.getConfig();

		assertThat(config.getA(), is(a));
		assertThat(config.getB(), is(TimeUnit.valueOf(b)));
	}

}
