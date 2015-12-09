package com.github.pfichtner.ardulink;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;

import java.net.URISyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.util.AnotherMqttClient;

public class MqttMainStandaloneTest {

	private static final String topic = "myTestTopic";

	private final Link link = mock(Link.class);

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Test
	public void clientCanConnectToNewlyStartedBroker() throws Exception {
		MqttMain mqttMain = new MqttMain() {
			@Override
			protected Link createLink() throws Exception, URISyntaxException {
				return link;
			}
		};
		mqttMain.setSleepSecs(0);
		mqttMain.setStandalone(true);
		mqttMain.setBrokerTopic(topic);

		try {
			mqttMain.connectToMqttBroker();
			AnotherMqttClient.builder().topic(topic).connect();
		} finally {
			mqttMain.close();
		}

	}

}
