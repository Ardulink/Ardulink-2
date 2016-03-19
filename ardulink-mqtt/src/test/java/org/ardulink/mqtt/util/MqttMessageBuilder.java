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
package org.ardulink.mqtt.util;

import static java.lang.String.format;
import static org.ardulink.util.Preconditions.checkArgument;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttMessageBuilder {

	public enum Type {
		SET("set"), GET("get");

		private final String name;

		private Type(String name) {
			this.name = name;
		}
	}

	private final StringBuilder topic;

	public static MqttMessageBuilder mqttMessageWithBasicTopic(String basicTopic) {
		return new MqttMessageBuilder(basicTopic);
	}

	private MqttMessageBuilder(String basicTopic) {
		this.topic = new StringBuilder(normalize(basicTopic));
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
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
		return appendTopic(type + pin);
	}

	public MqttMessageBuilder appendTopic(String subTopic) {
		return new MqttMessageBuilder(topic.toString() + '/' + subTopic);
	}

	public MqttMessageBuilder digitalListener(int pin) {
		return listener().digitalPin(pin);
	}

	public MqttMessageBuilder analogListener(int pin) {
		return listener().analogPin(pin);
	}

	public MqttMessageBuilder listener() {
		return appendTopic("system/listening/");
	}

	public Message enable() {
		return setValue(true);
	}

	public Message disable() {
		return setValue(false);
	}

	public Message setValue(Object value) {
		return value(Type.SET, value);
	}

	public Message hasValue(Object value) {
		return value(Type.GET, value);
	}

	public Message hasState(boolean state) {
		return value(Type.GET, state);
	}

	protected Message value(Type type, Object value) {
		return new Message(format("%s/value/%s", this.topic, type.name),
				mqttMessage(value));
	}

	private String mqttMessage(Object message) {
		return String.valueOf(message);
	}

}
