package org.ardulink.mqtt.camel;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.mqtt.MqttCamelRouteBuilder.CompactStrategy.AVERAGE;
import static org.ardulink.mqtt.MqttCamelRouteBuilder.CompactStrategy.USE_LATEST;

import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Pin;
import org.ardulink.mqtt.Topics;
import org.ardulink.mqtt.MqttCamelRouteBuilder;
import org.ardulink.mqtt.MqttCamelRouteBuilder.CompactStrategy;
import org.junit.After;
import org.junit.Test;

public class AggregateThrottleTest {

	private static final String HEADER_FOR_TOPIC = "CamelMQTTPublishTopic";

	private static final String TOPIC = "foo/bar/topic/";

	private static final String IN = "direct:in";
	private static final String OUT = "mock:result";

	private CamelContext context;

	@After
	public void testDown() throws Exception {
		context.stop();
	}

	@Test
	public void doesAggregateAnalogsUsingLastAndKeepsDigitals()
			throws Exception {
		context = camelContext(topics(), USE_LATEST);
		MockEndpoint out = getMockEndpoint();

		out.expectedBodiesReceived(true, false, 12, 1);
		out.expectedHeaderValuesReceivedInAnyOrder(HEADER_FOR_TOPIC,
				"foo/bar/topic/D0", "foo/bar/topic/D0", "foo/bar/topic/A1",
				"foo/bar/topic/A0");

		simArduinoSends(alpMessage(analogPin(0), 1));
		simArduinoSends(alpMessage(analogPin(0), 3));
		simArduinoSends(alpMessage(analogPin(1), 999));
		simArduinoSends(alpMessage(digitalPin(0), true));
		simArduinoSends(alpMessage(digitalPin(0), false));
		simArduinoSends(alpMessage(analogPin(0), 12));
		simArduinoSends(alpMessage(analogPin(1), 1));

		out.assertIsSatisfied();
	}

	@Test
	public void agregateAnalogsSeparatlyUsingAverageAndKeepsDigitals()
			throws Exception {
		context = camelContext(topics(), AVERAGE);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(true, false, 5, 500);
		out.expectedHeaderValuesReceivedInAnyOrder(HEADER_FOR_TOPIC,
				"foo/bar/topic/D0", "foo/bar/topic/D0", "foo/bar/topic/A1",
				"foo/bar/topic/A0");

		simArduinoSends(alpMessage(analogPin(0), 1));
		simArduinoSends(alpMessage(analogPin(0), 3));
		simArduinoSends(alpMessage(analogPin(1), 999));
		simArduinoSends(alpMessage(digitalPin(0), true));
		simArduinoSends(alpMessage(digitalPin(0), false));
		simArduinoSends(alpMessage(analogPin(0), 12));
		simArduinoSends(alpMessage(analogPin(1), 1));

		out.assertIsSatisfied();
	}

	private void simArduinoSends(String message) {
		context.createProducerTemplate().sendBody(IN, message);
	}

	private String alpMessage(Pin pin, Object value) {
		return String.format("alp://%sred/%s/%s", pin.is(ANALOG) ? "a" : "d",
				pin.pinNum(), alpValue(value));
	}

	private Integer alpValue(Object value) {
		if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof Boolean) {
			return Boolean.TRUE.equals(value) ? 1 : 0;
		}
		throw new IllegalStateException("Cannot handle " + value);
	}

	private MockEndpoint getMockEndpoint() {
		return context.getEndpoint(OUT, MockEndpoint.class);
	}

	private Topics topics() {
		return Topics.basedOn(TOPIC);
	}

	private CamelContext camelContext(final Topics topics,
			final CompactStrategy compactStrategy) throws Exception {
		CamelContext context = new DefaultCamelContext();
		new MqttCamelRouteBuilder(context, topics).compact(compactStrategy, 1,
				SECONDS).fromSomethingToMqtt(IN, OUT);
		context.start();
		return context;
	}

}
