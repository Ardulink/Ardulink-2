package com.github.pfichtner.ardulink.util;

import static java.lang.String.format;

public class MqttMessageBuilder {

	public enum Type {
		SET("set"), GET("get");

		private String name;

		private Type(String name) {
			this.name = name;
		}
	}

	private final String basicTopic;
	private String subTopic;

	public static MqttMessageBuilder mqttMessageWithBasicTopic(String basicTopic) {
		return new MqttMessageBuilder(basicTopic);
	}

	private MqttMessageBuilder(String basicTopic) {
		this.basicTopic = normalize(basicTopic);
	}

	private static String normalize(String string) {
		return string.replaceAll("/$", "");
	}

	public MqttMessageBuilder digitalPin(int pin) {
		return pin("D", pin);
	}

	public MqttMessageBuilder analogPin(int pin) {
		return pin("A", pin);
	}

	private MqttMessageBuilder pin(String type, int pin) {
		return withSubTopic(type + pin);
	}

	public MqttMessageBuilder withSubTopic(String subTopic) {
		this.subTopic = subTopic;
		return this;
	}

	public Message setValue(Object value) {
		return value(Type.SET, value);
	}

	public Message hasValue(Object value) {
		return value(Type.GET, value);
	}

	protected Message value(Type type, Object value) {
		return new Message(format("%s/%s/value/%s", this.basicTopic,
				this.subTopic, type.name), mqttMessage(value));
	}

	private String mqttMessage(Object message) {
		return String.valueOf(message);
	}

}
