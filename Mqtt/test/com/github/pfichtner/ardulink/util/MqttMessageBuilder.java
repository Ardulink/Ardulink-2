package com.github.pfichtner.ardulink.util;

import static java.lang.String.format;

public class MqttMessageBuilder {

	private final String basicTopic;
	private int pin;
	private String type;
	private Object value;

	public static MqttMessageBuilder mqttMessageWithBasicTopic(String basicTopic) {
		return new MqttMessageBuilder(basicTopic);
	}

	private MqttMessageBuilder(String basicTopic) {
		this.basicTopic = normalize(basicTopic);
	}

	private static String normalize(String string) {
		return string.replaceAll("/$", "");
	}

	public MqttMessageBuilder forDigitalPin(int pin) {
		return forPin("D", pin);
	}

	public MqttMessageBuilder forAnalogPin(int pin) {
		return forPin("A", pin);
	}

	private MqttMessageBuilder forPin(String type, int pin) {
		this.type = type;
		this.pin = pin;
		return this;
	}

	public MqttMessageBuilder withValue(Object value) {
		this.value = value;
		return this;
	}

	public Message createGetMessage() {
		return createMessage("get");
	}

	public Message createSetMessage() {
		return createMessage("set");
	}

	private Message createMessage(String msgType) {
		return new Message(format("%s/%s%s/value/%s", basicTopic, type, pin,
				msgType), mqttMessage(value));
	}

	private String mqttMessage(Object message) {
		return String.valueOf(message);
	}

}
