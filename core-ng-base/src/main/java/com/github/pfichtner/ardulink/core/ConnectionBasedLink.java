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

package com.github.pfichtner.ardulink.core;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStartListening;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStopListening;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoCustomMessage;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoKeyPressEvent;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoNoTone;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoPinEvent;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoStartListening;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoStopListening;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoTone;

public class ConnectionBasedLink extends AbstractConnectionBasedLink {

	private static final Logger logger = LoggerFactory
			.getLogger(ConnectionBasedLink.class);

	public ConnectionBasedLink(Connection connection, Protocol protocol) {
		super(connection, protocol);
	}

	@Override
	public void startListening(Pin pin) throws IOException {
		logger.info("Starting listening on pin {}", pin);
		ToArduinoStartListening startListeningEvent = new DefaultToArduinoStartListening(
				pin);
		send(getProtocol().toArduino(startListeningEvent));
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		ToArduinoStopListening stopListening = new DefaultToArduinoStopListening(
				pin);
		send(getProtocol().toArduino(stopListening));
		logger.info("Stopped listening on pin {}", pin);
	}

	@Override
	public void switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		send(analogPin, value);
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		send(digitalPin, value);
	}

	@Override
	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		send(getProtocol().toArduino(
				new DefaultToArduinoKeyPressEvent(keychar, keycode,
						keylocation, keymodifiers, keymodifiersex)));
	}

	@Override
	public void sendTone(Tone tone) throws IOException {
		send(getProtocol().toArduino(new DefaultToArduinoTone(tone)));
	}

	@Override
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		send(getProtocol().toArduino(new DefaultToArduinoNoTone(analogPin)));
	}

	@Override
	public void sendCustomMessage(String... messages) throws IOException {
		send(getProtocol().toArduino(
				new DefaultToArduinoCustomMessage(messages)));
	}

	private void send(AnalogPin pin, int value) throws IOException {
		send(getProtocol().toArduino(new DefaultToArduinoPinEvent(pin, value)));
	}

	private void send(DigitalPin pin, boolean value) throws IOException {
		send(getProtocol().toArduino(new DefaultToArduinoPinEvent(pin, value)));
	}

	private void send(byte[] bytes) throws IOException {
		getConnection().write(bytes);
	}

}
