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
package org.ardulink.core.proto.impl;

import static java.util.Collections.addAll;
import static org.ardulink.util.Preconditions.checkArgument;

import java.util.List;

import org.ardulink.util.Joiner;
import org.ardulink.util.Lists;
import org.ardulink.util.Optional;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ALProtoBuilder {

	private static final Joiner joiner = Joiner.on("/");

	private Long messageId;
	private final String command;
	private Object pin;

	public enum ALPProtocolKey {

		POWER_PIN_SWITCH("ppsw"), POWER_PIN_INTENSITY("ppin"), DIGITAL_PIN_READ(
				"dred"), ANALOG_PIN_READ("ared"), START_LISTENING_DIGITAL(
				"srld"), START_LISTENING_ANALOG("srla"), STOP_LISTENING_DIGITAL(
				"spld"), STOP_LISTENING_ANALOG("spla"), CHAR_PRESSED("kprs"), TONE(
				"tone"), NOTONE("notn"), CUSTOM_MESSAGE("cust"), RPLY("rply"), READY(
				"ready"), CUSTOM_EVENT("cevnt");

		private String proto;

		private ALPProtocolKey(String proto) {
			this.proto = proto;
		}

		public static Optional<ALPProtocolKey> fromString(String string) {
			for (ALPProtocolKey alpProtocolKeys : values()) {
				if (alpProtocolKeys.proto.equals(string)) {
					return Optional.of(alpProtocolKeys);
				}
			}
			return Optional.absent();
		}
	}

	public static ALProtoBuilder alpProtocolMessage(ALPProtocolKey command) {
		return new ALProtoBuilder(command.proto);
	}

	public static ALProtoBuilder arduinoCommand(String command) {
		return new ALProtoBuilder(command);
	}

	private ALProtoBuilder(String command) {
		this.command = command;
	}

	public String withoutValue() {
		return withValues();
	}

	public String withValue(Object value) {
		return withValues(String.valueOf(value));
	}

	public String withValues(String... values) {
		List<Object> concat = Lists.<Object> newArrayList(command);
		if (pin != null) {
			concat.add(pin);
		}
		addAll(concat, values);
		String message = "alp://" + joiner.join(concat);
		return messageId == null ? message : message + "?id="
				+ messageId.longValue();
	}

	public String withState(boolean value) {
		return withValue(value ? 1 : 0);
	}

	public ALProtoBuilder forPin(int pin) {
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
		this.pin = pin;
		return this;
	}

	public ALProtoBuilder forChar(char ch) {
		this.pin = ch;
		return this;
	}

	public ALProtoBuilder usingMessageId(Long messageId) {
		this.messageId = messageId;
		return this;
	}

}
