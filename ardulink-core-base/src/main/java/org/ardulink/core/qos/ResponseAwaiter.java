package org.ardulink.core.qos;

import static org.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ardulink.core.Link;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.events.RplyListener;
import org.ardulink.util.Lists;
import org.ardulink.util.Optional;

public class ResponseAwaiter {

	private final Link link;
	private final List<RplyEvent> replies = Lists.newArrayList();

	private final Lock lock = new ReentrantLock(false);
	private final Condition condition = lock.newCondition();

	private final RplyListener listener;
	private long timeout = -1;
	private TimeUnit timeUnit;

	/**
	 * Do not reuse instances! After calling {@link #waitForResponse(long)} the
	 * reply listener is deregistered! So you need to create a new instance.
	 * 
	 * @param link
	 *            the Link to wait for a reply on
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

	private ResponseAwaiter(final Link link) throws IOException {
		this.link = link;
		this.listener = new RplyListener() {
			@Override
			public void rplyReceived(RplyEvent event) {
				lock.lock();
				replies.add(event);
				try {
					condition.signal();
				} finally {
					lock.unlock();
				}

			}
		};
		this.link.addRplyListener(listener);

	}

	/**
	 * This method blocks until the response for the passed message id is
	 * received.
	 * 
	 * @param messageId
	 * @return
	 * @throws IOException
	 */
	public RplyEvent waitForResponse(long messageId) throws IOException {
		try {
			while (true) {
				lock.lock();
				try {
					if (timeout < 0 || timeUnit == null) {
						condition.await();
					} else {
						checkState(condition.await(timeout, timeUnit),
								"No response received within %s %s ",
								this.timeout, timeUnit);
					}
					Optional<RplyEvent> rply = messageIdReceived(messageId);
					replies.clear();
					if (rply.isPresent()) {
						return rply.get();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					lock.unlock();
				}
			}
		} finally {
			link.removeRplyListener(listener);
		}
	}

	private Optional<RplyEvent> messageIdReceived(long messageId) {
		lock.lock();
		try {
			for (RplyEvent reply : replies) {
				if (reply.getId() == messageId) {
					return Optional.of(reply);
				}
			}
			return Optional.absent();
		} finally {
			lock.unlock();
		}
	}

}
