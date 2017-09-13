package org.ardulink.mqtt;

import static java.math.RoundingMode.HALF_UP;
import static org.apache.camel.ShutdownRunningTask.CompleteAllTasks;
import static org.ardulink.mqtt.camel.FromArdulinkProtocol.fromArdulinkProtocol;
import static org.ardulink.mqtt.camel.ToArdulinkProtocol.toArdulinkProtocol;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

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
import org.ardulink.util.Strings;

public class MqttCamelRouteBuilder {

	private static final String PUBLISH_HEADER = "CamelMQTTPublishTopic";
	private static final String SUBSCRIBE_HEADER = "CamelMQTTSubscribeTopic";

	public enum CompactStrategy {
		AVERAGE, USE_LATEST;
	}

	public static class MqttConnectionProperties {

		private String name = "ardulink-mqtt";
		private String brokerHost = "localhost";
		private Integer brokerPort;
		private boolean ssl;
		private String clientId;
		private String user;
		private byte[] pass;

		public MqttConnectionProperties name(String name) {
			this.name = name;
			return this;
		}

		public MqttConnectionProperties brokerHost(String brokerHost) {
			this.brokerHost = brokerHost;
			return this;
		}

		public MqttConnectionProperties brokerPort(Integer brokerPort) {
			this.brokerPort = brokerPort;
			return this;
		}

		public MqttConnectionProperties brokerPort(int brokerPort) {
			return brokerPort(Integer.valueOf(brokerPort));
		}

		public MqttConnectionProperties ssl(boolean ssl) {
			this.ssl = ssl;
			return this;
		}

		public MqttConnectionProperties clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		public int getBrokerPort() {
			return brokerPort == null ? (ssl ? 8883 : 1883) : brokerPort
					.intValue();
		}

		public MqttConnectionProperties auth(String user, byte[] pass) {
			checkArgument(!Strings.nullOrEmpty(user),
					"user must not be null or empty");
			checkArgument(pass != null, "pass must not be null");
			this.user = user;
			this.pass = pass;
			return this;
		}

		public String buildCamelURI(Topics topics) {
			StringBuilder sb = new StringBuilder();
			sb = sb.append(String.format("mqtt:%s?host=%s://%s:%s", name,
					(ssl ? "ssl" : "tcp"), brokerHost, getBrokerPort()));
			sb = hasAuth() ? sb.append(String.format(
					"&userName=%s&password=%s", user, new String(pass))) : sb;
			sb = hasClientId() ? sb.append(String.format("&clientId=%s",
					clientId)) : sb;
			sb = sb.append(String.format("&subscribeTopicNames=%s#",
					topics.getTopic()));
			sb = sb.append("&connectAttemptsMax=1&reconnectAttemptsMax=0");
			return sb.toString();
		}

		private boolean hasAuth() {
			return user != null && pass != null;
		}

		private boolean hasClientId() {
			return clientId != null;
		}

	}

	public class ConfiguredMqttCamelRouteBuilder {

		public ConfiguredMqttCamelRouteBuilder andReverse() throws Exception {
			context.addRoutes(new RouteBuilder() {
				@Override
				public void configure() {
					from(mqtt)
							.transform(body().convertToString())
							.process(
									toArdulinkProtocol(topics).topicFrom(
											header(SUBSCRIBE_HEADER)))
							.to(something)
							.shutdownRunningTask(CompleteAllTasks);
				}

			});
			return this;
		}
	}

	private final CamelContext context;
	private final Topics topics;
	private String something;
	private String mqtt;

	private CompactStrategy compactStrategy;
	private long compactMillis;

	public MqttCamelRouteBuilder(CamelContext context, Topics topics) {
		this.context = context;
		this.topics = topics;
	}

	public MqttCamelRouteBuilder compact(CompactStrategy strategy,
			int duration, TimeUnit timeUnit) {
		this.compactStrategy = checkNotNull(strategy,
				"strategy must not be null");
		checkArgument(duration > 0,
				"duration must not be zero or negative but was %s", duration);
		this.compactMillis = checkNotNull(timeUnit, "timeUnit must not be null")
				.toMillis(duration);
		return this;
	}

	public MqttCamelRouteBuilder to(String to) {
		return this;
	}

	public ConfiguredMqttCamelRouteBuilder fromSomethingToMqtt(
			String something, MqttConnectionProperties properties)
			throws Exception {
		return fromSomethingToMqtt(something, properties.buildCamelURI(topics));
	}

	public ConfiguredMqttCamelRouteBuilder fromSomethingToMqtt(
			final String something, final String mqtt) throws Exception {
		this.something = something;
		this.mqtt = mqtt;
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				RouteDefinition routeDef = from(something).process(
						fromArdulinkProtocol(topics).headerNameForTopic(
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
					CompactStrategy strategy) {
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
						.completionInterval(compactMillis).completeAllOnStop();
			}

			private AggregateDefinition appendAverageStrategy(
					ChoiceDefinition def) {
				return def
						.aggregate(header(PUBLISH_HEADER), sum())
						.completionInterval(compactMillis)
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
