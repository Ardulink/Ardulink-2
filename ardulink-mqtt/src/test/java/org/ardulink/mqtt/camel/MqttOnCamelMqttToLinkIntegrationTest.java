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
import org.ardulink.mqtt.MqttBroker;
import org.ardulink.mqtt.util.AnotherMqttClient;
import org.ardulink.util.URIs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MqttOnCamelMqttToLinkIntegrationTest {

	private static final String mockURI = "ardulink://mock";

	private Link link;

	private static final String TOPIC = "any/topic-"
			+ System.currentTimeMillis();

	private AnotherMqttClient mqttClient;

	private Server broker;

	private CamelContext context;

	@Before
	public void setup() throws Exception {
		link = Links.getLink(URIs.newURI(mockURI));
		broker = MqttBroker.builder().startBroker();
		mqttClient = AnotherMqttClient.builder().topic(TOPIC).connect();
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
		context = camelContext(config());
		broker.stopServer();
		context.stop();
		Link mock = getMock(link);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
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
	@Ignore
	public void ignoresNegativeValues() throws Exception {
		context = camelContext(config());
		mqttClient.switchPin(analogPin(6), -1);
		haltCamel();
		Link mock = getMock(link);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canEnableAnalogListening() throws Exception {
		context = camelContext(config().withControlChannelEnabled());
		mqttClient.startListenig(analogPin(6));
		haltCamel();
		Link mock = getMock(link);
		verify(mock).startListening(analogPin(6));
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canEnableDigitalListening() throws Exception {
		context = camelContext(config().withControlChannelEnabled());
		mqttClient.startListenig(digitalPin(7));
		haltCamel();
		Link mock = getMock(link);
		verify(mock).startListening(digitalPin(7));
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canDisableAnalogListening() throws Exception {
		context = camelContext(config().withControlChannelEnabled());
		mqttClient.stopListenig(analogPin(6));
		haltCamel();
		Link mock = getMock(link);
		verify(mock).stopListening(analogPin(6));
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canDisableDigitalListening() throws Exception {
		context = camelContext(config().withControlChannelEnabled());
		mqttClient.stopListenig(digitalPin(7));
		haltCamel();
		Link mock = getMock(link);
		verify(mock).stopListening(digitalPin(7));
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void doesNotEnableAnalogListening_WhenControlChannelInsNOTenabled()
			throws Exception {
		context = camelContext(config());
		mqttClient.startListenig(analogPin(6));
		haltCamel();
		Link mock = getMock(link);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void doesNotEnableDigitalListening_WhenControlChannelInsNOTenabled()
			throws Exception {
		context = camelContext(config());
		mqttClient.startListenig(digitalPin(7));
		haltCamel();
		Link mock = getMock(link);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private void testDigital(DigitalPin pin, boolean state) throws Exception {
		context = camelContext(config());
		mqttClient.switchPin(pin, state);
		haltCamel();
		Link mock = getMock(link);
		verify(mock).switchDigitalPin(pin, state);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private void testAnalog(AnalogPin pin, int value) throws Exception {
		context = camelContext(config());
		mqttClient.switchPin(pin, value);
		haltCamel();
		Link mock = getMock(link);
		verify(mock).switchAnalogPin(pin, value);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private Config config() {
		return Config.withTopic(TOPIC);
	}

	private CamelContext haltCamel() throws InterruptedException, Exception {
		TimeUnit.MILLISECONDS.sleep(500);
		context.stop();
		return context;
	}

	private CamelContext camelContext(final Config config) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				ToArdulinkProtocol toArdulinkProtocol = new ToArdulinkProtocol(
						config).headerNameForTopic("CamelMQTTSubscribeTopic");
				from(mqtt()).transform(body().convertToString())
						.process(toArdulinkProtocol).to(mockURI)
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
	}

}
