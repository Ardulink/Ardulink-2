package com.github.pfichtner.ardulink.core;

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

import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.events.RplyEvent;
import com.github.pfichtner.ardulink.core.events.RplyListener;
import com.github.pfichtner.ardulink.core.proto.api.MessageIdHolder;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoTone;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoKeyPressEvent;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoNoTone;
import com.github.pfichtner.ardulink.core.proto.impl.DefaultToArduinoTone;

/**
 * Arduino sends ok/ko messages directly aftere receiving the work message. So
 * there is no need for a queue because if the next message read is not the
 * ok/ko response it never will arrive.
 * 
 * @author Peter Fichtner
 */
public class HALink extends ConnectionBasedLink implements RplyListener {

	private static final Logger logger = LoggerFactory.getLogger(HALink.class);

	private static final AtomicLong messageCounter = new AtomicLong();
	private final Lock lock = new ReentrantLock(false);
	private final Condition condition = lock.newCondition();
	private RplyEvent event;

	private final long timeout;
	private final TimeUnit timeUnit;

	public HALink(Connection connection, Protocol protocol) throws IOException {
		this(connection, protocol, 5, SECONDS);
	}

	public HALink(Connection connection, Protocol protocol, int timeout,
			TimeUnit timeUnit) throws IOException {
		super(connection, protocol);
		this.timeout = timeout;
		this.timeUnit = timeUnit;
		addRplyListener(this);
	}

	@Override
	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		long messageId = nextId();
		getConnection().write(
				getProtocol().toArduino(
						proxy(new DefaultToArduinoKeyPressEvent(keychar, keycode,
								keylocation, keymodifiers, keymodifiersex),
								messageId)));
		waitFor(messageId);
	}

	@Override
	public void sendTone(Tone tone) throws IOException {
		long messageId = nextId();
		ToArduinoTone a = new DefaultToArduinoTone(tone);
		ToArduinoTone b = proxy(a, messageId);
		getConnection().write(getProtocol().toArduino(b));
		waitFor(messageId);
	}

	@Override
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		long messageId = nextId();
		getConnection().write(
				getProtocol().toArduino(
						proxy(new DefaultToArduinoNoTone(analogPin), messageId)));
		waitFor(messageId);
	}

	@SuppressWarnings("unchecked")
	private <T> T proxy(final T t, final long messageId) {
		InvocationHandler h = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				if (method.equals(MessageIdHolder.class.getMethod("getId"))) {
					return messageId;
				}
				return method.invoke(t, args);
			}
		};
		Class<?>[] existingInterfaces = t.getClass().getInterfaces();
		Class<?>[] newInterfaces = new Class<?>[existingInterfaces.length + 1];
		newInterfaces[0] = MessageIdHolder.class;
		System.arraycopy(existingInterfaces, 0, newInterfaces, 1,
				existingInterfaces.length);
		return (T) Proxy.newProxyInstance(t.getClass().getClassLoader(),
				newInterfaces, h);
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
