package ardulink.ardumailng.camel;

import static ardulink.ardumailng.Commands.switchAnalogPin;
import static ardulink.ardumailng.Commands.switchDigitalPin;
import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.StringContains.containsString;
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

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.convenience.LinkDelegate;
import com.github.pfichtner.ardulink.core.convenience.Links;

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
	public void throwsExceptionIfCommandNotKnown() throws Exception {
		String anyUser = "anyuser";
		message.setHeader("From", anyUser);
		String commandName = "unknown command name";
		message.setBody(commandName);
		producer.setValidFroms(singletonList(anyUser));
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage(containsString("Command " + commandName
				+ " not known"));
		process();
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
		process();

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

	private void process() throws Exception {
		producer.process(exchange());
	}

	private Exchange exchange() {
		Exchange exchange = new DefaultExchange(new DefaultCamelContext());
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
