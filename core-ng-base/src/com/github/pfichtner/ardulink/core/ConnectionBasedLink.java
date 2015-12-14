package com.github.pfichtner.ardulink.core;

import static com.github.pfichtner.ardulink.core.Pin.Type.ANALOG;
import static com.github.pfichtner.ardulink.core.Pin.Type.DIGITAL;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.events.AnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultRplyEvent;
import com.github.pfichtner.ardulink.core.events.DigitalPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocol.FromArduino;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStartListening;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStopListening;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoCustomMessage;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoKeyPressEvent;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoNoTone;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoPinEvent;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoStartListening;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoStopListening;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoTone;
import com.github.pfichtner.ardulink.core.proto.impl.FromArduinoPinStateChanged;
import com.github.pfichtner.ardulink.core.proto.impl.FromArduinoReply;

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
		getConnection().write(getProtocol().toArduino(startListeningEvent));
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		ToArduinoStopListening stopListening = new DefaultToArduinoStopListening(
				pin);
		getConnection().write(getProtocol().toArduino(stopListening));
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
		getConnection().write(
				getProtocol().toArduino(
						new DefaultToArduinoKeyPressEvent(keychar, keycode,
								keylocation, keymodifiers, keymodifiersex)));
	}

	@Override
	public void sendTone(Tone tone) throws IOException {
		getConnection().write(
				getProtocol().toArduino(new DefaultToArduinoTone(tone)));
	}

	@Override
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		getConnection().write(
				getProtocol().toArduino(new DefaultToArduinoNoTone(analogPin)));
	}

	@Override
	public void sendCustomMessage(String... messages) throws IOException {
		getConnection().write(
				getProtocol().toArduino(
						new DefaultToArduinoCustomMessage(messages)));
	}

	private void send(AnalogPin pin, int value) throws IOException {
		getConnection().write(
				getProtocol().toArduino(
						new DefaultToArduinoPinEvent(pin, value)));
	}

	private void send(DigitalPin pin, boolean value) throws IOException {
		getConnection().write(
				getProtocol().toArduino(
						new DefaultToArduinoPinEvent(pin, value)));
	}

	protected void received(byte[] bytes) {
		FromArduino fromArduino = getProtocol().fromArduino(bytes);
		if (fromArduino instanceof FromArduinoPinStateChanged) {
			handlePinChanged((FromArduinoPinStateChanged) fromArduino);
		} else if (fromArduino instanceof FromArduinoReply) {
			FromArduinoReply reply = (FromArduinoReply) fromArduino;
			fireReplyReceived(new DefaultRplyEvent(reply.isOk(), reply.getId()));
		} else {
			throw new IllegalStateException("Cannot handle " + fromArduino);
		}
	}

	private void handlePinChanged(FromArduinoPinStateChanged pinChanged) {
		Pin pin = pinChanged.getPin();
		Object value = pinChanged.getValue();
		if (pin.is(ANALOG) && value instanceof Integer) {
			AnalogPinValueChangedEvent event = new DefaultAnalogPinValueChangedEvent(
					(AnalogPin) pin, (Integer) value);
			fireStateChanged(event);
		} else if (pin.is(DIGITAL) && value instanceof Boolean) {
			DigitalPinValueChangedEvent event = new DefaultDigitalPinValueChangedEvent(
					(DigitalPin) pin, (Boolean) value);
			fireStateChanged(event);
		} else {
			throw new IllegalStateException(
					"Cannot handle pin change event for pin " + pin
							+ " with value " + value);
		}
	}

}
