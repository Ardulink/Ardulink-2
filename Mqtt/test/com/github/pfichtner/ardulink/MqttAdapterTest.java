package com.github.pfichtner.ardulink;

import static com.github.pfichtner.ardulink.AbstractMqttAdapter.Tolerance.maxTolerance;
import static com.github.pfichtner.ardulink.util.MqttMessageBuilder.mqttMessageWithBasicTopic;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.ANALOG_PIN_READ;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.DIGITAL_PIN_READ;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.POWER_PIN_INTENSITY;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.POWER_PIN_SWITCH;
import static com.github.pfichtner.ardulink.util.TestUtil.createConnection;
import static com.github.pfichtner.ardulink.util.TestUtil.getField;
import static com.github.pfichtner.ardulink.util.TestUtil.set;
import static com.github.pfichtner.ardulink.util.TestUtil.toCodepoints;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zu.ardulink.ConnectionContactImpl;
import org.zu.ardulink.Link;
import org.zu.ardulink.connection.Connection;
import org.zu.ardulink.connection.ConnectionContact;

import com.github.pfichtner.ardulink.util.Message;
import com.github.pfichtner.ardulink.util.MqttMessageBuilder;

public class MqttAdapterTest {

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
	public void canPowerOnDigitalPin() {
		int pin = 0;
		simulateMqttToArduino(mqttMessage.digitalPin(pin).setValue(true));
		assertThat(serialReceived(), is(alpProtocolMessage(POWER_PIN_SWITCH)
				.forPin(pin).setValue(1)));
	}

	@Test
	public void canHandleInvalidTopics() {
		simulateMqttToArduino(mqttMessage.withSubTopic(
				"xxxxxxxxINVALID_TOPICxxxxxxxx").setValue(true));
		assertThat(serialReceived(), is(empty()));
	}

	@Test
	public void canHandleInvalidBooleanPayloads() {
		int pin = 3;
		simulateMqttToArduino(mqttMessage.digitalPin(pin).setValue(
				"xxxxxxxxINVALID_VALUExxxxxxxx"));
		assertThat(serialReceived(), is(alpProtocolMessage(POWER_PIN_SWITCH)
				.forPin(pin).setValue(0)));
	}

	@Test
	public void canSetPowerAtAnalogPin() {
		int pin = 3;
		int value = 127;
		simulateMqttToArduino(mqttMessage.analogPin(pin).setValue(value));
		assertThat(serialReceived(), is(alpProtocolMessage(POWER_PIN_INTENSITY)
				.forPin(pin).setValue(value)));
	}

	@Test
	public void canHandleInvalidDigitalPayloads() {
		int pin = 3;
		String value = "NaN";
		simulateMqttToArduino(mqttMessage.analogPin(pin).setValue(value));
		assertThat(serialReceived(), is(empty()));
	}

	@Test
	public void doesPublishDigitalPinChanges() {
		int pin = 0;
		int value = 1;
		mqttClient.enableDigitalPinChangeEvents(pin);
		simulateArduinoToMqtt(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin)
				.valueChangedTo(value));
		assertThat(published, is(singletonList(mqttMessage.digitalPin(pin)
				.hasValue(value))));
	}

	@Test
	public void doesNotPublishDigitalPinChangesOnUnobservedPins() {
		int pin = 0;
		int value = 1;
		mqttClient.enableDigitalPinChangeEvents(pin);
		simulateArduinoToMqtt(alpProtocolMessage(DIGITAL_PIN_READ).forPin(
				anyOtherPinThan(pin)).valueChangedTo(value));
		assertThat(published, is(Collections.<Message> emptyList()));
	}

	@Test
	public void doesPublishAnalogPinChanges() {
		int pin = 9;
		int value = 123;
		mqttClient.enableAnalogPinChangeEvents(pin);
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.valueChangedTo(value));
		assertThat(published, is(singletonList(mqttMessage.analogPin(pin)
				.hasValue((Object) value))));
	}

	@Test
	public void doesNotPublishAnalogPinChangesOnUnobservedPins() {
		int pin = 0;
		int value = 1;
		mqttClient.enableAnalogPinChangeEvents(pin);
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(
				anyOtherPinThan(pin)).valueChangedTo(value));
		assertThat(published, is(Collections.<Message> emptyList()));
	}

	// ----------------------------------------------------------------------------

	@Test
	public void doesNotPublishPinChangesLowerThanToleranceValueWhenIncreasing() {
		int pin = 9;
		int valueLow = 123;
		int valueHigh = 127;
		mqttClient.enableAnalogPinChangeEvents(pin, maxTolerance(3));
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
		mqttClient.enableAnalogPinChangeEvents(pin, maxTolerance(3));
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
		mqttClient.enableAnalogPinChangeEvents(pin, maxTolerance(25));
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
		mqttClient.enableAnalogPinChangeEvents(pin, maxTolerance(25));
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.valueChangedTo(254));
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.valueChangedTo(255));
		assertThat(published, is(Arrays.asList(mqttMessage.analogPin(pin)
				.hasValue(254), mqttMessage.analogPin(pin).hasValue(255))));
	}

	private int anyOtherPinThan(int pin) {
		return ++pin;
	}

	private void simulateArduinoToMqtt(String message) {
		int[] codepoints = toCodepoints(message);
		connectionContact.parseInput(anyId(), codepoints.length, codepoints);
	}

	private String anyId() {
		return "randomId";
	}

	private void simulateMqttToArduino(Message message) {
		mqttClient.toArduino(message.getTopic(), message.getMessage());
	}

	private String serialReceived() {
		try {
			outputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new String(outputStream.toByteArray());
	}

	private String empty() {
		return "";
	}

}
