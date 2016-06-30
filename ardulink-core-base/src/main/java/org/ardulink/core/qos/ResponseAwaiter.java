package org.ardulink.core.qos;

import java.io.IOException;
import java.util.List;
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

	/**
	 * Since the instance will reference <b>all</b> replies it should not be
	 * used as class attribute but as a very short living instance.
	 * 
	 * @param link
	 *            the Link to wait for a reply on
	 * @throws IOException
	 */
	public static ResponseAwaiter onLink(Link link) throws IOException {
		return new ResponseAwaiter(link);
	}

	/**
	 * Since the instance will reference <b>all</b> replies it should not be
	 * used as class attribute but as a very short living instance.
	 * 
	 * @param link
	 *            the Link to wait for a reply on
	 * @throws IOException
	 */
	public ResponseAwaiter(final Link link) throws IOException {
		this.link = link;
		this.link.addRplyListener(new RplyListener() {
			@Override
			public void rplyReceived(RplyEvent event) {
				lock.lock();
				synchronized (replies) {
					replies.add(event);
				}
				try {
					ResponseAwaiter.this.condition.signal();
				} finally {
					ResponseAwaiter.this.lock.unlock();
				}

			}
		});

	}

	/**
	 * This method blocks until the response for the passed message id is
	 * received.
	 * 
	 * @param messageId
	 * @return
	 */
	public RplyEvent waitForResponse(long messageId) {
		while (true) {
			try {
				condition.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			synchronized (replies) {
				Optional<RplyEvent> rply = messageIdReceived(messageId);
				replies.clear();
				if (rply.isPresent()) {
					return rply.get();
				}
			}
		}
	}

	private Optional<RplyEvent> messageIdReceived(long messageId) {
		for (RplyEvent reply : replies) {
			if (reply.getId() == messageId) {
				return Optional.of(reply);
			}
		}
		return Optional.absent();
	}

}
