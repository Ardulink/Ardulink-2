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

import static org.zu.ardulink.util.Preconditions.checkArgument;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public class ProtoBuilder {

	private final String command;
	private int pin;

	public enum ALPProtocolKeys {

		POWER_PIN_SWITCH("ppsw"), POWER_PIN_INTENSITY("ppin"), DIGITAL_PIN_READ(
				"dred"), ANALOG_PIN_READ("ared"), START_LISTENING_DIGITAL(
				"srld"), START_LISTENING_ANALOG("srla"), STOP_LISTENING_DIGITAL(
				"spld"), STOP_LISTENING_ANALOG("spla");

		private String proto;

		private ALPProtocolKeys(String proto) {
			this.proto = proto;
		}
	}

	public static ProtoBuilder alpProtocolMessage(ALPProtocolKeys command) {
		return new ProtoBuilder(command.proto);
	}

	public static ProtoBuilder arduinoCommand(String command) {
		return new ProtoBuilder(command);
	}

	private ProtoBuilder(String command) {
		this.command = command;
	}

	public String withoutValue() {
		return "alp://" + command + "/" + pin + "\n";
	}

	public String withValue(int value) {
		return "alp://" + command + "/" + pin + "/" + value + "\n";
	}

	public ProtoBuilder forPin(int pin) {
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
		this.pin = pin;
		return this;
	}

}
