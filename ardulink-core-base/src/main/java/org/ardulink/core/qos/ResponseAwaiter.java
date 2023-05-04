package org.ardulink.core.qos;

import static org.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Link;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.events.RplyListener;

public class ResponseAwaiter {

	private final Link link;
	private final Queue<RplyEvent> replies = new ConcurrentLinkedDeque<>();
	private final Semaphore semaphore = new Semaphore(0);

	private final RplyListener listener;
	private long timeout = -1;
	private TimeUnit timeUnit;

	/**
	 * Do not reuse instances! After calling {@link #waitForResponse(long)} the
	 * reply listener is unregistered! So you need to create a new instance.
	 * 
	 * @param link the Link to wait for a reply on
	 * @throws IOException
	 */
	public static ResponseAwaiter onLink(Link link) throws IOException {
		return new ResponseAwaiter(link);
	}

	public ResponseAwaiter withTimeout(long timeout, TimeUnit timeUnit) {
		this.timeout = timeout;
		this.timeUnit = timeUnit;
		return this;
	}

	private ResponseAwaiter(Link link) throws IOException {
		this.link = link;
		this.listener = e -> {
			replies.add(e);
			semaphore.release();
		};
		this.link.addRplyListener(listener);
	}

	/**
	 * This method blocks until the response for the passed message id is received.
	 * 
	 * @param messageId
	 * @return
	 * @throws IOException
	 */
	public RplyEvent waitForResponse(long messageId) throws IOException {
		try {
			while (true) {
				RplyEvent rply = waitForRplyEventWithMsgId(messageId);
				if (rply != null) {
					return rply;
				}
			}
		} finally {
			link.removeRplyListener(listener);
		}
	}

	private RplyEvent waitForRplyEventWithMsgId(long messageId) {
		try {
			if (timeout < 0 || timeUnit == null) {
				semaphore.acquire();
			} else {
				checkState(semaphore.tryAcquire(timeout, timeUnit), "No response received within %s %s ", this.timeout,
						timeUnit);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return messageIdReceived(messageId);
	}

	private RplyEvent messageIdReceived(long messageId) {
		RplyEvent polled;
		while ((polled = replies.poll()) != null) {
			if (polled.getId() == messageId) {
				return polled;
			}
		}
		return null;
	}

}
