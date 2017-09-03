package org.ardulink.mqtt;

import static java.math.RoundingMode.HALF_UP;
import static org.apache.camel.ShutdownRunningTask.CompleteAllTasks;
import static org.ardulink.mqtt.camel.FromArdulinkProtocol.fromArdulinkProtocol;
import static org.ardulink.mqtt.camel.ToArdulinkProtocol.toArdulinkProtocol;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.math.BigDecimal;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.model.AggregateDefinition;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;

public class MqttCamelRouteBuilder {

	private static final String PUBLISH_HEADER = "CamelMQTTPublishTopic";
	private static final String SUBSCRIBE_HEADER = "CamelMQTTSubscribeTopic";

	public enum CompactStrategy {
		AVERAGE, USE_LATEST;
	}

	public class ConfiguredMqttCamelRouteBuilder {

		public ConfiguredMqttCamelRouteBuilder andReverse() throws Exception {
			context.addRoutes(new RouteBuilder() {
				@Override
				public void configure() {
					from(mqtt)
							.transform(body().convertToString())
							.process(
									toArdulinkProtocol(config).topicFrom(
											header(SUBSCRIBE_HEADER)))
							.to(something)
							.shutdownRunningTask(CompleteAllTasks);
				}

			});
			return this;
		}
	}

	private final CamelContext context;
	private final Config config;
	private String something;
	private String mqtt;
	private CompactStrategy compactStrategy;

	public MqttCamelRouteBuilder(final CamelContext context, final Config config) {
		this.context = context;
		this.config = config;
	}

	public MqttCamelRouteBuilder compactStrategy(CompactStrategy compactStrategy) {
		this.compactStrategy = compactStrategy;
		return this;
	}

	public MqttCamelRouteBuilder to(final String to) {
		return this;
	}

	public ConfiguredMqttCamelRouteBuilder fromSomethingToMqtt(
			final String something, final String mqtt) throws Exception {
		this.something = something;
		this.mqtt = mqtt;
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				RouteDefinition routeDef = from(something).process(
						fromArdulinkProtocol(config).headerNameForTopic(
								PUBLISH_HEADER));
				if (compactStrategy != null) {
					ChoiceDefinition pre = routeDef.choice().when(
							simple("${in.body} is 'java.lang.Number'"));
					useStrategy(pre, compactStrategy)
							.to("direct:endOfAnalogAggregation").endChoice()
							.otherwise().to("direct:endOfAnalogAggregation");
					routeDef = from("direct:endOfAnalogAggregation");
				}
				routeDef.transform(body().convertToString()).to(mqtt);
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
						.aggregate(header(PUBLISH_HEADER),
								new UseLatestAggregationStrategy())
						.completionInterval(1000).completeAllOnStop();
			}

			private AggregateDefinition appendAverageStrategy(
					ChoiceDefinition def) {
				return def
						.aggregate(header(PUBLISH_HEADER), sum())
						.completionInterval(1000)
						.completeAllOnStop()
						.process(
								divideByValueOf(exchangeProperty("CamelAggregatedSize")));
			}

		});
		return new ConfiguredMqttCamelRouteBuilder();
	}

	private Processor divideByValueOf(final ValueBuilder valueBuilder) {
		return new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Message in = exchange.getIn();
				BigDecimal sum = new BigDecimal(checkNotNull(
						in.getBody(Number.class), "Body of %s is null", in)
						.toString());
				BigDecimal divisor = new BigDecimal(checkNotNull(
						valueBuilder.evaluate(exchange, Integer.class),
						"No %s set in exchange %s", valueBuilder, exchange)
						.toString());
				in.setBody(sum.divide(divisor, HALF_UP));
			}

		};
	}

	private AggregationStrategy sum() {
		return new AggregationStrategy() {
			@Override
			public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
				if (oldExchange == null) {
					return newExchange;
				}
				oldExchange.getIn().setBody(sum(oldExchange, newExchange));
				return oldExchange;
			}

			private BigDecimal sum(Exchange oldExchange, Exchange newExchange) {
				return numberFromPayload(oldExchange).add(
						numberFromPayload(newExchange));
			}

			private BigDecimal numberFromPayload(Exchange oldExchange) {
				return new BigDecimal(oldExchange.getIn().getBody(Number.class)
						.toString());
			}

		};
	}

}
