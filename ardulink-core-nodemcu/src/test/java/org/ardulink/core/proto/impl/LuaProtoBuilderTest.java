package org.ardulink.core.proto.impl;

import static org.ardulink.core.Pin.digitalPin;

import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.impl.DefaultToDeviceMessagePinStateChange;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class LuaProtoBuilderTest {
	
	@Test
	public void generatePowerPinSwitchMessage() {
		LuaProtocol protocol = new LuaProtocol();
		
		ToDeviceMessagePinStateChange message = new DefaultToDeviceMessagePinStateChange(digitalPin(1), true);
		
		byte[] protMessage = protocol.toDevice(message);
		String controlString = "gpio.mode(1, gpio.OUTPUT);gpio.write(1, gpio.HIGH);\r\n";
		
		assertThat(new String(protMessage), equalTo(controlString));

		message = new DefaultToDeviceMessagePinStateChange(digitalPin(2), false);
		
		protMessage = protocol.toDevice(message);
		controlString = "gpio.mode(2, gpio.OUTPUT);gpio.write(2, gpio.LOW);\r\n";
		
		assertThat(new String(protMessage), equalTo(controlString));
	}


}
