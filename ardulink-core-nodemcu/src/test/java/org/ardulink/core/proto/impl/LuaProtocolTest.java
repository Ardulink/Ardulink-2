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

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageCustom.toDeviceMessageCustom;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessagePinStateChange.toDeviceMessagePinStateChange;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageStartListening.toDeviceMessageStartListening;
import static org.ardulink.core.proto.api.Protocols.protoByName;
import static org.assertj.core.api.Assertions.assertThat;

import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.util.Joiner;
import org.junit.jupiter.api.Test;

class LuaProtocolTest {

	ByteStreamProcessor sut = protoByName(LuaProtocol.NAME).newByteStreamProcessor();

	DigitalPin anyDigitalPin = digitalPin(anyPin());
	AnalogPin anyAnalogPin = analogPin(anyPin());
	int anyValue = anyValue(int.class);

	@Test
	void generatePowerPinSwitchMessageHigh() {
		ToDeviceMessagePinStateChange msg = toDeviceMessagePinStateChange(anyDigitalPin, true);
		assertThat(stringOf(sut.toDevice(msg))).isEqualTo(lua(powerPinMessage(anyDigitalPin.pinNum(), "HIGH")));
	}

	@Test
	void generatePowerPinSwitchMessageLow() {
		ToDeviceMessagePinStateChange msg = toDeviceMessagePinStateChange(anyDigitalPin, false);
		assertThat(stringOf(sut.toDevice(msg))).isEqualTo(lua(powerPinMessage(anyDigitalPin.pinNum(), "LOW")));
	}

	@Test
	void generatePowerPinIntensityMessage() {
		ToDeviceMessagePinStateChange msg = toDeviceMessagePinStateChange(anyAnalogPin, anyValue);
		assertThat(stringOf(sut.toDevice(msg))).isEqualTo(lua(pinStateChangeMessage(anyAnalogPin.pinNum(), anyValue)));
	}

	@Test
	void generateCustomMessage() {
		String[] values = new String[] { "param1", "somethingelse2", "final3" };
		ToDeviceMessageCustom msg = toDeviceMessageCustom(values);
		assertThat(stringOf(sut.toDevice(msg))).isEqualTo(lua(customMessage(values)));
	}

	@Test
	void generateStartListeningDigitalMessage() {
		DigitalPin pin = digitalPin(anyPin());
		ToDeviceMessageStartListening msg = toDeviceMessageStartListening(pin);
		assertThat(stringOf(sut.toDevice(msg))).contains("alp://dred/" + pin.pinNum() + "/%s");
	}

	private String stringOf(byte[] bytes) {
		return new String(bytes);
	}

	private static String lua(String msg) {
		return msg + "\r\n";
	}

	private static String powerPinMessage(int pin, String state) {
		return String.format("gpio.mode(%s,gpio.OUTPUT) gpio.write(%s,gpio.%s)", pin, pin, state);
	}

	private static String pinStateChangeMessage(int pin, int value) {
		return String.format("pwm.setup(%s,1000,1023) pwm.start(%s) pwm.setduty(%s,%s)", pin, pin, pin, value);
	}

	private String customMessage(String... values) {
		return Joiner.on(" ").join(values);
	}

	private int anyPin() {
		return 42;
	}

	private int anyValue(Class<?> type) {
		return 1023;
	}

}
