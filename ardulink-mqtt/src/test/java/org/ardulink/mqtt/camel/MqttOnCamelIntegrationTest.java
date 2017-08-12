package org.ardulink.mqtt.camel;

import static org.apache.camel.ShutdownRunningTask.CompleteAllTasks;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import io.moquette.server.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Link;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;
import org.ardulink.mqtt.Config;
import org.ardulink.mqtt.Config.DefaultConfig;
import org.ardulink.mqtt.MqttBroker;
import org.ardulink.mqtt.util.AnotherMqttClient;
import org.ardulink.util.Optional;
import org.ardulink.util.URIs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MqttOnCamelIntegrationTest {

	private static final String mockURI = "ardulink://mock";

	private Link link;

	private static final String TOPIC = "some/test/topic-"
			+ System.currentTimeMillis();

	private AnotherMqttClient mqttClient;

	private Server broker;

	@Before
	public void setup() {
		link = Links.getLink(URIs.newURI(mockURI));
		broker = MqttBroker.builder().startBroker();
		mqttClient = AnotherMqttClient.builder().topic(TOPIC).controlTopic()
				.connect();
	}

	@After
	public void tearDown() throws IOException {
		mqttClient.close();
		broker.stopServer();
		link.close();
	}

	@Test
	@Ignore
	public void routeFailsIfBrokerIsNotRunning() throws Exception {
		broker.stopServer();
		createContext().stop();
	}

	@Test
	public void canSwitchDigitalPin2OnViaBroker() throws Exception {
		testDigital(digitalPin(2), true);
	}

	@Test
	public void canSwitchDigitalPin2OffViaBroker() throws Exception {
		testDigital(digitalPin(2), false);
	}

	@Test
	public void canSwitchDigitalPin3ViaBroker() throws Exception {
		testDigital(digitalPin(3), true);
	}

	@Test
	public void canSwitchAnalogPin3ViaBroker() throws Exception {
		testAnalog(analogPin(5), 123);
	}

	@Test
	public void ignoresNegativeValues() throws Exception {
		AnalogPin pin = analogPin(6);
		CamelContext context = createContext();

		mqttClient.switchPin(pin, -1);
		gracefulShutdown(context);

		Link mock = getMock(link);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canEnableAnalogListening() throws Exception {
		AnalogPin pin = analogPin(6);
		CamelContext context = createContext();

		mqttClient.startListenig(pin);
		gracefulShutdown(context);

		Link mock = getMock(link);
		verify(mock).startListening(pin);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canEnableDigitalListening() throws Exception {
		DigitalPin pin = digitalPin(7);
		CamelContext context = createContext();

		mqttClient.startListenig(pin);
		gracefulShutdown(context);

		Link mock = getMock(link);
		verify(mock).startListening(pin);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	// TODO ApacheCamel Throttler, Aggregator

	private void testDigital(DigitalPin pin, boolean state) throws Exception {
		CamelContext context = createContext();

		mqttClient.switchPin(pin, state);
		gracefulShutdown(context);

		Link mock = getMock(link);
		verify(mock).switchDigitalPin(pin, state);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private void testAnalog(AnalogPin pin, int value) throws Exception {
		CamelContext context = createContext();

		mqttClient.switchPin(pin, value);
		gracefulShutdown(context);

		Link mock = getMock(link);
		verify(mock).switchAnalogPin(pin, value);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	public interface MessageCreator {
		Optional<String> createMessage(String topic, String value);
	}

	private CamelContext gracefulShutdown(CamelContext context)
			throws InterruptedException, Exception {
		TimeUnit.MILLISECONDS.sleep(500);
		context.stop();
		return context;
	}

	private CamelContext createContext() throws Exception {
		CamelContext context = new DefaultCamelContext();
		final Config config = DefaultConfig.withTopic(
				TOPIC.endsWith("/") ? TOPIC : TOPIC + '/')
				.withControlChannelEnabled();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(mqtt())
						.transform(body().convertToString())
						.setHeader("topic")
						.expression(
								simple("${in.header.CamelMQTTSubscribeTopic}"))
						.process(new ToSimpleProtocol(config)).to(mockURI)
						.shutdownRunningTask(CompleteAllTasks);
			}
		});
		context.start();
		return context;
	}

	private Link getMock(Link link) {
		return ((LinkDelegate) link).getDelegate();
	}

	private String mqtt() {
		return "mqtt:localhost?connectAttemptsMax=1"
				+ "&reconnectAttemptsMax=0" + "&subscribeTopicNames=" + TOPIC
				+ "/#";
		// publishTopicName
	}

}
