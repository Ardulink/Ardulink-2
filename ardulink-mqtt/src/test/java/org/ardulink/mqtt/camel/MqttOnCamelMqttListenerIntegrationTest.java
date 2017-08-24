package org.ardulink.mqtt.camel;

import static org.apache.camel.ShutdownRunningTask.CompleteAllTasks;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import io.moquette.server.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;
import org.ardulink.mqtt.Config;
import org.ardulink.mqtt.MqttBroker;
import org.ardulink.util.URIs;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MqttOnCamelMqttListenerIntegrationTest {

	private static final String mockURI = "ardulink://mock";

	private Link link;

	private static final String TOPIC = "any/topic-"
			+ System.currentTimeMillis();

	private Server broker;

	private CamelContext context;

	@Before
	public void setup() throws Exception {
		link = Links.getLink(URIs.newURI(mockURI));
		broker = MqttBroker.builder().startBroker();
	}

	@After
	public void tearDown() throws IOException {
		broker.stopServer();
		link.close();
	}

	@Test
	public void startListeningOnPassedLinks() throws Exception {
		context = camelContext(config());
		TimeUnit.SECONDS.sleep(3);
		haltCamel();
		Link mock = getMock(link);
		verify(mock).startListening(digitalPin(1));
		verify(mock).startListening(digitalPin(2));
		verify(mock).startListening(analogPin(1));
		verify(mock).close();
		verifyNoMoreInteractions(mock);
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
				from(mqtt())
						.transform(body().convertToString())
						.setHeader("topic")
						.expression(
								simple("${in.header.CamelMQTTSubscribeTopic}"))
						.process(new ToArdulinkProtocol(config))
						.to(mockURI + "?listenTo=d1,d2,a1")
						.shutdownRunningTask(CompleteAllTasks);
			}
		});
		context.start();
		return context;
	}

	private Link getMock(Link link) {
		return ((LinkDelegate) link).getDelegate();
	}

	private String mqtt() {
		return "mqtt:localhost?connectAttemptsMax=1"
				+ "&reconnectAttemptsMax=0" + "&subscribeTopicNames=" + TOPIC
				+ "/#";
	}

}
