package org.ardulink.mqtt.camel;

import static java.math.RoundingMode.HALF_UP;
import static org.ardulink.mqtt.camel.AggregateThrottleTest.CompactStrategy.AVERAGE;
import static org.ardulink.mqtt.camel.AggregateThrottleTest.CompactStrategy.LAST_WINS;

import java.math.BigDecimal;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
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
		AVERAGE, LAST_WINS;
	}

	@After
	public void testDown() throws Exception {
		context.stop();
	}

	@Test
	public void doesAggregateAnalogsUsingLastAndKeepsDigitals()
			throws Exception {
		context = camelContext(config(), LAST_WINS);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived("alp://dred/0/1", "alp://dred/0/0",
				"alp://ared/0/12", "alp://ared/1/1");

		mqttSend("A0", 1);
		mqttSend("A0", 3);
		mqttSend("A1", 999);
		mqttSend("D0", true);
		mqttSend("D0", false);
		mqttSend("A0", 12);
		mqttSend("A1", 1);

		out.assertIsSatisfied();
	}

	@Test
	public void agregateAnalogsSeparatlyUsingAverageAndKeepsDigitals()
			throws Exception {
		context = camelContext(config(), AVERAGE);
		MockEndpoint out = getMockEndpoint();
		out.expectedBodiesReceived("alp://dred/0/1", "alp://dred/0/0",
				"alp://ared/0/5", "alp://ared/1/500");

		mqttSend("A0", 1);
		mqttSend("A0", 3);
		mqttSend("A1", 999);
		mqttSend("D0", true);
		mqttSend("D0", false);
		mqttSend("A0", 12);
		mqttSend("A1", 1);

		out.assertIsSatisfied();

	}

	private void mqttSend(String pin, Object value) {
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
						.to("seda:seda1");
				from("seda:seda1").transform(body().convertToString())
						.process(toArdulinkProtocol).to(OUT);
			}

			private AggregateDefinition useStrategy(ChoiceDefinition def,
					final CompactStrategy strategy) {
				switch (strategy) {
				case LAST_WINS:
					return appendLastStrategy(def);
				case AVERAGE:
					return appendAverageStrategy(def);
				default:
					throw new IllegalStateException("Cannot handle " + strategy);
				}
			}

			private AggregateDefinition appendLastStrategy(ChoiceDefinition def) {
				return def
						.aggregate(header(HEADER_FOR_TOPIC),
								new UseLatestAggregationStrategy())
						.completionInterval(1000).completeAllOnStop()
						.to("seda:seda1");
			}

			private AggregateDefinition appendAverageStrategy(
					ChoiceDefinition def) {
				return def.aggregate(header(HEADER_FOR_TOPIC), sum())
						.completionInterval(1000).completeAllOnStop()
						.process(divide()).to("seda:seda1");
			}

			private Processor divide() {
				return new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						Integer messageCount = exchange.getProperty(
								"CamelAggregatedSize", Integer.class);
						if (messageCount != null) {
							BigDecimal sum = new BigDecimal(exchange.getIn()
									.getBody(Number.class).toString());
							exchange.getIn().setBody(
									sum.divide(
											new BigDecimal(messageCount
													.toString()), HALF_UP));
						}
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
