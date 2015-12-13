package com.github.pfichtner.ardulink.core;

import static com.github.pfichtner.ardulink.core.Pin.Type.ANALOG;
import static com.github.pfichtner.ardulink.core.Pin.Type.DIGITAL;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pfichtner.ardulink.core.Connection.ListenerAdapter;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.events.AnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultRplyEvent;
import com.github.pfichtner.ardulink.core.events.DigitalPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocol.FromArduino;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoCustomMessage;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoKeyPressEvent;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoNoTone;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoPinEvent;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStartListening;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStopListening;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoTone;
import com.github.pfichtner.ardulink.core.proto.impl.FromArduinoPinStateChanged;
import com.github.pfichtner.ardulink.core.proto.impl.FromArduinoReply;

public class ConnectionBasedLink extends AbstractListenerLink {

	private static final Logger logger = LoggerFactory
			.getLogger(ConnectionBasedLink.class);

	private final Connection connection;
	private final Protocol protocol;

	public ConnectionBasedLink(Connection connection, Protocol protocol) {
		this.connection = connection;
		this.protocol = protocol;
		this.connection.addListener(new ListenerAdapter() {
			@Override
			public void received(byte[] bytes) throws IOException {
				ConnectionBasedLink.this.received(bytes);
			}
		});
	}

	public Connection getConnection() {
		return this.connection;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	@Override
	public void startListening(Pin pin) throws IOException {
		logger.info("Starting listening on pin {}", pin);
		ToArduinoStartListening startListeningEvent = new ToArduinoStartListening(
				pin);
		this.connection.write(this.protocol.toArduino(startListeningEvent));
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		ToArduinoStopListening stopListening = new ToArduinoStopListening(pin);
		this.connection.write(this.protocol.toArduino(stopListening));
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
		this.connection.write(this.protocol
				.toArduino(new ToArduinoKeyPressEvent(keychar, keycode,
						keylocation, keymodifiers, keymodifiersex)));
	}

	@Override
	public void sendTone(Tone tone) throws IOException {
		this.connection.write(this.protocol.toArduino(new ToArduinoTone(tone)));
	}

	@Override
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		this.connection.write(this.protocol.toArduino(new ToArduinoNoTone(
				analogPin)));
	}

	@Override
	public void sendCustomMessage(String... messages) throws IOException {
		this.connection.write(this.protocol
				.toArduino(new ToArduinoCustomMessage(messages)));
	}

	private void send(AnalogPin pin, Integer value) throws IOException {
		this.connection.write(this.protocol.toArduino(new ToArduinoPinEvent(
				pin, value)));
	}

	private void send(DigitalPin pin, Boolean value) throws IOException {
		this.connection.write(this.protocol.toArduino(new ToArduinoPinEvent(
				pin, value)));
	}

	protected void received(byte[] bytes) {
		FromArduino fromArduino = this.protocol.fromArduino(bytes);
		if (fromArduino instanceof FromArduinoPinStateChanged) {
			handlePinChanged((FromArduinoPinStateChanged) fromArduino);
		} else if (fromArduino instanceof FromArduinoReply) {
			FromArduinoReply reply = (FromArduinoReply) fromArduino;
			fireReplyReceived(new DefaultRplyEvent(reply.ok, reply.id));
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

	@Override
	public void close() throws IOException {
		deregisterAllEventListeners();
		this.connection.close();
		super.close();
	}

}
