package org.ardulink.mqtt.camel;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.util.ServerSockets.freePort;

import java.util.Arrays;
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
import org.ardulink.mqtt.util.AnotherMqttClient.Builder;
import org.ardulink.util.Throwables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class MqttOnCamelMqttToLinkIntegrationTest {

	private static class TestConfig {

		private final String name;
		private final Builder mqttClientBuilder;
		private final Topics topics;

		public TestConfig(String name, Builder mqttClientBuilder, Topics topics) {
			this.name = name;
			this.mqttClientBuilder = mqttClientBuilder;
			this.topics = topics;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	private static final String MOCK = "mock:result";

	private static final String TOPIC = "any/topic-" + System.currentTimeMillis();

	private MqttBroker broker;
	private AnotherMqttClient mqttClient;
	private Topics topics;

	private CamelContext context;

	private static List<TestConfig> data() {
		return Arrays.asList(
				new TestConfig("sameTopic", AnotherMqttClient.builder().topic(TOPIC), Topics.basedOn(TOPIC)), //
				new TestConfig("separateTopics", AnotherMqttClient.builder().topic(TOPIC).appendValueSet(true),
						Topics.withSeparateReadWriteTopics(TOPIC)) //
		);
	}

	public void init(TestConfig config) {
		this.broker = MqttBroker.builder().port(freePort()).startBroker();
		this.mqttClient = config.mqttClientBuilder.host(brokerHost()).port(brokerPort()).connect();
		this.topics = config.topics;
	}

	private String brokerHost() {
		return this.broker.getHost();
	}

	private int brokerPort() {
		return this.broker.getPort();
	}

	@AfterEach
	void tearDown() throws Exception {
		mqttClient.close();
		ofNullable(context).ifPresent(CamelContext::stop);
		broker.close();
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canSwitchPins(TestConfig config) throws Exception {
		init(config);
		testAnalog(analogPin(2), 123);
		testAnalog(analogPin(2), 245);
		testDigital(digitalPin(3), false);
		testDigital(digitalPin(3), true);
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canEnableAnalogListening(TestConfig config) throws Exception {
		init(config);
		context = camelContext(topics.withControlChannelEnabled());
		int anyPinNumber = anyPinNumber();
		mqttClient.startListenig(analogPin(anyPinNumber));
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(START_LISTENING_ANALOG).forPin(anyPinNumber).withoutValue());
		out.assertIsSatisfied();
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canEnableDigitalListening(TestConfig config) throws Exception {
		init(config);
		context = camelContext(topics.withControlChannelEnabled());
		int anyPinNumber = anyPinNumber();
		mqttClient.startListenig(digitalPin(anyPinNumber));
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(anyPinNumber).withoutValue());
		out.assertIsSatisfied();
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canDisableAnalogListening(TestConfig config) throws Exception {
		init(config);
		context = camelContext(topics.withControlChannelEnabled());
		int anyPinNumber = anyPinNumber();
		mqttClient.stopListenig(analogPin(anyPinNumber));
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(anyPinNumber).withoutValue());
		out.assertIsSatisfied();
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canDisableDigitalListening(TestConfig config) throws Exception {
		init(config);
		context = camelContext(topics.withControlChannelEnabled());
		int anyPinNumber = anyPinNumber();
		mqttClient.stopListenig(digitalPin(anyPinNumber));
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(STOP_LISTENING_DIGITAL).forPin(anyPinNumber).withoutValue());
		out.assertIsSatisfied();
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void doesNotEnableAnalogListening_WhenControlChannelInsNOTenabled(TestConfig config) throws Exception {
		init(config);
		context = camelContext(topics);
		mqttClient.startListenig(analogPin(anyPinNumber()));
		assertNoMessage(getMockEndpoint());
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void doesNotEnableDigitalListening_WhenControlChannelInsNOTenabled(TestConfig config) throws Exception {
		init(config);
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
