/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/

package org.zu.ardulink;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.zu.ardulink.connection.ConnectionContact;
import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;
import org.zu.ardulink.event.ConnectionEvent;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DigitalReadChangeEvent;
import org.zu.ardulink.event.DigitalReadChangeListener;
import org.zu.ardulink.event.DisconnectionEvent;
import org.zu.ardulink.event.IncomingMessageEvent;
import org.zu.ardulink.util.SetMultiMap;

/**
 * [ardulinktitle] [ardulinkversion]
 * Implements the Raphael Blatter's Network interface (a little modified) to integrate RXTX
 * library with Ardulink http://www.ardulink.org/.
 * 
 * This class implements other methods to manage events about messages from arduino board.
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ConnectionContactImpl implements ConnectionContact {

	private static final Logger logger = Logger.getLogger(ConnectionContactImpl.class.getName());
	
	private final Link link;

	private final Set<ConnectionListener> connectionListeners = Collections.synchronizedSet(new HashSet<ConnectionListener>());
	private final Set<RawDataListener> rawDataListeners = Collections.synchronizedSet(new HashSet<RawDataListener>());
	private final SetMultiMap<Integer, AnalogReadChangeListener> analogReadChangeListeners = new SetMultiMap<Integer, AnalogReadChangeListener>();
	private final SetMultiMap<Integer, DigitalReadChangeListener> digitalReadChangeListeners = new SetMultiMap<Integer, DigitalReadChangeListener>();
	
	/**
	 * Register a ConnectionListener to receive events about connection status.
	 * @param connectionListener
	 * @return true if this set did not already contain the specified connectionListener
	 * @see Link
	 */
	public boolean addConnectionListener(ConnectionListener connectionListener) {
		return connectionListeners.add(connectionListener);
	}

	/**
	 * Remove a ConnectionListener from the event notification set.
	 * @param connectionListener
	 * @return
	 * @see Link
	 */
	public boolean removeConnectionListener(ConnectionListener connectionListener) {
		return connectionListeners.remove(connectionListener);
	}
	
	/**
	 * Register a RawDataListener to receive data from Arduino.
	 * @param rawDataListener
	 * @return true if this set did not already contain the specified rawDataListener
	 * @see Link
	 */
	public boolean addRawDataListener(RawDataListener rawDataListener) {
		return rawDataListeners.add(rawDataListener);
	}

	/**
	 * Remove a RawDataListener from the data notification set.
	 * @param rawDataListener
	 * @return
	 * @see Link
	 */
	public boolean removeRawDataListener(RawDataListener rawDataListener) {
		return rawDataListeners.remove(rawDataListener);
	}
	
	public ConnectionContactImpl(Link link) {
		this.link = link;
	}
	
	/**
	 * Register an AnalogReadChangeListener to receive events about analog pin change state.
	 * With this method ardulink is able to receive information from arduino board
	 * Call a startListenAnalogPin.
	 * @param listener
	 * @return true if this set did not already contain the specified AnalogReadChangeListener
	 * @see Link
	 */
	public boolean addAnalogReadChangeListener(AnalogReadChangeListener listener) {
		int pinListening = listener.getPinListening();
		synchronized (analogReadChangeListeners) {
			boolean added = analogReadChangeListeners.put(pinListening,
					listener);
			if (pinListening != AnalogReadChangeListener.ALL_PINS) {
				link.startListenAnalogPin(pinListening);
			}
			return added;
		}
	}

	/**
	 * Remove a AnalogReadChangeListener from the event notification set.
	 * Call a stopListenAnalogPin if this is the last remove element.
	 * @param listener
	 * @return true if this set contained the specified AnalogReadChangeListener
	 * @see Link
	 */
	public boolean removeAnalogReadChangeListener(
			AnalogReadChangeListener listener) {
		int pinListening = listener.getPinListening();
		synchronized (analogReadChangeListeners) {
			boolean removed = analogReadChangeListeners.remove(pinListening,
					listener);
			if (removed
					&& pinListening != AnalogReadChangeListener.ALL_PINS
					&& analogReadChangeListeners.asMap().get(pinListening)
							.isEmpty()) {
				link.stopListenAnalogPin(pinListening);
			}
			return removed;
		}
	}

	/**
	 * Register an DigitalReadChangeListener to receive events about digital pin change state.
	 * With this method ardulink is able to receive information from arduino board
	 * Call a startListenAnalogPin.
	 * @param listener
	 * @return true if this set did not already contain the specified DigitalReadChangeListener
	 * @see ConnectionContactImpl
	 */
	public boolean addDigitalReadChangeListener(DigitalReadChangeListener listener) {
		int pinListening = listener.getPinListening();
		synchronized (digitalReadChangeListeners) {
			boolean added = digitalReadChangeListeners.put(pinListening, listener);
			if (pinListening != DigitalReadChangeListener.ALL_PINS) {
				link.startListenDigitalPin(pinListening);
			}
			return added;
		}
	}

	/**
	 * Remove a DigitalReadChangeListener from the event notification set.
	 * Call a stopListenDigitalPin if this is the last remove element.
	 * @param listener
	 * @return true if this set contained the specified DigitalReadChangeListener
	 * @see ConnectionContactImpl
	 */
	public boolean removeDigitalReadChangeListener(DigitalReadChangeListener listener) {
		int pinListening = listener.getPinListening();
		synchronized (digitalReadChangeListeners) {
			boolean removed = digitalReadChangeListeners.remove(pinListening,
					listener);
			if (removed
					&& digitalReadChangeListeners.asMap().get(pinListening)
							.isEmpty()
					&& pinListening != DigitalReadChangeListener.ALL_PINS) {
				link.stopListenDigitalPin(pinListening);
			}
			return removed;
		}
	}
	
	@Override
	public void writeLog(String id, String text) {
		logger.info(text);
	}

	/**
	 * Method invoked by Raphael Blatter's SerialConnection class.
	 * This method call the Link.parseMessage method and if the IncomingMessageEvent is not null fire the event
	 * to the listeners.
	 */
	@Override
	public void parseInput(String id, int numBytes, int[] message) {
		logger.fine("Message from Arduino has arrived.");
		fireDataToRawDataListener(id, numBytes, message);
		int[] realMsg = Arrays.copyOf(message, numBytes);
		// String msg = new String(message, 0, numBytes);
		// logger.fine(msg);
		IncomingMessageEvent event = link.parseMessage(realMsg);
		if (event != null) {
			if(event instanceof AnalogReadChangeEvent) {
				fireAnalogReadChangeEvent((AnalogReadChangeEvent)event);
			} else if(event instanceof DigitalReadChangeEvent) {
				fireDigitalReadChangeEvent((DigitalReadChangeEvent)event);
			}
		}
	}

	private void fireDataToRawDataListener(String id, int numBytes, int[] message) {
		for (RawDataListener rawDataListener : rawDataListeners) {
			rawDataListener.parseInput(id, numBytes, message);
		}
	}

	private void fireAnalogReadChangeEvent(AnalogReadChangeEvent event) {
		int pin = event.getPin();
		synchronized (analogReadChangeListeners) {
			Map<Integer, Set<AnalogReadChangeListener>> listeners = analogReadChangeListeners
					.asMap();
			Set<AnalogReadChangeListener> pinListeningSet = listeners.get(pin);
			if (pinListeningSet != null) {
				for (AnalogReadChangeListener analogReadChangeListener : pinListeningSet) {
					analogReadChangeListener.stateChanged(event);
				}
			}
			pinListeningSet = listeners.get(AnalogReadChangeListener.ALL_PINS);
			if (pinListeningSet != null) {
				for (AnalogReadChangeListener analogReadChangeListener : pinListeningSet) {
					analogReadChangeListener.stateChanged(event);
				}
			}
		}
	}

	private void fireDigitalReadChangeEvent(DigitalReadChangeEvent event) {
		int pin = event.getPin();
		synchronized (digitalReadChangeListeners) {
			Map<Integer, Set<DigitalReadChangeListener>> listeners = digitalReadChangeListeners
					.asMap();
			Set<DigitalReadChangeListener> pinListeningSet = listeners.get(pin);
			if (pinListeningSet != null) {
				for (DigitalReadChangeListener digitalReadChangeListener : pinListeningSet) {
					digitalReadChangeListener.stateChanged(event);
				}
			}
			pinListeningSet = listeners.get(DigitalReadChangeListener.ALL_PINS);
			if (pinListeningSet != null) {
				for (DigitalReadChangeListener digitalReadChangeListener : pinListeningSet) {
					digitalReadChangeListener.stateChanged(event);
				}
			}
		}
	}

	@Override
	public void disconnected(String id) {
		logger.fine("disconnected()");
		DisconnectionEvent event = new DisconnectionEvent(id);
		for (ConnectionListener connectionListener : connectionListeners) {
			connectionListener.disconnected(event);
		}
	}
	
	@Override
	public void connected(String id, String portName) {
		logger.fine("connected()");
		ConnectionEvent event = new ConnectionEvent(id, portName);
		for (ConnectionListener connectionListener : connectionListeners) {
			connectionListener.connected(event);
		}
	}

}
