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

import static com.github.pfichtner.ardulink.util.MqttMessageBuilder.mqttMessageWithBasicTopic;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.ANALOG_PIN_READ;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.DIGITAL_PIN_READ;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.POWER_PIN_INTENSITY;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.POWER_PIN_SWITCH;
import static com.github.pfichtner.ardulink.util.TestUtil.createConnection;
import static com.github.pfichtner.ardulink.util.TestUtil.getField;
import static com.github.pfichtner.ardulink.util.TestUtil.listWithSameOrder;
import static com.github.pfichtner.ardulink.util.TestUtil.set;
import static com.github.pfichtner.ardulink.util.TestUtil.toCodepoints;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zu.ardulink.ConnectionContact;
import org.zu.ardulink.Link;
import org.zu.ardulink.connection.Connection;

import com.github.pfichtner.ardulink.util.Message;
import com.github.pfichtner.ardulink.util.MqttMessageBuilder;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class MqttAdapterTest {

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

	private final MqttMessageBuilder mqttMessage = mqttMessageWithBasicTopic(Config.DEFAULT_TOPIC);

	{
		// there is an extremely high coupling of ConnectionContact and Link
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
		simulateMqttToArduino(mqttMessage.digitalPin(pin).enable());
		assertThat(serialReceived(), is(alpProtocolMessage(POWER_PIN_SWITCH)
				.forPin(pin).withValue(1)));
	}

	@Test
	public void canHandleInvalidTopics() {
		simulateMqttToArduino(mqttMessage.appendTopic(
				"xxxxxxxxINVALID_TOPICxxxxxxxx").enable());
		assertThat(serialReceived(), is(empty()));
	}

	@Test
	public void canHandleInvalidBooleanPayloads() {
		int pin = 3;
		simulateMqttToArduino(mqttMessage.digitalPin(pin).setValue(
				"xxxxxxxxINVALID_VALUExxxxxxxx"));
		assertThat(serialReceived(), is(alpProtocolMessage(POWER_PIN_SWITCH)
				.forPin(pin).withValue(0)));
	}

	@Test
	public void canSetPowerAtAnalogPin() {
		int pin = 3;
		int value = 127;
		simulateMqttToArduino(mqttMessage.analogPin(pin).setValue(value));
		assertThat(serialReceived(), is(alpProtocolMessage(POWER_PIN_INTENSITY)
				.forPin(pin).withValue(value)));
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
				.withValue(value));
		assertThat(published, is(listWithSameOrder(mqttMessage.digitalPin(pin)
				.hasValue(value))));
	}

	@Test
	public void doesNotPublishDigitalPinChangesOnUnobservedPins() {
		int pin = 0;
		int value = 1;
		mqttClient.enableDigitalPinChangeEvents(pin);
		simulateArduinoToMqtt(alpProtocolMessage(DIGITAL_PIN_READ).forPin(
				anyOtherPinThan(pin)).withValue(value));
		assertThat(published, is(noMessages()));
	}

	@Test
	public void doesPublishAnalogPinChanges() {
		int pin = 9;
		int value = 123;
		mqttClient.enableAnalogPinChangeEvents(pin);
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.withValue(value));
		assertThat(published, is(listWithSameOrder(mqttMessage.analogPin(pin)
				.hasValue(value))));
	}

	@Test
	public void doesNotPublishAnalogPinChangesOnUnobservedPins() {
		int pin = 0;
		int value = 1;
		mqttClient.enableAnalogPinChangeEvents(pin);
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(
				anyOtherPinThan(pin)).withValue(value));
		assertThat(published, is(noMessages()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void doesNotAcceptNegativeDigitalPins() {
		mqttClient.enableDigitalPinChangeEvents(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void doesNotAcceptNegativeAnalogPins() {
		mqttClient.enableAnalogPinChangeEvents(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotConfigureChangeListenerOnNegativeAnalogPins() {
		mqttClient.configureAnalogReadChangeListener(-1);
	}

	private void simulateArduinoToMqtt(String message) {
		int[] codepoints = toCodepoints(message);
		connectionContact.parseInput(anyId(), codepoints.length, codepoints);
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

	private static int anyOtherPinThan(int pin) {
		return ++pin;
	}

	private static String anyId() {
		return "randomId";
	}

	private static String empty() {
		return "";
	}

	private static List<Message> noMessages() {
		return Collections.<Message> emptyList();
	}

}
