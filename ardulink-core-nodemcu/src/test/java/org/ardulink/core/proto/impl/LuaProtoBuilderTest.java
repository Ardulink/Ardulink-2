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

import static java.util.Arrays.asList;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;

import java.util.Random;

import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageCustom;
import org.ardulink.core.messages.impl.DefaultToDeviceMessagePinStateChange;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageStartListening;
import org.ardulink.util.Joiner;
import org.junit.Test;

public class LuaProtoBuilderTest {

	private final LuaProtocol protocol = new LuaProtocol();

	@Test
	public void generatePowerPinSwitchMessageHigh() {
		powerPinSwitchMessage(true);
	}

	@Test
	public void generatePowerPinSwitchMessageLow() {
		powerPinSwitchMessage(false);
	}

	private void powerPinSwitchMessage(boolean state) {
		int pin = anyPin();
		DefaultToDeviceMessagePinStateChange message = new DefaultToDeviceMessagePinStateChange(
				digitalPin(pin), state);
		byte[] protMessage = protocol.toDevice(message);
		String stateString = state ? "HIGH" : "LOW";
		assertThat(new String(protMessage), equalTo("gpio.mode(" + pin
				+ ",gpio.OUTPUT) gpio.write(" + pin + ",gpio." + stateString
				+ ")\r\n"));
	}

	@Test
	public void generatePowerPinIntensityMessage() {
		int pin = anyPin();
		int value = anyValue();
		ToDeviceMessagePinStateChange message = new DefaultToDeviceMessagePinStateChange(
				analogPin(pin), value);
		byte[] protMessage = protocol.toDevice(message);
		assertThat(new String(protMessage), equalTo("pwm.setup(" + pin
				+ ",1000,1023) pwm.start(" + pin + ") pwm.setduty(" + pin + ","
				+ value + ")\r\n"));
	}

	@Test
	public void generateCustomMessage() {
		String p1 = "param1";
		String p2 = "somethingelse2";
		String p3 = "final3";
		ToDeviceMessageCustom message = new DefaultToDeviceMessageCustom(p1,
				p2, p3);
		byte[] protMessage = protocol.toDevice(message);
		String expected = Joiner.on(" ").join(asList(p1, p2, p3)) + "\r\n";
		assertThat(new String(protMessage), equalTo(expected));
	}

	@Test
	public void generateStartListeningDigitalMessage() {
		int pin = anyPin();
		ToDeviceMessageStartListening message = new DefaultToDeviceMessageStartListening(
				digitalPin(pin));
		byte[] protMessage = protocol.toDevice(message);
		assertThat(new String(protMessage), containsString("alp://dred/" + pin
				+ "/%s"));
	}

	private int anyPin() {
		return new Random().nextInt(99);
	}

	private int anyValue() {
		return new Random().nextInt(1023);
	}
}
