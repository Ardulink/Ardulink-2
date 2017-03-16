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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageCustom;
import org.ardulink.core.messages.impl.DefaultToDeviceMessagePinStateChange;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageStartListening;
import org.junit.Test;

public class LuaProtoBuilderTest {

	private final LuaProtocol protocol = new LuaProtocol();

	@Test
	public void generatePowerPinSwitchMessageHigh() {
		ToDeviceMessagePinStateChange message = new DefaultToDeviceMessagePinStateChange(
				digitalPin(1), true);
		byte[] protMessage = protocol.toDevice(message);
		assertThat(new String(protMessage),
				equalTo("gpio.mode(1,gpio.OUTPUT) gpio.write(1,gpio.HIGH)\r\n"));
	}

	@Test
	public void generatePowerPinSwitchMessageLow() {
		DefaultToDeviceMessagePinStateChange message = new DefaultToDeviceMessagePinStateChange(
				digitalPin(2), false);
		byte[] protMessage = protocol.toDevice(message);
		assertThat(new String(protMessage),
				equalTo("gpio.mode(2,gpio.OUTPUT) gpio.write(2,gpio.LOW)\r\n"));
	}

	@Test
	public void generatePowerPinIntensityMessage() {
		ToDeviceMessagePinStateChange message = new DefaultToDeviceMessagePinStateChange(
				analogPin(1), 123);
		byte[] protMessage = protocol.toDevice(message);
		assertThat(
				new String(protMessage),
				equalTo("pwm.setup(1,1000,1023) pwm.start(1) pwm.setduty(1,123)\r\n"));
	}

	@Test
	public void generateCustomMessage() {
		ToDeviceMessageCustom message = new DefaultToDeviceMessageCustom(
				"param1", "somethingelse2", "final3");
		byte[] protMessage = protocol.toDevice(message);
		assertThat(new String(protMessage),
				equalTo("param1 somethingelse2 final3\r\n"));
	}

	@Test
	public void generateStartListeningDigitalMessage() {
		ToDeviceMessageStartListening message = new DefaultToDeviceMessageStartListening(
				digitalPin(1));
		byte[] protMessage = protocol.toDevice(message);
		assertThat(new String(protMessage), containsString("alp://dred/1/%s"));
	}

}
