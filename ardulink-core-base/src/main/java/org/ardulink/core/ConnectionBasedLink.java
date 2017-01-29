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

package org.ardulink.core;

import static org.ardulink.core.ConnectionBasedLink.Mode.ANY_MESSAGE_RECEIVED;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.proto.api.MessageIdHolders.NO_ID;
import static org.ardulink.core.proto.api.MessageIdHolders.addMessageId;
import static org.ardulink.core.proto.api.MessageIdHolders.toHolder;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Connection.ListenerAdapter;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultCustomEvent;
import org.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import org.ardulink.core.events.DefaultRplyEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessageCustom;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.api.FromDeviceMessageReady;
import org.ardulink.core.messages.api.FromDeviceMessageReply;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageCustom;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageKeyPress;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageNoTone;
import org.ardulink.core.messages.impl.DefaultToDeviceMessagePinStateChange;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageStartListening;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageStopListening;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageTone;
import org.ardulink.core.proto.api.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ConnectionBasedLink extends AbstractListenerLink {

	private static final Logger logger = LoggerFactory
			.getLogger(ConnectionBasedLink.class);

	private final Connection connection;
	private final Protocol protocol;
	private long messageId = 0;
	private boolean readyMsgReceived;

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

	protected void received(byte[] bytes) {
		received(this.protocol.fromDevice(bytes));
	}

	protected void received(FromDeviceMessage fromDevice) {
		if (fromDevice instanceof FromDeviceMessagePinStateChanged) {
			handlePinChanged((FromDeviceMessagePinStateChanged) fromDevice);
		} else if (fromDevice instanceof FromDeviceMessageReply) {
			FromDeviceMessageReply reply = (FromDeviceMessageReply) fromDevice;
			fireReplyReceived(new DefaultRplyEvent(reply.isOk(), reply.getId(), reply.getParameters()));
		} else if (fromDevice instanceof FromDeviceMessageCustom) {
			FromDeviceMessageCustom custom_event = (FromDeviceMessageCustom) fromDevice;
			fireCustomReceived(new DefaultCustomEvent(custom_event.getMessage()));
		} else if (fromDevice instanceof FromDeviceMessageReady) {
			this.readyMsgReceived = true;
		} else {
			throw new IllegalStateException("Cannot handle " + fromDevice);
		}
	}

	protected void handlePinChanged(FromDeviceMessagePinStateChanged pinChanged) {
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

	/**
	 * Will wait for the arduino to received the "ready" paket or the arduino to
	 * respond to our messages sent.
	 * 
	 * @param wait
	 *            the maximum time to wait
	 * @param timeUnit
	 *            the units to wait
	 * @return <code>true</code> if the arduino did response within the given
	 *         time otherwise <code>false</code>
	 */
	public boolean waitForArduinoToBoot(int wait, TimeUnit timeUnit) {
		return waitForArduinoToBoot(wait, timeUnit, Mode.ANY_MESSAGE_RECEIVED);
	}

	public enum Mode {
		/**
		 * interpret any message received from arduino to be ok
		 */
		ANY_MESSAGE_RECEIVED, /**
		 * only interpret "ready" packets to be ok
		 */
		READY_MESSAGE_ONLY;
	}

	/**
	 * Will wait for the arduino to received the "ready" paket or the arduino to
	 * respond to our messages sent.
	 * 
	 * @param wait
	 *            the maximum time to wait
	 * @param timeUnit
	 *            the units to wait
	 * @param mode
	 *            the messages to be interpreted as "ok"
	 * 
	 * @return <code>true</code> if the arduino did response within the given
	 *         time otherwise <code>false</code>
	 */
	public boolean waitForArduinoToBoot(int wait, TimeUnit timeUnit, final Mode mode) {

		/*
		 * lockingQueue used instead of Lock and Condition it has a shorter and simpler
		 * implementation.
		 */
		final ArrayBlockingQueue<Boolean> lockingQueue = new ArrayBlockingQueue<Boolean>(1);

		ListenerAdapter listener = new ListenerAdapter() {
			@Override
			public void received(byte[] bytes) throws IOException {
				if (mode == ANY_MESSAGE_RECEIVED || readyMsgReceived) {
					// messages are arriving so device is ready
					lockingQueue.offer(true);
				}
			}
		};
		this.connection.addListener(listener);

		Boolean deviceIsReady = null;
		try {
			ping();
			deviceIsReady = lockingQueue.poll(wait, timeUnit);
			if (deviceIsReady == null) {
				deviceIsReady = false;
			}

		} catch (InterruptedException e) {
			throw propagate(e);
		} finally {
			this.connection.removeListener(listener);
		}
		return deviceIsReady;
	}

	private void ping() {
		// this is not really a ping message since such a message does not exist
		// (yet). So let's write something that the arduino tries to respond to.
		try {
			long messageId = 0;
			connection.write(this.protocol.toDevice(addMessageId(
					new DefaultToDeviceMessageNoTone(analogPin(0)), messageId)));
		} catch (IOException e) {
			// ignore
		}
	}

	@Override
	public long startListening(Pin pin) throws IOException {
		logger.info("Starting listening on pin {}", pin);
		ToDeviceMessageStartListening msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToDeviceMessageStartListening(pin));
			send(this.protocol.toDevice(msg));
		}
		return messageIdOf(msg);
	}

	@Override
	public long stopListening(Pin pin) throws IOException {
		ToDeviceMessageStopListening msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToDeviceMessageStopListening(pin));
			send(this.protocol.toDevice(msg));
		}
		logger.info("Stopped listening on pin {}", pin);
		return messageIdOf(msg);
	}

	@Override
	public long switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		return send(analogPin, value);
	}

	@Override
	public long switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		return send(digitalPin, value);
	}

	@Override
	public long sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		ToDeviceMessageKeyPress msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToDeviceMessageKeyPress(
					keychar, keycode, keylocation, keymodifiers, keymodifiersex));
			send(this.protocol.toDevice(msg));
		}
		return messageIdOf(msg);
	}

	@Override
	public long sendTone(Tone tone) throws IOException {
		ToDeviceMessageTone msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToDeviceMessageTone(tone));
			send(this.protocol.toDevice(msg));
		}
		return messageIdOf(msg);
	}

	@Override
	public long sendNoTone(AnalogPin analogPin) throws IOException {
		ToDeviceMessageNoTone msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToDeviceMessageNoTone(analogPin));
			send(this.protocol.toDevice(msg));
		}
		return messageIdOf(msg);
	}

	@Override
	public long sendCustomMessage(String... messages) throws IOException {
		ToDeviceMessageCustom msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToDeviceMessageCustom(
					messages));
			send(this.protocol.toDevice(msg));
		}
		return messageIdOf(msg);
	}

	private long send(AnalogPin pin, int value) throws IOException {
		ToDeviceMessagePinStateChange msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToDeviceMessagePinStateChange(pin, value));
			send(this.protocol.toDevice(msg));
		}
		return messageIdOf(msg);
	}

	private long send(DigitalPin pin, boolean value) throws IOException {
		ToDeviceMessagePinStateChange msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToDeviceMessagePinStateChange(pin, value));
			send(this.protocol.toDevice(msg));
		}
		return messageIdOf(msg);
	}

	private void send(byte[] bytes) throws IOException {
		this.connection.write(bytes);
	}

	private <T> T addMessageIdIfNeeded(T event) {
		return hasRplyListeners() ? addMessageId(event, nextId()) : event;
	}

	private long nextId() {
		return ++messageId;
	}

	private long messageIdOf(Object msg) {
		return toHolder(msg).or(NO_ID).getId();
	}

	@Override
	public void close() throws IOException {
		deregisterAllEventListeners();
		this.connection.close();
		super.close();
	}

}
