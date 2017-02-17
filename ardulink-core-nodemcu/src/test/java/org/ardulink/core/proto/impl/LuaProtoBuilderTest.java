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
	
	@Test
	public void generatePowerPinSwitchMessage() {
		LuaProtocol protocol = new LuaProtocol();
		
		ToDeviceMessagePinStateChange message = new DefaultToDeviceMessagePinStateChange(digitalPin(1), true);
		
		byte[] protMessage = protocol.toDevice(message);
		String controlString = "gpio.mode(1,gpio.OUTPUT) gpio.write(1,gpio.HIGH)\r\n";
		
		assertThat(new String(protMessage), equalTo(controlString));

		message = new DefaultToDeviceMessagePinStateChange(digitalPin(2), false);
		
		protMessage = protocol.toDevice(message);
		controlString = "gpio.mode(2,gpio.OUTPUT) gpio.write(2,gpio.LOW)\r\n";
		
		assertThat(new String(protMessage), equalTo(controlString));
	}

	@Test
	public void generatePowerPinIntensityMessage() {
		LuaProtocol protocol = new LuaProtocol();
		
		ToDeviceMessagePinStateChange message = new DefaultToDeviceMessagePinStateChange(analogPin(1), 123);
		
		byte[] protMessage = protocol.toDevice(message);
		String controlString = "pwm.setup(1,1000,1023) pwm.start(1) pwm.setduty(1,123)\r\n";
		
		assertThat(new String(protMessage), equalTo(controlString));
	}
	
	@Test
	public void generateCustomMessage() {
		LuaProtocol protocol = new LuaProtocol();
		
		ToDeviceMessageCustom message = new DefaultToDeviceMessageCustom("param1", "somethingelse2", "final3");
		
		byte[] protMessage = protocol.toDevice(message);
		String controlString = "param1 somethingelse2 final3\r\n";
		
		assertThat(new String(protMessage), equalTo(controlString));
	}

	@Test
	public void generateStartListeningDigitalMessage() {
		LuaProtocol protocol = new LuaProtocol();
		
		ToDeviceMessageStartListening message = new DefaultToDeviceMessageStartListening(digitalPin(1));
		
		byte[] protMessage = protocol.toDevice(message);
		String controlString = "alp://dred/1/%s";
		
		assertThat(new String(protMessage), containsString(controlString));
	}
}
