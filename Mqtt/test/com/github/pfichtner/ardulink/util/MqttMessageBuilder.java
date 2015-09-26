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
package com.github.pfichtner.ardulink.util;

import static com.github.pfichtner.ardulink.util.Preconditions.checkArgument;
import static java.lang.String.format;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class MqttMessageBuilder {

	public enum Type {
		SET("set"), GET("get");

		private final String name;

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
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
		return pin("D", pin);
	}

	public MqttMessageBuilder analogPin(int pin) {
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
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
