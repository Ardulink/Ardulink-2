package org.ardulink.mqtt.camel;

import static org.ardulink.util.ServerSockets.freePort;

import org.ardulink.mqtt.Config;
import org.ardulink.mqtt.MqttBroker;
import org.ardulink.mqtt.util.AnotherMqttClient;
import org.junit.Before;

public class MqttOnCamelMqttToLinkIntegrationOneTopicTest extends
		AbstractMqttOnCamelMqttToLinkIntegrationTest {

	@Before
	public void setup() throws Exception {
		broker = MqttBroker.builder().port(freePort()).startBroker();
		mqttClient = AnotherMqttClient.builder().port(broker.getPort())
				.topic(TOPIC).connect();
		config = Config.withTopic(TOPIC);
	}

}
