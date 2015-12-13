package com.github.pfichtner.ardulink.core;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.io.IOException;
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
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoNoTone;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoTone;

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
	public void sendTone(Tone tone) throws IOException {
		long messageId = nextId();
		getConnection().write(
				getProtocol().toArduino(new ToArduinoTone(messageId, tone)));
		waitFor(messageId);
	}

	@Override
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		long messageId = nextId();
		getConnection().write(
				getProtocol().toArduino(
						new ToArduinoNoTone(messageId, analogPin)));
		waitFor(messageId);
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
