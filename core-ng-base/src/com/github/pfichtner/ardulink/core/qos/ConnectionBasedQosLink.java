package com.github.pfichtner.ardulink.core.qos;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pfichtner.ardulink.core.AbstractConnectionBasedLink;
import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.Tone;
import com.github.pfichtner.ardulink.core.events.RplyEvent;
import com.github.pfichtner.ardulink.core.events.RplyListener;
import com.github.pfichtner.ardulink.core.proto.api.MessageIdHolder;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoCustomMessage;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoKeyPressEvent;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoNoTone;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoPinEvent;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoStartListening;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoStopListening;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoTone;

/**
 * Arduino sends ok/ko messages directly after receiving the work message. So
 * there is no need for a queue because if the next message read is not the
 * ok/ko response it never will arrive.
 * 
 * @author Peter Fichtner
 */
public class ConnectionBasedQosLink extends AbstractConnectionBasedLink
		implements RplyListener {

	private static class MessageIdHolderInvocationHandler implements
			InvocationHandler {

		private static final Method messageIdHolderGetIdMethod = getMessageIdHolderGetIdMethod();

		private final Object delegate;
		private final long messageId;

		private static Method getMessageIdHolderGetIdMethod() {
			try {
				return MessageIdHolder.class.getMethod("getId");
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}

		public MessageIdHolderInvocationHandler(Object delegate, long messageId) {
			this.delegate = delegate;
			this.messageId = messageId;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			return messageIdHolderGetIdMethod.equals(method) ? messageId
					: method.invoke(delegate, args);
		}

	}

	private static final Logger logger = LoggerFactory
			.getLogger(ConnectionBasedQosLink.class);

	private static final AtomicLong messageCounter = new AtomicLong();
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
		addRplyListener(this);
	}

	@Override
	public void startListening(Pin pin) throws IOException {
		logger.info("Starting listening on pin {}", pin);
		long messageId = nextId();
		sendAndWait(
				getProtocol().toArduino(
						proxy(new DefaultToArduinoStartListening(pin),
								messageId)), messageId);
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		long messageId = nextId();
		sendAndWait(
				getProtocol()
						.toArduino(
								proxy(new DefaultToArduinoStopListening(pin),
										messageId)), messageId);
		logger.info("Stopped listening on pin {}", pin);
	}

	@Override
	public void switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		long messageId = nextId();
		sendAndWait(
				getProtocol().toArduino(
						proxy(new DefaultToArduinoPinEvent(analogPin, value),
								messageId)), messageId);
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		long messageId = nextId();
		sendAndWait(
				getProtocol().toArduino(
						proxy(new DefaultToArduinoPinEvent(digitalPin, value),
								messageId)), messageId);
	}

	@Override
	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		long messageId = nextId();
		sendAndWait(
				getProtocol().toArduino(
						proxy(new DefaultToArduinoKeyPressEvent(keychar,
								keycode, keylocation, keymodifiers,
								keymodifiersex), messageId)), messageId);
	}

	@Override
	public void sendTone(Tone tone) throws IOException {
		long messageId = nextId();
		sendAndWait(
				getProtocol().toArduino(
						proxy(new DefaultToArduinoTone(tone), messageId)),
				messageId);
	}

	@Override
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		long messageId = nextId();
		sendAndWait(
				getProtocol()
						.toArduino(
								proxy(new DefaultToArduinoNoTone(analogPin),
										messageId)), messageId);
	}

	@Override
	public void sendCustomMessage(String... messages) throws IOException {
		long messageId = nextId();
		sendAndWait(
				getProtocol().toArduino(
						proxy(new DefaultToArduinoCustomMessage(messages),
								messageId)), messageId);
	}

	private void sendAndWait(byte[] bytes, long messageId) throws IOException {
		getConnection().write(bytes);
		waitFor(messageId);
	}

	private <T> T proxy(T delegateTo, long messageId) {
		Class<?>[] existingInterfaces = delegateTo.getClass().getInterfaces();
		Class<?>[] newInterfaces = new Class<?>[existingInterfaces.length + 1];
		newInterfaces[0] = MessageIdHolder.class;
		System.arraycopy(existingInterfaces, 0, newInterfaces, 1,
				existingInterfaces.length);
		@SuppressWarnings("unchecked")
		T proxy = (T) Proxy.newProxyInstance(delegateTo.getClass()
				.getClassLoader(), newInterfaces,
				new MessageIdHolderInvocationHandler(delegateTo, messageId));
		return proxy;
	}

	private long nextId() {
		return messageCounter.incrementAndGet();
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

	@Override
	public void rplyReceived(RplyEvent event) {
		logger.debug("Received {}", event.getId());
		this.event = event;
		lock.lock();
		try {
			condition.signal();
		} finally {
			lock.unlock();
		}
	}

}
