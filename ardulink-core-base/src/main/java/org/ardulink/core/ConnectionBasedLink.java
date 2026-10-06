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

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.core.ConnectionBasedLink.Mode.ANY_MESSAGE_RECEIVED;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageCustom.toDeviceMessageCustom;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageKeyPress.toDeviceMessageKeyPress;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageNoTone.toDeviceMessageNoTone;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessagePinStateChange.toDeviceMessagePinStateChange;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessagePing.toDeviceMessageNoTone;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageStartListening.toDeviceMessageStartListening;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageStopListening.toDeviceMessageStopListening;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageTone.toDeviceMessageTone;
import static org.ardulink.core.proto.api.MessageIdHolders.addMessageId;
import static org.ardulink.core.proto.api.MessageIdHolders.messageIdOf;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Connection.ListenerAdapter;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.events.DefaultCustomEvent;
import org.ardulink.core.events.DefaultRplyEvent;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessageCustom;
import org.ardulink.core.messages.api.FromDeviceMessageInfo;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.api.FromDeviceMessageReply;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
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

	private static final Logger logger = LoggerFactory.getLogger(ConnectionBasedLink.class);

	private final ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor();

	private final Connection connection;
	private final ByteStreamProcessor byteStreamProcessor;
	private long messageId;
	private volatile boolean infoMsgReceived;

	public <T extends Connection & ByteStreamProcessorProvider> ConnectionBasedLink(T connection) {
		this(connection, connection.getByteStreamProcessor());
	}

	public ConnectionBasedLink(Connection connection, ByteStreamProcessor byteStreamProcessor) {
		this.connection = connection;
		this.byteStreamProcessor = byteStreamProcessor;
		this.byteStreamProcessor.addListener(this::received);
		this.byteStreamProcessor.setOutboundListener(bytes -> {
			try {
				this.connection.write(bytes);
			} catch (Exception e) {
				// ignore outbound errors during boot
			}
		});
	}

	public Connection getConnection() {
		return connection;
	}

	protected void received(FromDeviceMessage fromDevice) {
		if (fromDevice instanceof FromDeviceMessagePinStateChanged) {
			handlePinChanged((FromDeviceMessagePinStateChanged) fromDevice);
		} else if (fromDevice instanceof FromDeviceMessageReply) {
			FromDeviceMessageReply reply = (FromDeviceMessageReply) fromDevice;
			fireReplyReceived(new DefaultRplyEvent(reply.isOk(), reply.getId(), reply.getParameters()));
		} else if (fromDevice instanceof FromDeviceMessageCustom) {
			FromDeviceMessageCustom customEvent = (FromDeviceMessageCustom) fromDevice;
			fireCustomReceived(new DefaultCustomEvent(customEvent.getMessage()));
		} else if (fromDevice instanceof FromDeviceMessageInfo) {
			this.infoMsgReceived = true;
		} else {
			throw new IllegalStateException("Cannot handle " + fromDevice);
		}
	}

	protected void handlePinChanged(FromDeviceMessagePinStateChanged pinChanged) {
		Pin pin = pinChanged.getPin();
		Object value = pinChanged.getValue();
		if (pin.is(ANALOG) && value instanceof Integer) {
			fireStateChanged(analogPinValueChanged((AnalogPin) pin, (Integer) value));
		} else if (pin.is(DIGITAL) && value instanceof Boolean) {
			fireStateChanged(digitalPinValueChanged((DigitalPin) pin, (Boolean) value));
		} else {
			throw new IllegalStateException("Cannot handle pin change event for pin " + pin + " with value " + value);
		}
	}

	/**
	 * Will wait for the arduino to received the "ready" packet or the arduino to
	 * respond to our messages sent.
	 * 
	 * @param wait     the maximum time to wait
	 * @param timeUnit the units to wait
	 * @return <code>true</code> if the arduino did response within the given time
	 *         otherwise <code>false</code>
	 */
	public boolean waitForArduinoToBoot(int wait, TimeUnit timeUnit) {
		return waitForArduinoToBoot(wait, timeUnit, Mode.ANY_MESSAGE_RECEIVED);
	}

	public enum Mode {
		/**
		 * interpret any message received from arduino as "device is ready"
		 */
		ANY_MESSAGE_RECEIVED,
		/**
		 * only interpret "info" packets as "device is ready"
		 */
		INFO_MESSAGE_ONLY
	}

	/**
	 * Will wait for the arduino to received the "info" packet or the arduino to
	 * respond to our messages sent.
	 * 
	 * @param wait     the maximum time to wait
	 * @param timeUnit the units to wait
	 * @param mode     the messages to be interpreted as "ok"
	 * 
	 * @return <code>true</code> if the arduino did response within the given time
	 *         otherwise <code>false</code>
	 */
	public boolean waitForArduinoToBoot(int wait, TimeUnit timeUnit, Mode mode) {
		CompletableFuture<Boolean> ready = new CompletableFuture<>();

		ListenerAdapter listener = new ListenerAdapter() {
			@Override
			public void received(byte[] bytes) {
				if (mode == ANY_MESSAGE_RECEIVED || infoMsgReceived) {
					ready.complete(true);
				}
			}
		};

		connection.addListener(listener);
		ScheduledFuture<?> pinger = scheduler.scheduleAtFixedRate(this::ping, 0, 100, MILLISECONDS);
		return ready.whenComplete((__, ___) -> {
			pinger.cancel(false);
			connection.removeListener(listener);
		}).orTimeout(wait, timeUnit) //
				.exceptionally(__ -> false) //
				.join();
	}

	private void ping() {
		try {
			connection.write(this.byteStreamProcessor.toDevice(addMessageId(toDeviceMessageNoTone(), 0)));
		} catch (IOException e) {
			// ignore
		}
	}

	@Override
	public long startListening(Pin pin) throws IOException {
		logger.info("Starting listening on pin {}", pin);
		synchronized (connection) {
			ToDeviceMessageStartListening msg = addMessageIdIfNeeded(toDeviceMessageStartListening(pin));
			send(this.byteStreamProcessor.toDevice(msg));
			return messageIdOf(msg);
		}
	}

	@Override
	public long stopListening(Pin pin) throws IOException {
		synchronized (connection) {
			ToDeviceMessageStopListening msg = addMessageIdIfNeeded(toDeviceMessageStopListening(pin));
			send(this.byteStreamProcessor.toDevice(msg));
			logger.info("Stopped listening on pin {}", pin);
			return messageIdOf(msg);
		}
	}

	@Override
	public long switchAnalogPin(AnalogPin analogPin, int value) throws IOException {
		return send(analogPin, value);
	}

	@Override
	public long switchDigitalPin(DigitalPin digitalPin, boolean value) throws IOException {
		return send(digitalPin, value);
	}

	@Override
	public long sendKeyPressEvent(char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex)
			throws IOException {
		synchronized (connection) {
			ToDeviceMessageKeyPress msg = addMessageIdIfNeeded(
					toDeviceMessageKeyPress(keychar, keycode, keylocation, keymodifiers, keymodifiersex));
			send(this.byteStreamProcessor.toDevice(msg));
			return messageIdOf(msg);
		}
	}

	@Override
	public long sendTone(Tone tone) throws IOException {
		synchronized (connection) {
			ToDeviceMessageTone msg = addMessageIdIfNeeded(toDeviceMessageTone(tone));
			send(this.byteStreamProcessor.toDevice(msg));
			return messageIdOf(msg);
		}
	}

	@Override
	public long sendNoTone(AnalogPin analogPin) throws IOException {
		synchronized (connection) {
			ToDeviceMessageNoTone msg = addMessageIdIfNeeded(toDeviceMessageNoTone(analogPin));
			send(this.byteStreamProcessor.toDevice(msg));
			return messageIdOf(msg);
		}
	}

	@Override
	public long sendCustomMessage(String... messages) throws IOException {
		synchronized (connection) {
			ToDeviceMessageCustom msg = addMessageIdIfNeeded(toDeviceMessageCustom(messages));
			send(this.byteStreamProcessor.toDevice(msg));
			return messageIdOf(msg);
		}
	}

	private long send(AnalogPin pin, int value) throws IOException {
		synchronized (connection) {
			ToDeviceMessagePinStateChange msg = addMessageIdIfNeeded(toDeviceMessagePinStateChange(pin, value));
			send(this.byteStreamProcessor.toDevice(msg));
			return messageIdOf(msg);
		}
	}

	private long send(DigitalPin pin, boolean value) throws IOException {
		synchronized (connection) {
			ToDeviceMessagePinStateChange msg = addMessageIdIfNeeded(toDeviceMessagePinStateChange(pin, value));
			send(this.byteStreamProcessor.toDevice(msg));
			return messageIdOf(msg);
		}
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

	@Override
	public void close() throws IOException {
		deregisterAllEventListeners();
		this.connection.close();
		super.close();
	}

}
