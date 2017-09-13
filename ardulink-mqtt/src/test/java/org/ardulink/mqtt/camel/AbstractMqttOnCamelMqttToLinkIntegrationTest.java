package org.ardulink.mqtt.camel;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.RouteDefinition;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.mqtt.Config;
import org.ardulink.mqtt.MqttBroker;
import org.ardulink.mqtt.MqttCamelRouteBuilder;
import org.ardulink.mqtt.MqttCamelRouteBuilder.MqttConnectionProperties;
import org.ardulink.mqtt.util.AnotherMqttClient;
import org.junit.After;
import org.junit.Test;

public abstract class AbstractMqttOnCamelMqttToLinkIntegrationTest {

	private static final String OUT = "mock:result";

	protected static final String TOPIC = "any/topic-"
			+ System.currentTimeMillis();

	protected AnotherMqttClient mqttClient;

	protected MqttBroker broker;

	protected CamelContext context;

	protected Config config;

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
		context = camelContext(config.withControlChannelEnabled());
		AnalogPin pin = analogPin(6);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(START_LISTENING_ANALOG)
				.forPin(pin.pinNum()).withoutValue());
		mqttClient.startListenig(pin);
		out.assertIsSatisfied();
	}

	@Test
	public void canEnableDigitalListening() throws Exception {
		context = camelContext(config.withControlChannelEnabled());
		DigitalPin pin = digitalPin(7);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(START_LISTENING_DIGITAL)
				.forPin(pin.pinNum()).withoutValue());
		mqttClient.startListenig(pin);
		out.assertIsSatisfied();
	}

	@Test
	public void canDisableAnalogListening() throws Exception {
		context = camelContext(config.withControlChannelEnabled());
		AnalogPin pin = analogPin(6);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(STOP_LISTENING_ANALOG)
				.forPin(pin.pinNum()).withoutValue());
		mqttClient.stopListenig(pin);
		out.assertIsSatisfied();
	}

	@Test
	public void canDisableDigitalListening() throws Exception {
		context = camelContext(config.withControlChannelEnabled());
		DigitalPin pin = digitalPin(7);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(STOP_LISTENING_DIGITAL)
				.forPin(pin.pinNum()).withoutValue());
		mqttClient.stopListenig(pin);
		out.assertIsSatisfied();
	}

	@Test
	public void doesNotEnableAnalogListening_WhenControlChannelInsNOTenabled()
			throws Exception {
		context = camelContext(config);
		mqttClient.startListenig(analogPin(6));
		assertNoMessage(getMockEndpoint());
	}

	@Test
	public void doesNotEnableDigitalListening_WhenControlChannelInsNOTenabled()
			throws Exception {
		context = camelContext(config);
		mqttClient.startListenig(digitalPin(7));
		assertNoMessage(getMockEndpoint());
	}

	private void testDigital(DigitalPin pin, boolean state) throws Exception {
		context = camelContext(config);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(DIGITAL_PIN_READ).forPin(
				pin.pinNum()).withState(state));
		mqttClient.switchPin(pin, state);
		out.assertIsSatisfied();
	}

	private void testAnalog(AnalogPin pin, int value) throws Exception {
		context = camelContext(config);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(alpProtocolMessage(ANALOG_PIN_READ).forPin(
				pin.pinNum()).withValue(value));
		mqttClient.switchPin(pin, value);
		out.assertIsSatisfied();
	}

	private static void assertNoMessage(MockEndpoint out)
			throws InterruptedException {
		out.expectedMessageCount(0);
		out.assertIsSatisfied();
	}

	private CamelContext camelContext(final Config config) throws Exception {
		CamelContext context = new DefaultCamelContext();
		MqttConnectionProperties mqtt = new MqttConnectionProperties()
				.name("foo").brokerHost("localhost")
				.brokerPort(broker.getPort());
		new MqttCamelRouteBuilder(context, config).fromSomethingToMqtt(OUT,
				mqtt).andReverse();
		replaceInputs(context.getRouteDefinitions(), OUT, "direct:noop");
		context.start();
		return context;
	}

	private static void replaceInputs(Iterable<RouteDefinition> definitions,
			String oldFrom, String newFrom) {
		for (RouteDefinition definition : definitions) {
			List<FromDefinition> inputs = definition.getInputs();
			for (int i = 0; i < inputs.size(); i++) {
				if (oldFrom.equals(inputs.get(i).getEndpointUri())) {
					inputs.set(i, new FromDefinition(newFrom));
				}
			}
		}
	}

	private MockEndpoint getMockEndpoint() {
		return context.getEndpoint(OUT, MockEndpoint.class);
	}

}
