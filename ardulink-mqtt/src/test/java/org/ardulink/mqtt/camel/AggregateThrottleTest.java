package org.ardulink.mqtt.camel;

import static java.math.RoundingMode.HALF_UP;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.mqtt.camel.AggregateThrottleTest.CompactStrategy.AVERAGE;
import static org.ardulink.mqtt.camel.AggregateThrottleTest.CompactStrategy.USE_LATEST;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.math.BigDecimal;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.AggregateDefinition;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.ardulink.core.Pin;
import org.ardulink.mqtt.Config;
import org.junit.After;
import org.junit.Test;

public class AggregateThrottleTest {

	private static final String HEADER_FOR_TOPIC = "CamelMQTTPublishTopic";

	private static final String TOPIC = "foo/bar/topic/";

	private static final String IN = "direct:in";
	private static final String OUT = "mock:result";

	private CamelContext context;

	public enum CompactStrategy {
		AVERAGE, USE_LATEST;
	}

	@After
	public void testDown() throws Exception {
		context.stop();
	}

	@Test
	public void doesAggregateAnalogsUsingLastAndKeepsDigitals()
			throws Exception {
		context = camelContext(config(), USE_LATEST);
		MockEndpoint out = getMockEndpoint();

		out.expectedBodiesReceived(true, false, 1, 12);
		out.expectedHeaderValuesReceivedInAnyOrder(HEADER_FOR_TOPIC,
				"foo/bar/topic/D0/value/get", "foo/bar/topic/D0/value/get",
				"foo/bar/topic/A1/value/get", "foo/bar/topic/A0/value/get");

		simArdulinkSends(analogPin(0), 1);
		simArdulinkSends(analogPin(0), 3);
		simArdulinkSends(analogPin(1), 999);
		simArdulinkSends(digitalPin(0), true);
		simArdulinkSends(digitalPin(0), false);
		simArdulinkSends(analogPin(0), 12);
		simArdulinkSends(analogPin(1), 1);

		out.assertIsSatisfied();
	}

	@Test
	public void agregateAnalogsSeparatlyUsingAverageAndKeepsDigitals()
			throws Exception {
		context = camelContext(config(), AVERAGE);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived(true, false, 500, 5);
		out.expectedHeaderValuesReceivedInAnyOrder(HEADER_FOR_TOPIC,
				"foo/bar/topic/D0/value/get", "foo/bar/topic/D0/value/get",
				"foo/bar/topic/A1/value/get", "foo/bar/topic/A0/value/get");

		simArdulinkSends(analogPin(0), 1);
		simArdulinkSends(analogPin(0), 3);
		simArdulinkSends(analogPin(1), 999);
		simArdulinkSends(digitalPin(0), true);
		simArdulinkSends(digitalPin(0), false);
		simArdulinkSends(analogPin(0), 12);
		simArdulinkSends(analogPin(1), 1);

		out.assertIsSatisfied();
	}

	private void simArdulinkSends(Pin pin, Object value) {
		String body = String.format("alp://%sred/%s/%s", pin.is(ANALOG) ? "a"
				: "d", pin.pinNum(), alpValue(value));
		context.createProducerTemplate().sendBody(IN, body);
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

	private Config config() {
		return Config.withTopic(TOPIC);
	}

	private CamelContext camelContext(final Config config,
			final CompactStrategy compactStrategy) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				FromArdulinkProtocol fromArdulinkProtocol = new FromArdulinkProtocol(
						config).headerNameForTopic("CamelMQTTPublishTopic");
				ChoiceDefinition pre = from(IN).bean(fromArdulinkProtocol)
						.choice()
						.when(simple("${in.body} is 'java.lang.Number'"));
				useStrategy(pre, compactStrategy).endChoice().otherwise()
						.to("direct:endOfAnalogAggregation");
				from("direct:endOfAnalogAggregation").transform(
						body().convertToString()).to(OUT);
			}

			private AggregateDefinition useStrategy(ChoiceDefinition def,
					final CompactStrategy strategy) {
				switch (strategy) {
				case USE_LATEST:
					return appendUseLatestStrategy(def);
				case AVERAGE:
					return appendAverageStrategy(def);
				default:
					throw new IllegalStateException("Cannot handle " + strategy);
				}
			}

			private AggregateDefinition appendUseLatestStrategy(
					ChoiceDefinition def) {
				return def
						.aggregate(header(HEADER_FOR_TOPIC),
								new UseLatestAggregationStrategy())
						.completionInterval(1000).completeAllOnStop()
						.to("direct:endOfAnalogAggregation");
			}

			private AggregateDefinition appendAverageStrategy(
					ChoiceDefinition def) {
				return def
						.aggregate(header(HEADER_FOR_TOPIC), sum())
						.completionInterval(1000)
						.completeAllOnStop()
						.process(
								divideByValueOf(exchangeProperty("CamelAggregatedSize")))
						.to("direct:endOfAnalogAggregation");
			}

			private Processor divideByValueOf(final ValueBuilder valueBuilder) {
				return new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						Message in = exchange.getIn();
						BigDecimal sum = new BigDecimal(checkNotNull(
								in.getBody(Number.class), "Body of %s is null",
								in).toString());
						BigDecimal divisor = new BigDecimal(checkNotNull(
								valueBuilder.evaluate(exchange, Integer.class),
								"No %s set in exchange %s", valueBuilder,
								exchange).toString());
						in.setBody(sum.divide(divisor, HALF_UP));
					}

				};
			}

			private AggregationStrategy sum() {
				return new AggregationStrategy() {
					@Override
					public Exchange aggregate(Exchange oldExchange,
							Exchange newExchange) {
						if (oldExchange == null) {
							return newExchange;
						}
						oldExchange.getIn().setBody(
								sum(oldExchange, newExchange));
						return oldExchange;
					}

					private BigDecimal sum(Exchange oldExchange,
							Exchange newExchange) {
						return numberFromPayload(oldExchange).add(
								numberFromPayload(newExchange));
					}

					private BigDecimal numberFromPayload(Exchange oldExchange) {
						return new BigDecimal(oldExchange.getIn()
								.getBody(Number.class).toString());
					}

				};
			}
		});
		context.start();
		return context;
	}

}
