package org.ardulink.mqtt.camel;

import static java.util.Arrays.asList;
import static org.apache.camel.ShutdownRunningTask.CompleteAllTasks;
import static org.ardulink.core.Pin.analogPin;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.moquette.server.Server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import org.ardulink.mqtt.Config;
import org.ardulink.mqtt.MqttBroker;
import org.ardulink.mqtt.util.AnotherMqttClient;
import org.ardulink.mqtt.util.Message;
import org.ardulink.util.URIs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MqttOnCamelLinkToMqttIntegrationTest {

	private static final String mockURI = "ardulink://dummy";

	private static final String TOPIC = "any/topic-"
			+ System.currentTimeMillis();

	private AnotherMqttClient mqttClient;

	private final Link link = Links.getLink(URIs.newURI(mockURI));

	private Server broker;

	private CamelContext context;

	@Before
	public void setup() throws Exception {
		broker = MqttBroker.builder().startBroker();
		mqttClient = AnotherMqttClient.builder().topic(TOPIC).connect();
	}

	@After
	public void tearDown() throws IOException {
		mqttClient.close();
		broker.stopServer();
		link.close();
	}

	@Test
	public void mqttMessageIsSentOnAnalogPinChange() throws Exception {
		context = camelContext(config());
		getDelegate().fireStateChanged(
				new DefaultAnalogPinValueChangedEvent(analogPin(2), 42));

		haltCamel();
		List<Message> messages = mqttClient.getMessages();
		assertThat(messages,
				is(asList(new Message("camel/mqtt/test", "A2=42"))));
		// TODO this is nor the right topic nor the right message
	}

	private Config config() {
		return Config.withTopic(TOPIC);
	}

	private CamelContext haltCamel() throws InterruptedException, Exception {
		TimeUnit.MILLISECONDS.sleep(500);
		context.stop();
		return context;
	}

	private CamelContext camelContext(final Config config) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(mockURI)
						.transform(body().convertToString())
						.process(new Processor() {

							@Override
							public void process(Exchange exchange)
									throws Exception {
								org.apache.camel.Message in = exchange.getIn();
								System.out.println(in);
							}
						})
						.setHeader("topic")
						.expression(
								simple("${in.header.CamelMQTTSubscribeTopic}"))
						.to(mqtt()).shutdownRunningTask(CompleteAllTasks);
			}
		});
		context.start();
		return context;
	}

	private AbstractListenerLink getDelegate() {
		return (AbstractListenerLink) ((LinkDelegate) link).getDelegate();
	}

	private String mqtt() {
		return "mqtt:localhost?connectAttemptsMax=1"
				+ "&reconnectAttemptsMax=0" + "&subscribeTopicNames=" + TOPIC
				+ "/#";
		// publishTopicName
	}

}
