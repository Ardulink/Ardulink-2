package com.github.pfichtner.ardulink.core;

import static com.github.pfichtner.ardulink.core.Pins.isAnalog;
import static com.github.pfichtner.ardulink.core.Pins.isDigital;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pfichtner.ardulink.core.Connection.ListenerAdapter;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.events.AnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DigitalPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.EventListener;
import com.github.pfichtner.ardulink.core.events.FilteredEventListenerAdapter;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocol.FromArduino;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoCharEvent;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoPinEvent;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStartListening;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoStopListening;

public class ConnectionBasedLink implements Link {

	private static final Logger logger = LoggerFactory
			.getLogger(ConnectionBasedLink.class);

	private final Connection connection;
	private final Protocol protocol;
	private final List<EventListener> eventListeners = new CopyOnWriteArrayList<EventListener>();

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

	@Override
	public Link addListener(EventListener listener) throws IOException {
		this.eventListeners.add(listener);
		if (listener instanceof FilteredEventListenerAdapter) {
			startListening(((FilteredEventListenerAdapter) listener).getPin());
		}
		return this;
	}

	@Override
	public Link removeListener(EventListener listener) throws IOException {
		this.eventListeners.remove(listener);
		if (listener instanceof FilteredEventListenerAdapter) {
			Pin pin = ((FilteredEventListenerAdapter) listener).getPin();
			if (!listenerLeftFor(pin)) {
				stopListening(pin);
			}
		}
		return this;
	}

	@Override
	public void startListening(Pin pin) throws IOException {
		ToArduinoStartListening startListeningEvent = new ToArduinoStartListening(
				pin);
		this.connection.write(this.protocol.toArduino(startListeningEvent));
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		ToArduinoStopListening stopListening = new ToArduinoStopListening(pin);
		this.connection.write(this.protocol.toArduino(stopListening));
	}

	private boolean listenerLeftFor(Pin pin) {
		for (EventListener listener : this.eventListeners) {
			if (listener instanceof FilteredEventListenerAdapter
					&& pin.equals(((FilteredEventListenerAdapter) listener)
							.getPin())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void switchAnalogPin(AnalogPin analogPin, Integer value)
			throws IOException {
		send(analogPin, value);
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, Boolean value)
			throws IOException {
		send(digitalPin, value);
	}

	@Override
	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		this.connection.write(this.protocol.toArduino(new ToArduinoCharEvent(
				keychar, keycode, keylocation, keymodifiers, keymodifiersex)));
	}

	private void send(Pin pin, Object value) throws IOException {
		this.connection.write(this.protocol.toArduino(new ToArduinoPinEvent(
				pin, value)));
	}

	protected void received(byte[] bytes) {
		FromArduino fromArduino = this.protocol.fromArduino(bytes);
		Pin pin = fromArduino.getPin();
		Object value = fromArduino.getValue();
		if (isAnalog(pin) && value instanceof Integer) {
			AnalogPinValueChangedEvent event = new DefaultAnalogPinValueChangedEvent(
					(AnalogPin) pin, (Integer) value);
			for (EventListener eventListener : this.eventListeners) {
				try {
					eventListener.stateChanged(event);
				} catch (Exception e) {
					logger.error("EventListener {} failure", eventListener, e);
				}
			}
		}
		if (isDigital(pin) && value instanceof Boolean) {
			DigitalPinValueChangedEvent event = new DefaultDigitalPinValueChangedEvent(
					(DigitalPin) pin, (Boolean) value);
			for (EventListener eventListener : this.eventListeners) {
				try {
					eventListener.stateChanged(event);
				} catch (Exception e) {
					logger.error("EventListener {} failure", eventListener, e);
				}
			}
		}
	}

	@Override
	public void close() throws IOException {
		this.connection.close();
	}

}
