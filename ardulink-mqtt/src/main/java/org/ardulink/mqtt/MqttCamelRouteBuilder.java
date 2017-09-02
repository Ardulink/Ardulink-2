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
					from(mqtt).transform(body().convertToString())
							.process(toArdulinkProtocol).to(something)
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

	public MqttCamelRouteBuilder(final CamelContext context, final Config config) {
		this.context = context;
		this.config = config;
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
				FromArdulinkProtocol fromArdulinkProtocol = new FromArdulinkProtocol(
						config).headerNameForTopic("CamelMQTTPublishTopic");
				from(something).process(fromArdulinkProtocol)
						.transform(body().convertToString()).to(mqtt);
			}

		});
		return new ConfiguredMqttCamelRouteBuilder();
	}

}
