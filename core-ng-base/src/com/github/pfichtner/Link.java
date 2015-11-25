package com.github.pfichtner;

import static com.github.pfichtner.Pins.isAnalog;
import static com.github.pfichtner.Pins.isDigital;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pfichtner.Connection.ListenerAdapter;
import com.github.pfichtner.Pin.AnalogPin;
import com.github.pfichtner.Pin.DigitalPin;
import com.github.pfichtner.events.AnalogPinValueChangedEvent;
import com.github.pfichtner.events.DefaultAnalogPinValueChangedEvent;
import com.github.pfichtner.events.DefaultDigitalPinValueChangedEvent;
import com.github.pfichtner.events.DigitalPinValueChangedEvent;
import com.github.pfichtner.events.EventListener;
import com.github.pfichtner.events.FilteredEventListenerAdapter;
import com.github.pfichtner.proto.api.Protocol;
import com.github.pfichtner.proto.api.Protocol.FromArduino;
import com.github.pfichtner.proto.api.ToArduinoCharEvent;
import com.github.pfichtner.proto.api.ToArduinoPinEvent;
import com.github.pfichtner.proto.api.ToArduinoStartListening;
import com.github.pfichtner.proto.api.ToArduinoStopListening;

public class Link {

	private static final Logger logger = LoggerFactory.getLogger(Link.class);

	private final Connection connection;
	private final Protocol protocol;
	private final List<EventListener> eventListeners = new CopyOnWriteArrayList<EventListener>();

	public Link(Connection connection, Protocol protocol) {
		this.connection = connection;
		this.protocol = protocol;
		this.connection.addListener(new ListenerAdapter() {
			@Override
			public void received(byte[] bytes) throws IOException {
				Link.this.received(bytes);
			}
		});
	}

	public Link addListener(EventListener listener) throws IOException {
		this.eventListeners.add(listener);
		if (listener instanceof FilteredEventListenerAdapter) {
			Pin pin = ((FilteredEventListenerAdapter) listener).getPin();
			ToArduinoStartListening startListeningEvent = new ToArduinoStartListening(
					pin);
			this.connection.write(this.protocol.toArduino(startListeningEvent));
		}
		return this;
	}

	public Link removeListener(EventListener listener) throws IOException {
		this.eventListeners.remove(listener);
		if (listener instanceof FilteredEventListenerAdapter) {
			Pin pin = ((FilteredEventListenerAdapter) listener).getPin();
			ToArduinoStopListening stopListening = new ToArduinoStopListening(
					pin);
			if (!listenerLeftFor(pin)) {
				this.connection.write(this.protocol.toArduino(stopListening));
			}
		}
		return this;
	}

	private boolean listenerLeftFor(Pin pin) {
		for (EventListener listener : eventListeners) {
			if (listener instanceof FilteredEventListenerAdapter
					&& pin.equals(((FilteredEventListenerAdapter) listener)
							.getPin())) {
				return true;
			}
		}
		return false;
	}

	public void switchAnalogPin(AnalogPin analogPin, Integer value)
			throws IOException {
		send(analogPin, value);
	}

	public void switchDigitalPin(DigitalPin digitalPin, Boolean value)
			throws IOException {
		send(digitalPin, value);
	}

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

	public void close() throws IOException {
		this.connection.close();
	}

}
