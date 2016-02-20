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

import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static com.github.pfichtner.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_SWITCH;
import static com.github.pfichtner.ardulink.util.MqttMessageBuilder.mqttMessageWithBasicTopic;
import static com.github.pfichtner.ardulink.util.TestUtil.analogPinChanged;
import static com.github.pfichtner.ardulink.util.TestUtil.digitalPinChanged;
import static com.github.pfichtner.ardulink.util.TestUtil.listWithSameOrder;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
public class MqttAdapterTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

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

	private final MqttMessageBuilder mqttMessage = mqttMessageWithBasicTopic(Config.DEFAULT_TOPIC);

	@After
	public void tearDown() throws IOException {
		link.close();
	}

	@Test
	public void canPowerOnDigitalPin() throws IOException {
		int pin = 0;
		simulateMqttToArduino(mqttMessage.digitalPin(pin).enable());
		assertThat(serialReceived(), is(alpProtocolMessage(POWER_PIN_SWITCH)
				.forPin(pin).withValue(1) + "\n"));
	}

	@Test
	public void canHandleInvalidTopics() throws IOException {
		simulateMqttToArduino(mqttMessage.appendTopic(
				"xxxxxxxxINVALID_TOPICxxxxxxxx").enable());
		assertThat(serialReceived(), is(empty()));
	}

	@Test
	public void canHandleInvalidBooleanPayloads() throws IOException {
		int pin = 3;
		simulateMqttToArduino(mqttMessage.digitalPin(pin).setValue(
				"xxxxxxxxINVALID_VALUExxxxxxxx"));
		assertThat(serialReceived(), is(alpProtocolMessage(POWER_PIN_SWITCH)
				.forPin(pin).withValue(0) + "\n"));
	}

	@Test
	public void canSetPowerAtAnalogPin() throws IOException {
		int pin = 3;
		int value = 127;
		simulateMqttToArduino(mqttMessage.analogPin(pin).setValue(value));
		assertThat(serialReceived(), is(alpProtocolMessage(POWER_PIN_INTENSITY)
				.forPin(pin).withValue(value) + "\n"));
	}

	@Test
	public void canHandleInvalidDigitalPayloads() throws IOException {
		int pin = 3;
		String value = "NaN";
		simulateMqttToArduino(mqttMessage.analogPin(pin).setValue(value));
		assertThat(serialReceived(), is(empty()));
	}

	@Test
	public void doesPublishDigitalPinChanges() throws IOException {
		int pin = 0;
		boolean value = true;
		this.mqttClient.enableDigitalPinChangeEvents(pin);
		this.link.fireStateChanged(digitalPinChanged(pin, value));
		assertThat(published, is(listWithSameOrder(mqttMessage.digitalPin(pin)
				.hasValue(value))));
	}

	@Test
	public void doesNotPublishDigitalPinChangesOnUnobservedPins()
			throws IOException {
		int pin = 0;
		boolean value = true;
		this.mqttClient.enableDigitalPinChangeEvents(pin);
		this.link.fireStateChanged(digitalPinChanged(anyOtherPinThan(pin),
				value));
		assertThat(published, is(noMessages()));
	}

	@Test
	public void doesPublishAnalogPinChanges() throws IOException {
		int pin = 9;
		int value = 123;
		this.mqttClient.enableAnalogPinChangeEvents(pin);
		link.fireStateChanged(analogPinChanged(pin, value));
		assertThat(published, is(listWithSameOrder(mqttMessage.analogPin(pin)
				.hasValue(value))));
	}

	@Test
	public void doesNotPublishAnalogPinChangesOnUnobservedPins()
			throws IOException {
		int pin = 0;
		int value = 1;
		mqttClient.enableAnalogPinChangeEvents(pin);
		link.fireStateChanged(analogPinChanged(anyOtherPinThan(pin), value));
		assertThat(published, is(noMessages()));
	}

	@Test
	public void doesNotAcceptNegativeDigitalPins() throws IOException {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(pinMustNotBeNegativeButWas(-1));
		mqttClient.enableDigitalPinChangeEvents(-1);
	}

	@Test
	public void doesNotAcceptNegativeAnalogPins() throws IOException {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(pinMustNotBeNegativeButWas(-1));
		mqttClient.enableAnalogPinChangeEvents(-1);
	}

	@Test
	public void cannotConfigureChangeListenerOnNegativeAnalogPins() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(pinMustNotBeNegativeButWas(-1));
		mqttClient.configureAnalogReadChangeListener(-1);
	}

	private Matcher<String> pinMustNotBeNegativeButWas(int pin) {
		return allOf(containsString("Pin"), containsString("negative"),
				containsString(String.valueOf(pin)));
	}

	@Test
	public void ignoresConfigsWithWrongGroupCountInRegep() throws IOException {
		Config defaultConfig = Config.DEFAULT;
		Config config = defaultConfig.withTopicPatternAnalogWrite(Pattern
				.compile(removeAll(defaultConfig.getTopicPatternAnalogWrite()
						.pattern(), "()")));
		mqttClient = new AbstractMqttAdapter(link, config) {
			@Override
			void fromArduino(String topic, String message) {
				published.add(new Message(topic, message));
			}
		};
		simulateMqttToArduino(mqttMessage.analogPin(3).setValue("123"));
		assertThat(serialReceived(), is(empty()));
	}

	private String removeAll(String string, String remove) {
		return string.replaceAll("[" + remove + "]", "");
	}

	private void simulateMqttToArduino(Message message) throws IOException {
		this.mqttClient.toArduino(message.getTopic(), message.getMessage());
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

	private static String empty() {
		return "";
	}

	private static List<Message> noMessages() {
		return Collections.<Message> emptyList();
	}

}
