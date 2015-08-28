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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zu.ardulink.ConnectionContactImpl;
import org.zu.ardulink.Link;
import org.zu.ardulink.connection.Connection;
import org.zu.ardulink.connection.ConnectionContact;

import com.github.pfichtner.ardulink.compactors.SlicedAnalogReadChangeListenerAdapter;
import com.github.pfichtner.ardulink.compactors.TimeSlicer;
import com.github.pfichtner.ardulink.util.Message;
import com.github.pfichtner.ardulink.util.MqttMessageBuilder;

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
	private final ConnectionContact connectionContact = new ConnectionContactImpl(
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
		// there is an extremely high coupling of ConnectionContactImpl and Link
		// which can not be solved other than injecting the variables through
		// reflection
		set(connectionContact, getField(connectionContact, "link"), link);
		set(link, getField(link, "connectionContact"), connectionContact);

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
					pin).valueChangedTo(i));
		}
		assertThat(published, is(Arrays.asList(mqttMessage.analogPin(pin)
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
					pin).valueChangedTo(i));
		}
		assertThat(published, is(Arrays.asList(mqttMessage.analogPin(pin)
				.hasValue(valueHigh),
				mqttMessage.analogPin(pin).hasValue(valueLow))));
	}

	@Test
	public void whenGettingLowValueMessageIsPublishedAnyhow() {
		int pin = 9;
		mqttClient.configureAnalogReadChangeListener(pin)
				.tolerance(maxTolerance(25)).add();
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.valueChangedTo(1));
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.valueChangedTo(0));
		MqttMessageBuilder mqttBuilder = mqttMessage.analogPin(pin);
		assertThat(
				published,
				is(Arrays.asList(mqttBuilder.hasValue(1),
						mqttBuilder.hasValue(0))));
	}

	@Test
	public void whenGettingHighValueMessageIsPublishedAnyhow() {
		int pin = 9;
		mqttClient.configureAnalogReadChangeListener(pin)
				.tolerance(maxTolerance(25)).add();
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.valueChangedTo(254));
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.valueChangedTo(255));
		assertThat(published, is(Arrays.asList(mqttMessage.analogPin(pin)
				.hasValue(254), mqttMessage.analogPin(pin).hasValue(255))));
	}

	// --------------------------------------------------------------------------------------------------

	@Test
	public void compactLatestWins() {
		int pin = 11;
		mqttClient.configureAnalogReadChangeListener(pin).compact(LAST_WINS, dummyTimeSlicer).add();
		fire(pin);
		dummyTimeSlicer.simulateTick();
		assertThat(
				new HashSet<Message>(published),
				is(new HashSet<Message>(Collections.singleton(mqttMessage
						.analogPin(pin).hasValue(99)))));
	}

	@Test
	public void compactAverage() {
		int pin = 11;
		mqttClient.configureAnalogReadChangeListener(pin).compact(AVERAGE, dummyTimeSlicer).add();
		fire(pin);
		dummyTimeSlicer.simulateTick();
		assertThat(
				new HashSet<Message>(published),
				is(new HashSet<Message>(Collections.singleton(mqttMessage
						.analogPin(pin).hasValue(49)))));

	}

	protected void fire(int pin) {
		for (int value = 0; value < 100; value++) {
			simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(
					pin).valueChangedTo(value));
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
