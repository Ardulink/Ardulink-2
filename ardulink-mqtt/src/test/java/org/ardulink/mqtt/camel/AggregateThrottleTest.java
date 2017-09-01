package org.ardulink.mqtt.camel;

import static java.math.RoundingMode.HALF_UP;
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
import org.ardulink.mqtt.Config;
import org.junit.After;
import org.junit.Test;

public class AggregateThrottleTest {

	private static final String HEADER_FOR_TOPIC = "CamelMQTTSubscribeTopic";

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
		out.expectedBodiesReceived("alp://dred/0/1", "alp://dred/0/0",
				"alp://ared/0/12", "alp://ared/1/1");

		ardulinkSend("A0", 1);
		ardulinkSend("A0", 3);
		ardulinkSend("A1", 999);
		ardulinkSend("D0", true);
		ardulinkSend("D0", false);
		ardulinkSend("A0", 12);
		ardulinkSend("A1", 1);

		out.assertIsSatisfied();
	}

	@Test
	public void agregateAnalogsSeparatlyUsingAverageAndKeepsDigitals()
			throws Exception {
		context = camelContext(config(), AVERAGE);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived("alp://dred/0/1", "alp://dred/0/0",
				"alp://ared/0/5", "alp://ared/1/500");

		ardulinkSend("A0", 1);
		ardulinkSend("A0", 3);
		ardulinkSend("A1", 999);
		ardulinkSend("D0", true);
		ardulinkSend("D0", false);
		ardulinkSend("A0", 12);
		ardulinkSend("A1", 1);

		out.assertIsSatisfied();

	}

	private void ardulinkSend(String pin, Object value) {
		context.createProducerTemplate().sendBodyAndHeader(IN, value,
				HEADER_FOR_TOPIC, TOPIC + pin + "/value/set");
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
				ToArdulinkProtocol toArdulinkProtocol = new ToArdulinkProtocol(
						config).headerNameForTopic(HEADER_FOR_TOPIC);
				ChoiceDefinition pre = from(IN).choice().when(
						simple("${in.body} is 'java.lang.Number'"));
				useStrategy(pre, compactStrategy).endChoice().otherwise()
						.to("direct:endOfAnalogAggregation");
				from("direct:endOfAnalogAggregation")
						.transform(body().convertToString())
						.process(toArdulinkProtocol).to(OUT);
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
