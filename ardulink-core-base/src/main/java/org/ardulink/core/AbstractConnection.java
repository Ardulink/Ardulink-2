package org.ardulink.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConnection implements Connection {

	private static final Logger logger = LoggerFactory
			.getLogger(AbstractConnection.class);
	
	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	@Override
	public void addListener(Listener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		this.listeners.remove(listener);
	}

	public List<Listener> getListeners() {
		return listeners;
	}
	
	protected void fireSent(byte[] bytes) {
		for (Listener listener : listeners) {
			try {
				listener.sent(bytes);
			} catch (Exception e) {
				logger.error("Listener {} failure", listener, e);
			}
		}
	}

	protected void fireReceived(byte[] bytes) {
		for (Listener listener : listeners) {
			try {
				listener.received(bytes);
			} catch (Exception e) {
				logger.error("Listener {} failure", listener, e);
			}
		}
	}
}
