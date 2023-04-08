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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.CustomEvent;
import org.ardulink.core.events.CustomListener;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.FilteredEventListenerAdapter;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.events.RplyListener;
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
public abstract class AbstractListenerLink implements Link {

	private static final Logger logger = LoggerFactory.getLogger(AbstractListenerLink.class);

	private final List<EventListener> eventListeners = new CopyOnWriteArrayList<>();
	private final List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
	private final List<RplyListener> rplyListeners = new CopyOnWriteArrayList<>();
	private final List<CustomListener> customListeners = new CopyOnWriteArrayList<>();

	private boolean closed;

	@Override
	public Link addListener(EventListener listener) throws IOException {
		if (!closed && listener instanceof FilteredEventListenerAdapter) {
			Pin pin = ((FilteredEventListenerAdapter) listener).getPin();
			// old impl did start "startListening" on each addListener, so
			// we do too for the moment
			// TODO should/can we change that behavior?
			// if (!hasListenerForPin(pin)) {
			startListening(pin);
			// }
		}
		this.eventListeners.add(listener);
		return this;
	}

	@Override
	public Link removeListener(EventListener listener) throws IOException {
		this.eventListeners.remove(listener);
		if (!closed && listener instanceof FilteredEventListenerAdapter) {
			Pin pin = ((FilteredEventListenerAdapter) listener).getPin();
			if (!hasListenerForPin(pin)) {
				stopListening(pin);
			}
		}
		return this;
	}

	@Override
	public Link addRplyListener(RplyListener listener) throws IOException {
		this.rplyListeners.add(listener);
		return this;
	}

	@Override
	public Link removeRplyListener(RplyListener listener) throws IOException {
		this.rplyListeners.remove(listener);
		return this;
	}

	public boolean hasRplyListeners() {
		return !rplyListeners.isEmpty();
	}

	@Override
	public Link addCustomListener(CustomListener listener) throws IOException {
		this.customListeners.add(listener);
		return this;
	}

	@Override
	public Link removeCustomListener(CustomListener listener) throws IOException {
		this.customListeners.remove(listener);
		return this;
	}

	public void fireStateChanged(AnalogPinValueChangedEvent event) {
		for (EventListener eventListener : this.eventListeners) {
			try {
				eventListener.stateChanged(event);
			} catch (Exception e) {
				logError(event, "analog change", e);
			}
		}
	}

	public void fireStateChanged(DigitalPinValueChangedEvent event) {
		for (EventListener eventListener : this.eventListeners) {
			try {
				eventListener.stateChanged(event);
			} catch (Exception e) {
				logError(event, "digital change", e);
			}
		}
	}

	public void fireReplyReceived(RplyEvent event) {
		for (RplyListener rplyListener : this.rplyListeners) {
			try {
				rplyListener.rplyReceived(event);
			} catch (Exception e) {
				logError(event, "reply", e);
			}
		}
	}

	public void fireCustomReceived(CustomEvent event) {
		for (CustomListener customListener : this.customListeners) {
			try {
				customListener.customEventReceived(event);
			} catch (Exception e) {
				logError(event, "custom", e);
			}
		}
	}

	public void fireConnectionLost() {
		for (ConnectionListener connectionListener : this.connectionListeners) {
			try {
				connectionListener.connectionLost();
			} catch (Exception e) {
				logger.error("Error while publishing connectionLost event", e);
			}
		}
	}

	public void fireReconnected() {
		for (ConnectionListener connectionListener : this.connectionListeners) {
			try {
				connectionListener.reconnected();
			} catch (Exception e) {
				logger.error("Error while publishing reconnected event", e);
			}
		}
	}

	private boolean hasListenerForPin(Pin pin) {
		for (EventListener listener : this.eventListeners) {
			if (listener instanceof FilteredEventListenerAdapter
					&& pin.equals(((FilteredEventListenerAdapter) listener).getPin())) {
				return true;
			}
		}
		return false;
	}

	private void logError(Object event, String eventType, Exception e) {
		logger.error("Error while publishing " + eventType + " event {}", event, e);
	}

	// TODO make this part of an interface, e.g. ConnectionStateObservable
	public Link addConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.add(connectionListener);
		return this;
	}

	// TODO make this part of an interface, e.g. ConnectionStateObservable
	public Link removeConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.remove(connectionListener);
		return this;
	}

	public void deregisterAllEventListeners() throws IOException {
		for (EventListener eventListener : this.eventListeners) {
			removeListener(eventListener);
		}
	}

	@Override
	public void close() throws IOException {
		this.closed = true;
	}

}
