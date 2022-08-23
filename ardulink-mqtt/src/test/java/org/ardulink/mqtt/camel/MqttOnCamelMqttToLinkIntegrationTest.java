package org.ardulink.mqtt.camel;

import static java.util.stream.Collectors.toList;
import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.util.ServerSockets.freePort;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.util.function.ThrowingConsumer;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.mqtt.MqttBroker;
import org.ardulink.mqtt.MqttCamelRouteBuilder;
import org.ardulink.mqtt.MqttCamelRouteBuilder.MqttConnectionProperties;
import org.ardulink.mqtt.Topics;
import org.ardulink.mqtt.util.AnotherMqttClient;
import org.ardulink.util.Throwables;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MqttOnCamelMqttToLinkIntegrationTest {

	private static final String MOCK = "mock:result";

	private static final String TOPIC = "any/topic-" + System.currentTimeMillis();

	private final AnotherMqttClient mqttClient;

	private final MqttBroker broker;

	private final Topics topics;

	private CamelContext context;

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { sameTopic(), separateTopics() });
	}

	private static Object[] sameTopic() {
		return new Object[] { "sameTopic", AnotherMqttClient.builder().topic(TOPIC), Topics.basedOn(TOPIC) };
	}

	private static Object[] separateTopics() {
		return new Object[] { "separateTopics", AnotherMqttClient.builder().topic(TOPIC).appendValueSet(true),
				Topics.withSeparateReadWriteTopics(TOPIC) };
	}

	public MqttOnCamelMqttToLinkIntegrationTest(String description, AnotherMqttClient.Builder mqttClientBuilder,
			Topics topics) {
		this.broker = MqttBroker.builder().port(freePort()).startBroker();
		this.mqttClient = mqttClientBuilder.host(brokerHost()).port(brokerPort()).connect();
		this.topics = topics;
	}

	private String brokerHost() {
		return this.broker.getHost();
	}

	private int brokerPort() {
		return this.broker.getPort();
	}

	@After
	public void tearDown() throws Exception {
		mqttClient.close();
		context.stop();
		broker.close();
	}

	@Test
	public void canSwitchPins() throws Exception {
		testAnalog(analogPin(2), 123);
		testAnalog(analogPin(2), 245);
		testDigital(digitalPin(3), false);
		testDigital(digitalPin(3), true);
	}

	@Test
	public void canEnableAnalogListening() throws Exception {
		context = camelContext(topics.withControlChannelEnabled());
		int anyPinNumber = anyPinNumber();
		mqttClient.startListenig(analogPin(anyPinNumber));
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(START_LISTENING_ANALOG).forPin(anyPinNumber).withoutValue());
		out.assertIsSatisfied();
	}

	@Test
	public void canEnableDigitalListening() throws Exception {
		context = camelContext(topics.withControlChannelEnabled());
		int anyPinNumber = anyPinNumber();
		mqttClient.startListenig(digitalPin(anyPinNumber));
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(anyPinNumber).withoutValue());
		out.assertIsSatisfied();
	}

	@Test
	public void canDisableAnalogListening() throws Exception {
		context = camelContext(topics.withControlChannelEnabled());
		int anyPinNumber = anyPinNumber();
		mqttClient.stopListenig(analogPin(anyPinNumber));
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(anyPinNumber).withoutValue());
		out.assertIsSatisfied();
	}

	@Test
	public void canDisableDigitalListening() throws Exception {
		context = camelContext(topics.withControlChannelEnabled());
		int anyPinNumber = anyPinNumber();
		mqttClient.stopListenig(digitalPin(anyPinNumber));
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(anyPinNumber).withoutValue());
		out.assertIsSatisfied();
	}

	@Test
	public void doesNotEnableAnalogListening_WhenControlChannelInsNOTenabled() throws Exception {
		context = camelContext(topics);
		mqttClient.startListenig(analogPin(anyPinNumber()));
		assertNoMessage(getMockEndpoint());
	}

	@Test
	public void doesNotEnableDigitalListening_WhenControlChannelInsNOTenabled() throws Exception {
		context = camelContext(topics);
		mqttClient.startListenig(digitalPin(anyPinNumber()));
		assertNoMessage(getMockEndpoint());
	}

	private static int anyPinNumber() {
		return 6;
	}

	private void testDigital(DigitalPin pin, boolean state) throws Exception {
		context = camelContext(topics);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin.pinNum()).withState(state));
		mqttClient.switchPin(pin, state);
		out.assertIsSatisfied();
	}

	private void testAnalog(AnalogPin pin, int value) throws Exception {
		context = camelContext(topics);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin.pinNum()).withValue(value));
		mqttClient.switchPin(pin, value);
		out.assertIsSatisfied();
	}

	private static void assertNoMessage(MockEndpoint out) throws InterruptedException {
		out.expectedMessageCount(0);
		out.assertIsSatisfied();
	}

	private CamelContext camelContext(Topics topics) throws Exception {
		ModelCamelContext context = new DefaultCamelContext();
		MqttConnectionProperties mqtt = new MqttConnectionProperties().name("foo").brokerHost(brokerHost())
				.brokerPort(brokerPort());
		new MqttCamelRouteBuilder(context, topics).fromSomethingToMqtt(MOCK, mqtt).andReverse();
		adviceAll(context, d -> d.getInput().getEndpointUri().equals(MOCK), a -> a.replaceFromWith("direct:noop"));
		// CamelContext#start is async so it does not guarantee that routes are ready,
		// so we call #startRouteDefinitions
		context.startRouteDefinitions();
		context.start();
		return context;
	}

	private static void adviceAll(ModelCamelContext context, Predicate<RouteDefinition> predicate,
			ThrowingConsumer<AdviceWithRouteBuilder, Exception> throwingConsumer) throws Exception {
		List<String> routeIds = context.getRouteDefinitions().stream().filter(predicate)
				.map(RouteDefinition::getRouteId).collect(toList());
		routeIds.forEach(d -> replaceFromWith(context, d, throwingConsumer));
	}

	private static void replaceFromWith(CamelContext context, String routeId,
			ThrowingConsumer<AdviceWithRouteBuilder, Exception> throwingConsumer) {
		try {
			adviceWith(context, routeId, throwingConsumer);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	private MockEndpoint getMockEndpoint() {
		return context.getEndpoint(MOCK, MockEndpoint.class);
	}

}
