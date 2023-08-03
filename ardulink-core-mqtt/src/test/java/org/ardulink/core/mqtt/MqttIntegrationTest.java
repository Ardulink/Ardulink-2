/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.core.mqtt;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.ardulink.core.mqtt.Broker.newBroker;
import static org.ardulink.core.mqtt.EventCollector.eventCollector;
import static org.ardulink.core.mqtt.duplicated.AnotherMqttClient.newClient;
import static org.ardulink.util.ServerSockets.freePort;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.ardulink.core.Link;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.events.EventListenerAdapter;
import org.ardulink.core.events.FilteredEventListenerAdapter;
import org.ardulink.core.mqtt.duplicated.AnotherMqttClient;
import org.ardulink.core.mqtt.duplicated.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Timeout(value = 10, unit = SECONDS)
class MqttIntegrationTest {

	private static class TestConfig {

		private final String name;
		private final boolean separatedTopics;
		private final String messageFormat;

		public TestConfig(String name, boolean separateTopics, String messageFormat) {
			this.name = name;
			this.separatedTopics = separateTopics;
			this.messageFormat = messageFormat;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	static final String TOPIC = "myTopic" + System.currentTimeMillis();

	@RegisterExtension
	Broker broker = newBroker().port(freePort());

	@RegisterExtension
	AnotherMqttClient mqttClient = newClient(TOPIC, broker.port());

	Link link;

	String messageFormat;

	@AfterEach
	void tearDown() throws InterruptedException, IOException {
		link.close();
	}

	private static List<TestConfig> data() {
		return asList( //
				new TestConfig("sameTopic", false, TOPIC + "/%s"), //
				new TestConfig("separateTopics", true, TOPIC + "/%s/value/set") //
		);
	}

	void init(TestConfig config) {
		this.mqttClient.appendValueSet(config.separatedTopics);
		this.messageFormat = config.messageFormat;
		String clientUri = "ardulink://mqtt?host=localhost&port=" + broker.port() + "&topic=" + TOPIC
				+ "&separatedTopics=" + config.separatedTopics;
		this.link = Links.getLink(clientUri);
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canSwitchDigitalPin(TestConfig config) throws IOException {
		init(config);
		link.switchDigitalPin(digitalPin(30), true);
		mqttClient.awaitMessages(m -> assertThat(m).singleElement().isEqualTo(new Message(topic("D30"), "true")));
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canSwitchAnalogPin(TestConfig config) throws IOException {
		init(config);
		link.switchAnalogPin(analogPin(12), 34);
		mqttClient.awaitMessages(m -> assertThat(m).singleElement().isEqualTo(new Message(topic("A12"), "34")));
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void sendsControlMessageWhenAddingAnalogListener(TestConfig config) throws IOException {
		init(config);
		link.addListener(new FilteredEventListenerAdapter(analogPin(1), delegate()));
		mqttClient.awaitMessages(
				m -> assertThat(m).singleElement().isEqualTo(new Message(topic("system/listening/A1"), "true")));
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void sendsControlMessageWhenAddingDigitalListener(TestConfig config) throws IOException {
		init(config);
		link.addListener(new FilteredEventListenerAdapter(digitalPin(2), delegate()));
		mqttClient.awaitMessages(
				m -> assertThat(m).singleElement().isEqualTo(new Message(topic("system/listening/D2"), "true")));
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void sendsControlMessageWhenRemovingAnalogListener(TestConfig config) throws IOException {
		init(config);
		EventListenerAdapter listener = new FilteredEventListenerAdapter(analogPin(1), delegate());
		link.addListener(listener);
		link.addListener(listener);
		Message m1 = new Message(topic("system/listening/A1"), "true");
		// at the moment this is sent twice (see AbstractListenerLink)
		mqttClient.awaitMessages(m -> assertThat(m).containsExactly(m1, m1));
		link.removeListener(listener);
		mqttClient.awaitMessages(m -> assertThat(m).isEmpty());
		link.removeListener(listener);
		Message m2 = new Message(topic("system/listening/A1"), "false");
		mqttClient.awaitMessages(m -> assertThat(m).singleElement().isEqualTo(m2));
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void sendsControlMessageWhenRemovingDigitalListener(TestConfig config) throws IOException {
		init(config);
		EventListenerAdapter listener = new FilteredEventListenerAdapter(digitalPin(1), delegate());
		link.addListener(listener);
		link.addListener(listener);
		Message m1 = new Message(topic("system/listening/D1"), "true");
		// at the moment this is sent twice (see AbstractListenerLink)
		mqttClient.awaitMessages(m -> assertThat(m).containsExactly(m1, m1));
		link.removeListener(listener);
		mqttClient.awaitMessages(m -> assertThat(m).isEmpty());
		link.removeListener(listener);
		Message m2 = new Message(topic("system/listening/D1"), "false");
		mqttClient.awaitMessages(m -> assertThat(m).singleElement().isEqualTo(m2));
	}

	private String topic(String pin) {
		return String.format(messageFormat, pin);
	}

	// ---------------------------------------------------------------------------

	EventCollector eventCollector = eventCollector();

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canSwitchDigitalPinViaBroker(TestConfig config) throws Exception {
		init(config);
		DigitalPin pin = digitalPin(1);
		boolean value = true;
		link.addListener(eventCollector);
		mqttClient.switchPin(pin, value);
		eventCollector.awaitEvents(DIGITAL, l -> l.contains(digitalPinValueChanged(pin, value)));
	}

	@ParameterizedTest(name = "{index} {0}")
	@MethodSource("data")
	void canSwitchAnalogPinViaBroker(TestConfig config) throws Exception {
		init(config);
		AnalogPin pin = analogPin(2);
		int value = 123;
		link.addListener(eventCollector);
		mqttClient.switchPin(pin, value);
		eventCollector.awaitEvents(ANALOG, l -> l.contains(analogPinValueChanged(pin, value)));
	}

	private EventListenerAdapter delegate() {
		return null;
	}

}
