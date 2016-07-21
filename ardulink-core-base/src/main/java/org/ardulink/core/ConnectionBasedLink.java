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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.core.ConnectionBasedLink.Mode.ANY_MESSAGE_RECEIVED;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.proto.api.MessageIdHolders.NO_ID;
import static org.ardulink.core.proto.api.MessageIdHolders.addMessageId;
import static org.ardulink.core.proto.api.MessageIdHolders.toHolder;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.ardulink.core.Connection.ListenerAdapter;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import org.ardulink.core.events.DefaultRplyEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.Protocol.FromArduino;
import org.ardulink.core.proto.api.ToArduinoCustomMessage;
import org.ardulink.core.proto.api.ToArduinoKeyPressEvent;
import org.ardulink.core.proto.api.ToArduinoNoTone;
import org.ardulink.core.proto.api.ToArduinoPinEvent;
import org.ardulink.core.proto.api.ToArduinoStartListening;
import org.ardulink.core.proto.api.ToArduinoStopListening;
import org.ardulink.core.proto.api.ToArduinoTone;
import org.ardulink.core.proto.impl.DefaultToArduinoCustomMessage;
import org.ardulink.core.proto.impl.DefaultToArduinoKeyPressEvent;
import org.ardulink.core.proto.impl.DefaultToArduinoNoTone;
import org.ardulink.core.proto.impl.DefaultToArduinoPinEvent;
import org.ardulink.core.proto.impl.DefaultToArduinoStartListening;
import org.ardulink.core.proto.impl.DefaultToArduinoStopListening;
import org.ardulink.core.proto.impl.DefaultToArduinoTone;
import org.ardulink.core.proto.impl.FromArduinoPinStateChanged;
import org.ardulink.core.proto.impl.FromArduinoReady;
import org.ardulink.core.proto.impl.FromArduinoReply;
import org.ardulink.util.StopWatch;
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
		received(this.protocol.fromArduino(bytes));
	}

	protected void received(FromArduino fromArduino) {
		if (fromArduino instanceof FromArduinoPinStateChanged) {
			handlePinChanged((FromArduinoPinStateChanged) fromArduino);
		} else if (fromArduino instanceof FromArduinoReply) {
			FromArduinoReply reply = (FromArduinoReply) fromArduino;
			fireReplyReceived(new DefaultRplyEvent(reply.isOk(), reply.getId(),
					reply.getParameters()));
		} else if (fromArduino instanceof FromArduinoReady) {
			this.readyMsgReceived = true;
		} else {
			throw new IllegalStateException("Cannot handle " + fromArduino);
		}
	}

	protected void handlePinChanged(FromArduinoPinStateChanged pinChanged) {
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
	public boolean waitForArduinoToBoot(int wait, TimeUnit timeUnit,
			final Mode mode) {
		final ReentrantLock lock = new ReentrantLock();
		final Condition condition = lock.newCondition();
		ListenerAdapter listener = new ListenerAdapter() {
			@Override
			public void received(byte[] bytes) throws IOException {
				lock.lock();
				try {
					if (mode == ANY_MESSAGE_RECEIVED || readyMsgReceived)
						condition.signalAll();
				} finally {
					lock.unlock();
				}
			}
		};
		this.connection.addListener(listener);

		StopWatch stopwatch = new StopWatch().start();
		try {
			while (true) {
				lock.lock();
				try {
					ping();
					if (condition.await(500, MILLISECONDS)) {
						return true;
					}
					long time = stopwatch.getTime(timeUnit);
					if (time >= wait) {
						return false;
					}
				} finally {
					lock.unlock();
				}
			}
		} catch (InterruptedException e) {
			throw propagate(e);
		} finally {
			this.connection.removeListener(listener);
		}
	}

	private void ping() {
		// this is not really a ping message since such a message does not exist
		// (yet). So let's write something that the arduino tries to respond to.
		try {
			long messageId = 0;
			connection.write(this.protocol.toArduino(addMessageId(
					new DefaultToArduinoNoTone(analogPin(0)), messageId)));
		} catch (IOException e) {
			// ignore
		}
	}

	@Override
	public long startListening(Pin pin) throws IOException {
		logger.info("Starting listening on pin {}", pin);
		ToArduinoStartListening msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToArduinoStartListening(pin));
			send(this.protocol.toArduino(msg));
		}
		return messageIdOf(msg);
	}

	@Override
	public long stopListening(Pin pin) throws IOException {
		ToArduinoStopListening msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToArduinoStopListening(pin));
			send(this.protocol.toArduino(msg));
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
		ToArduinoKeyPressEvent msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToArduinoKeyPressEvent(
					keychar, keycode, keylocation, keymodifiers, keymodifiersex));
			send(this.protocol.toArduino(msg));
		}
		return messageIdOf(msg);
	}

	@Override
	public long sendTone(Tone tone) throws IOException {
		ToArduinoTone msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToArduinoTone(tone));
			send(this.protocol.toArduino(msg));
		}
		return messageIdOf(msg);
	}

	@Override
	public long sendNoTone(AnalogPin analogPin) throws IOException {
		ToArduinoNoTone msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToArduinoNoTone(analogPin));
			send(this.protocol.toArduino(msg));
		}
		return messageIdOf(msg);
	}

	@Override
	public long sendCustomMessage(String... messages) throws IOException {
		ToArduinoCustomMessage msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToArduinoCustomMessage(
					messages));
			send(this.protocol.toArduino(msg));
		}
		return messageIdOf(msg);
	}

	private long send(AnalogPin pin, int value) throws IOException {
		ToArduinoPinEvent msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToArduinoPinEvent(pin, value));
			send(this.protocol.toArduino(msg));
		}
		return messageIdOf(msg);
	}

	private long send(DigitalPin pin, boolean value) throws IOException {
		ToArduinoPinEvent msg;
		synchronized (connection) {
			msg = addMessageIdIfNeeded(new DefaultToArduinoPinEvent(pin, value));
			send(this.protocol.toArduino(msg));
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
