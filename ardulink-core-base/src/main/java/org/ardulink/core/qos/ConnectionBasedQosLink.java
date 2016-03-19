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

package org.ardulink.core.qos;

import static org.ardulink.core.proto.api.MessageIdHolders.addMessageId;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ardulink.core.AbstractConnectionBasedLink;
import org.ardulink.core.Connection;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.events.RplyListener;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.impl.DefaultToArduinoCustomMessage;
import org.ardulink.core.proto.impl.DefaultToArduinoKeyPressEvent;
import org.ardulink.core.proto.impl.DefaultToArduinoNoTone;
import org.ardulink.core.proto.impl.DefaultToArduinoPinEvent;
import org.ardulink.core.proto.impl.DefaultToArduinoStartListening;
import org.ardulink.core.proto.impl.DefaultToArduinoStopListening;
import org.ardulink.core.proto.impl.DefaultToArduinoTone;

/**
 * [ardulinktitle] [ardulinkversion]
 * Arduino sends ok/ko messages directly after receiving the work message. So
 * there is no need for a queue because if the next message read is not the
 * ok/ko response it never will arrive.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class ConnectionBasedQosLink extends AbstractConnectionBasedLink {

	private static final Logger logger = LoggerFactory
			.getLogger(ConnectionBasedQosLink.class);

	private static long messageCounter;
	private final Lock lock = new ReentrantLock(false);
	private final Condition condition = lock.newCondition();
	private RplyEvent event;

	private final long timeout;
	private final TimeUnit timeUnit;

	public ConnectionBasedQosLink(Connection connection, Protocol protocol)
			throws IOException {
		this(connection, protocol, 5, SECONDS);
	}

	public ConnectionBasedQosLink(Connection connection, Protocol protocol,
			int timeout, TimeUnit timeUnit) throws IOException {
		super(connection, protocol);
		this.timeout = timeout;
		this.timeUnit = timeUnit;
		addRplyListener(new RplyListener() {
			@Override
			public void rplyReceived(RplyEvent event) {
				logger.debug("Received {}", event.getId());
				ConnectionBasedQosLink.this.event = event;
				lock.lock();
				try {
					ConnectionBasedQosLink.this.condition.signal();
				} finally {
					ConnectionBasedQosLink.this.lock.unlock();
				}
			}
		});
	}

	@Override
	public void startListening(Pin pin) throws IOException {
		logger.info("Starting listening on pin {}", pin);
		synchronized (getConnection()) {
			long messageId = nextId();
			sendAndWait(
					getProtocol().toArduino(
							addMessageId(
									new DefaultToArduinoStartListening(pin),
									messageId)), messageId);
		}
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		synchronized (getConnection()) {
			long messageId = nextId();
			sendAndWait(
					getProtocol().toArduino(
							addMessageId(
									new DefaultToArduinoStopListening(pin),
									messageId)), messageId);
		}
		logger.info("Stopped listening on pin {}", pin);
	}

	@Override
	public void switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		synchronized (getConnection()) {
			long messageId = nextId();
			sendAndWait(
					getProtocol().toArduino(
							addMessageId(new DefaultToArduinoPinEvent(
									analogPin, value), messageId)), messageId);
		}
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		synchronized (getConnection()) {
			long messageId = nextId();
			sendAndWait(
					getProtocol().toArduino(
							addMessageId(new DefaultToArduinoPinEvent(
									digitalPin, value), messageId)), messageId);
		}
	}

	@Override
	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		synchronized (getConnection()) {
			long messageId = nextId();
			sendAndWait(
					getProtocol().toArduino(
							addMessageId(new DefaultToArduinoKeyPressEvent(
									keychar, keycode, keylocation,
									keymodifiers, keymodifiersex), messageId)),
					messageId);
		}
	}

	@Override
	public void sendTone(Tone tone) throws IOException {
		synchronized (getConnection()) {
			long messageId = nextId();
			sendAndWait(
					getProtocol().toArduino(
							addMessageId(new DefaultToArduinoTone(tone),
									messageId)), messageId);
		}
	}

	@Override
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		synchronized (getConnection()) {
			long messageId = nextId();
			sendAndWait(
					getProtocol().toArduino(
							addMessageId(new DefaultToArduinoNoTone(analogPin),
									messageId)), messageId);
		}
	}

	@Override
	public void sendCustomMessage(String... messages) throws IOException {
		synchronized (getConnection()) {
			long messageId = nextId();
			sendAndWait(
					getProtocol().toArduino(
							addMessageId(new DefaultToArduinoCustomMessage(
									messages), messageId)), messageId);
		}
	}

	private void sendAndWait(byte[] bytes, long messageId) throws IOException {
		getConnection().write(bytes);
		waitFor(messageId);
	}

	private long nextId() {
		return ++messageCounter;
	}

	// TODO register a listener that interrupts if ANY other message received in
	// the meanwhile
	private void waitFor(long idToWaitFor) {
		lock.lock();
		try {
			logger.debug("Wait for {}", idToWaitFor);
			try {
				checkState(
						condition.await(timeout, timeUnit),
						"No response received while waiting for messageId %s within %s %s",
						idToWaitFor, timeout, timeUnit);
				long idReceived = checkNotNull(event,
						"No event received but condition signalled").getId();
				checkState(idReceived == idToWaitFor,
						"Waited for %s but got %s", idToWaitFor, idReceived);
				checkState(event.isOk(), "Response status is not ok");
				logger.debug("Condition wait {}", idReceived);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} finally {
			lock.unlock();
		}
	}

}
