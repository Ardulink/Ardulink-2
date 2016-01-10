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
import static com.github.pfichtner.ardulink.util.TestUtil.analogPinChanged;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.github.pfichtner.ardulink.compactors.SlicedAnalogReadChangeListenerAdapter;
import com.github.pfichtner.ardulink.compactors.TimeSlicer;
import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.StreamConnection;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol2;
import com.github.pfichtner.ardulink.util.Message;
import com.github.pfichtner.ardulink.util.MqttMessageBuilder;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
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

	private final List<Message> published = new ArrayList<Message>();

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	private final Connection connection = new StreamConnection(null,
			outputStream, ArdulinkProtocol2.instance());

	private final ConnectionBasedLink link = new ConnectionBasedLink(
			connection, ArdulinkProtocol2.instance());

	private AbstractMqttAdapter mqttClient = new AbstractMqttAdapter(link,
			Config.DEFAULT) {
		@Override
		void fromArduino(String topic, String message) {
			published.add(new Message(topic, message));
		}
	};

	private final DummyTimeSlicer dummyTimeSlicer = new DummyTimeSlicer();

	private final MqttMessageBuilder mqttMessage = mqttMessageWithBasicTopic(Config.DEFAULT_TOPIC);

	private List<Message> published() {
		List<Message> result = new ArrayList<Message>(published);
		published.clear();
		return result;
	}

	@After
	public void tearDown() throws IOException {
		link.close();
	}

	@Test
	public void doesNotPublishPinChangesLowerThanToleranceValueWhenIncreasing()
			throws IOException {
		int pin = 9;
		int valueLow = 123;
		int valueHigh = 127;
		mqttClient.configureAnalogReadChangeListener(pin)
				.tolerance(maxTolerance(3)).add();
		for (int i = valueLow; i <= valueHigh; i++) {
			link.fireStateChanged(analogPinChanged(pin, i));
		}
		assertThat(published(), is(Arrays.asList(mqttMessage.analogPin(pin)
				.hasValue(valueLow),
				mqttMessage.analogPin(pin).hasValue(valueHigh))));
	}

	@Test
	public void doesNotPublishPinChangesLowerThanToleranceValueWhenDecreasing()
			throws IOException {
		int pin = 9;
		int valueHigh = 123;
		int valueLow = 119;
		mqttClient.configureAnalogReadChangeListener(pin)
				.tolerance(maxTolerance(3)).add();
		for (int i = valueHigh; i >= valueLow; i--) {
			link.fireStateChanged(analogPinChanged(pin, i));
		}
		assertThat(published(), is(Arrays.asList(mqttMessage.analogPin(pin)
				.hasValue(valueHigh),
				mqttMessage.analogPin(pin).hasValue(valueLow))));
	}

	@Test
	public void whenGettingLowValueMessageIsPublishedAnyhow()
			throws IOException {
		int pin = 9;
		mqttClient.configureAnalogReadChangeListener(pin)
				.tolerance(maxTolerance(25)).add();
		link.fireStateChanged(analogPinChanged(pin, 1));
		link.fireStateChanged(analogPinChanged(pin, 0));
		MqttMessageBuilder mqttBuilder = mqttMessage.analogPin(pin);
		assertThat(
				published(),
				is(Arrays.asList(mqttBuilder.hasValue(1),
						mqttBuilder.hasValue(0))));
	}

	@Test
	public void whenGettingHighValueMessageIsPublishedAnyhow()
			throws IOException {
		int pin = 9;
		mqttClient.configureAnalogReadChangeListener(pin)
				.tolerance(maxTolerance(25)).add();
		link.fireStateChanged(analogPinChanged(pin, 254));
		link.fireStateChanged(analogPinChanged(pin, 255));
		assertThat(published(), is(Arrays.asList(mqttMessage.analogPin(pin)
				.hasValue(254), mqttMessage.analogPin(pin).hasValue(255))));
	}

	// --------------------------------------------------------------------------------------------------

	@Test
	public void compactLatestWins() throws IOException {
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
	public void compactAverage() throws IOException {
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
			link.fireStateChanged(analogPinChanged(pin, value));
		}
	}

}
