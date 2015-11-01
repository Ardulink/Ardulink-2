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
package com.github.pfichtner.ardulink;

import static com.github.pfichtner.ardulink.AbstractMqttAdapter.CompactStrategy.AVERAGE;
import static com.github.pfichtner.ardulink.AbstractMqttAdapter.CompactStrategy.LAST_WINS;
import static com.github.pfichtner.ardulink.compactors.Tolerance.maxTolerance;
import static com.github.pfichtner.ardulink.util.MqttMessageBuilder.mqttMessageWithBasicTopic;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.ANALOG_PIN_READ;
import static com.github.pfichtner.ardulink.util.TestUtil.createConnection;
import static com.github.pfichtner.ardulink.util.TestUtil.getField;
import static com.github.pfichtner.ardulink.util.TestUtil.set;
import static com.github.pfichtner.ardulink.util.TestUtil.toCodepoints;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zu.ardulink.ConnectionContact;
import org.zu.ardulink.Link;
import org.zu.ardulink.connection.Connection;

import com.github.pfichtner.ardulink.compactors.SlicedAnalogReadChangeListenerAdapter;
import com.github.pfichtner.ardulink.compactors.TimeSlicer;
import com.github.pfichtner.ardulink.util.Message;
import com.github.pfichtner.ardulink.util.MqttMessageBuilder;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class FloodProtectTest {

	public static class DummyTimeSlicer implements TimeSlicer {

		private final List<SlicedAnalogReadChangeListenerAdapter> workers = new ArrayList<SlicedAnalogReadChangeListenerAdapter>();

		@Override
		public void add(SlicedAnalogReadChangeListenerAdapter worker) {
			this.workers.add(worker);
		}

		public void simulateTick() {
			for (SlicedAnalogReadChangeListenerAdapter worker : workers) {
				worker.ticked();
			}
		}

	}

	private static final String LINKNAME = "testlink";

	private final List<Message> published = new ArrayList<Message>();

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final ConnectionContact connectionContact = new ConnectionContact(
			null);
	private final Connection connection = createConnection(outputStream,
			connectionContact);
	private final Link link = Link.createInstance(LINKNAME, connection);

	private final AbstractMqttAdapter mqttClient = new AbstractMqttAdapter(
			link, Config.DEFAULT) {
		@Override
		void fromArduino(String topic, String message) {
			published.add(new Message(topic, message));
		}
	};

	private final DummyTimeSlicer dummyTimeSlicer = new DummyTimeSlicer();

	private final MqttMessageBuilder mqttMessage = mqttMessageWithBasicTopic(Config.DEFAULT_TOPIC);

	{
		// there is an extremely high coupling of ConnectionContact and Link
		// which can not be solved other than injecting the variables through
		// reflection
		set(connectionContact, getField(connectionContact, "link"), link);
		set(link, getField(link, "connectionContact"), connectionContact);

	}

	private List<Message> published() {
		List<Message> result = new ArrayList<Message>(published);
		published.clear();
		return result;
	}

	@Before
	public void setup() {
		link.connect();
	}

	@After
	public void tearDown() {
		link.disconnect();
		Link.destroyInstance(LINKNAME);
	}

	@Test
	public void doesNotPublishPinChangesLowerThanToleranceValueWhenIncreasing() {
		int pin = 9;
		int valueLow = 123;
		int valueHigh = 127;
		mqttClient.configureAnalogReadChangeListener(pin)
				.tolerance(maxTolerance(3)).add();
		for (int i = valueLow; i <= valueHigh; i++) {
			simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(
					pin).withValue(i));
		}
		assertThat(published(), is(Arrays.asList(mqttMessage.analogPin(pin)
				.hasValue(valueLow),
				mqttMessage.analogPin(pin).hasValue(valueHigh))));
	}

	@Test
	public void doesNotPublishPinChangesLowerThanToleranceValueWhenDecreasing() {
		int pin = 9;
		int valueHigh = 123;
		int valueLow = 119;
		mqttClient.configureAnalogReadChangeListener(pin)
				.tolerance(maxTolerance(3)).add();
		for (int i = valueHigh; i >= valueLow; i--) {
			simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(
					pin).withValue(i));
		}
		assertThat(published(), is(Arrays.asList(mqttMessage.analogPin(pin)
				.hasValue(valueHigh),
				mqttMessage.analogPin(pin).hasValue(valueLow))));
	}

	@Test
	public void whenGettingLowValueMessageIsPublishedAnyhow() {
		int pin = 9;
		mqttClient.configureAnalogReadChangeListener(pin)
				.tolerance(maxTolerance(25)).add();
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.withValue(1));
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.withValue(0));
		MqttMessageBuilder mqttBuilder = mqttMessage.analogPin(pin);
		assertThat(
				published(),
				is(Arrays.asList(mqttBuilder.hasValue(1),
						mqttBuilder.hasValue(0))));
	}

	@Test
	public void whenGettingHighValueMessageIsPublishedAnyhow() {
		int pin = 9;
		mqttClient.configureAnalogReadChangeListener(pin)
				.tolerance(maxTolerance(25)).add();
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.withValue(254));
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.withValue(255));
		assertThat(published(), is(Arrays.asList(mqttMessage.analogPin(pin)
				.hasValue(254), mqttMessage.analogPin(pin).hasValue(255))));
	}

	// --------------------------------------------------------------------------------------------------

	@Test
	public void compactLatestWins() {
		int pin = 11;
		mqttClient.configureAnalogReadChangeListener(pin)
				.compact(LAST_WINS, dummyTimeSlicer).add();
		fire(pin);
		dummyTimeSlicer.simulateTick();
		Message first = mqttMessage.analogPin(pin).hasValue(0);
		Message last = mqttMessage.analogPin(pin).hasValue(99);
		assertThat(published(), is(Arrays.asList(first, last)));
	}

	@Test
	public void compactAverage() {
		int pin = 11;
		mqttClient.configureAnalogReadChangeListener(pin)
				.compact(AVERAGE, dummyTimeSlicer).add();
		fire(pin);
		dummyTimeSlicer.simulateTick();
		Message first = mqttMessage.analogPin(pin).hasValue(0);
		Message average = mqttMessage.analogPin(pin).hasValue(50);
		assertThat(published(), is(Arrays.asList(first, average)));

	}

	protected void fire(int pin) {
		for (int value = 0; value < 100; value++) {
			simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(
					pin).withValue(value));
		}
	}

	private void simulateArduinoToMqtt(String message) {
		int[] codepoints = toCodepoints(message);
		connectionContact.parseInput(anyId(), codepoints.length, codepoints);
	}

	private String anyId() {
		return "randomId";
	}

}
