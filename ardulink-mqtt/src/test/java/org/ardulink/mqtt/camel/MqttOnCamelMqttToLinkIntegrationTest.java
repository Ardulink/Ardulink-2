package org.ardulink.mqtt.camel;

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
import static org.ardulink.mqtt.camel.ToArdulinkProtocol.toArdulinkProtocol;

import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.mqtt.Config;
import org.ardulink.mqtt.MqttBroker;
import org.ardulink.mqtt.util.AnotherMqttClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MqttOnCamelMqttToLinkIntegrationTest {

	private static final String OUT = "mock:result";

	private static final String TOPIC = "any/topic-"
			+ System.currentTimeMillis();

	private AnotherMqttClient mqttClient;

	private MqttBroker broker;

	private CamelContext context;

	@Before
	public void setup() throws Exception {
		broker = MqttBroker.builder().startBroker();
		mqttClient = AnotherMqttClient.builder().topic(TOPIC).connect();
	}

	@After
	public void tearDown() throws InterruptedException, Exception {
		context.stop();
		mqttClient.close();
		broker.close();
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
		assertNoMessage(getMockEndpoint());
	}

	@Test
	public void canEnableAnalogListening() throws Exception {
		context = camelContext(config().withControlChannelEnabled());
		AnalogPin pin = analogPin(6);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(START_LISTENING_ANALOG)
				.forPin(pin.pinNum()).withoutValue());
		mqttClient.startListenig(pin);
		assertIsSatisfied(out);
	}

	@Test
	public void canEnableDigitalListening() throws Exception {
		context = camelContext(config().withControlChannelEnabled());
		DigitalPin pin = digitalPin(7);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(START_LISTENING_DIGITAL)
				.forPin(pin.pinNum()).withoutValue());
		mqttClient.startListenig(pin);
		assertIsSatisfied(out);
	}

	@Test
	public void canDisableAnalogListening() throws Exception {
		context = camelContext(config().withControlChannelEnabled());
		AnalogPin pin = analogPin(6);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(STOP_LISTENING_ANALOG)
				.forPin(pin.pinNum()).withoutValue());
		mqttClient.stopListenig(pin);
		assertIsSatisfied(out);
	}

	@Test
	public void canDisableDigitalListening() throws Exception {
		context = camelContext(config().withControlChannelEnabled());
		DigitalPin pin = digitalPin(7);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(STOP_LISTENING_DIGITAL)
				.forPin(pin.pinNum()).withoutValue());
		mqttClient.stopListenig(pin);
		assertIsSatisfied(out);
	}

	@Test
	public void doesNotEnableAnalogListening_WhenControlChannelInsNOTenabled()
			throws Exception {
		context = camelContext(config());
		mqttClient.startListenig(analogPin(6));
		assertNoMessage(getMockEndpoint());
	}

	@Test
	public void doesNotEnableDigitalListening_WhenControlChannelInsNOTenabled()
			throws Exception {
		context = camelContext(config());
		mqttClient.startListenig(digitalPin(7));
		assertNoMessage(getMockEndpoint());
	}

	private void testDigital(DigitalPin pin, boolean state) throws Exception {
		context = camelContext(config());
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(DIGITAL_PIN_READ).forPin(
				pin.pinNum()).withState(state));
		mqttClient.switchPin(pin, state);
		assertIsSatisfied(out);
	}

	private void testAnalog(AnalogPin pin, int value) throws Exception {
		context = camelContext(config());
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(ANALOG_PIN_READ).forPin(
				pin.pinNum()).withValue(value));
		mqttClient.switchPin(pin, value);
		assertIsSatisfied(out);
	}

	private static void assertNoMessage(MockEndpoint out)
			throws InterruptedException {
		out.expectedMessageCount(0);
		assertIsSatisfied(out);
	}

	private static void assertIsSatisfied(MockEndpoint out)
			throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(100);
		out.assertIsSatisfied();
	}

	private Config config() {
		return Config.withTopic(TOPIC);
	}

	private CamelContext camelContext(final Config config) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(mqtt())
						.transform(body().convertToString())
						.process(
								toArdulinkProtocol(config).topicFrom(
										header("CamelMQTTSubscribeTopic")))
						.to(OUT).shutdownRunningTask(CompleteAllTasks);
			}
		});
		context.start();
		return context;
	}

	private MockEndpoint getMockEndpoint() {
		return context.getEndpoint(OUT, MockEndpoint.class);
	}

	private String mqtt() {
		return "mqtt:localhost?" + "connectAttemptsMax=1"
				+ "&reconnectAttemptsMax=0" + "&subscribeTopicNames=" + TOPIC
				+ "/#";
	}

}
