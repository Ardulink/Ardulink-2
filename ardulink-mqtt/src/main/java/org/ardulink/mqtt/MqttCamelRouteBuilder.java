package org.ardulink.mqtt;

import static org.apache.camel.ShutdownRunningTask.CompleteAllTasks;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.ardulink.mqtt.camel.FromArdulinkProtocol;
import org.ardulink.mqtt.camel.ToArdulinkProtocol;

public class MqttCamelRouteBuilder {

	public class ConfiguredMqttCamelRouteBuilder {
		public ConfiguredMqttCamelRouteBuilder andReverse() throws Exception {
			context.addRoutes(new RouteBuilder() {
				@Override
				public void configure() {
					ToArdulinkProtocol toArdulinkProtocol = new ToArdulinkProtocol(
							config)
							.headerNameForTopic("CamelMQTTSubscribeTopic");
					from(to).transform(body().convertToString())
							.process(toArdulinkProtocol).to(from)
							.shutdownRunningTask(CompleteAllTasks);
				}

			});
			return this;
		}
	}

	private final CamelContext context;
	private final Config config;
	private String from;
	private String to;

	public MqttCamelRouteBuilder(final CamelContext context, final Config config) {
		this.context = context;
		this.config = config;
	}

	public MqttCamelRouteBuilder to(final String to) {
		return this;
	}

	public ConfiguredMqttCamelRouteBuilder addRoute(final String from,
			final String to) throws Exception {
		this.from = from;
		this.to = to;
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				FromArdulinkProtocol fromArdulinkProtocol = new FromArdulinkProtocol(
						config).headerNameForTopic("CamelMQTTPublishTopic");
				from(from).transform(body().convertToString())
						.process(fromArdulinkProtocol).to(to);
			}

		});
		return new ConfiguredMqttCamelRouteBuilder();
	}

}
