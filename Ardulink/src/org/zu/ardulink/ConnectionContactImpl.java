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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

	private static Logger logger = Logger.getLogger(ConnectionContactImpl.class.getName());
	
	private Link link;

	private Set<ConnectionListener> connectionListeners = Collections.synchronizedSet(new HashSet<ConnectionListener>());
	private Set<RawDataListener> rawDataListeners = Collections.synchronizedSet(new HashSet<RawDataListener>());
	private Map<Integer, Set<AnalogReadChangeListener>> analogReadChangeListeners = Collections.synchronizedMap(new HashMap<Integer, Set<AnalogReadChangeListener>>());
	private Map<Integer, Set<DigitalReadChangeListener>> digitalReadChangeListeners = Collections.synchronizedMap(new HashMap<Integer, Set<DigitalReadChangeListener>>());
	
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
	
	private Iterator<ConnectionListener> getConnectionListenersIterator() {
		return connectionListeners.iterator();
	}
	
	public ConnectionContactImpl(Link link) {
		super();
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
		boolean retvalue = false;
		int pinListening = listener.getPinListening();
		synchronized (analogReadChangeListeners) {
			Set<AnalogReadChangeListener> pinListeningSet = analogReadChangeListeners.get(pinListening);
			if(pinListeningSet == null) {
				pinListeningSet = Collections.synchronizedSet(new HashSet<AnalogReadChangeListener>());
				analogReadChangeListeners.put(pinListening, pinListeningSet);
			}
			retvalue = pinListeningSet.add(listener);
			if(pinListening != AnalogReadChangeListener.ALL_PINS) {
				link.startListenAnalogPin(pinListening);
			}
		}
		
		return retvalue;
	}

	/**
	 * Remove a AnalogReadChangeListener from the event notification set.
	 * Call a stopListenAnalogPin if this is the last remove element.
	 * @param listener
	 * @return true if this set contained the specified AnalogReadChangeListener
	 * @see Link
	 */
	public boolean removeAnalogReadChangeListener(AnalogReadChangeListener listener) {
		boolean retvalue = true;
		int pinListening = listener.getPinListening();
		synchronized (analogReadChangeListeners) {
			Set<AnalogReadChangeListener> pinListeningSet = analogReadChangeListeners.get(pinListening);
			if(pinListeningSet != null) {
				retvalue = pinListeningSet.remove(listener);
			}
			if(pinListeningSet.isEmpty() && pinListening != AnalogReadChangeListener.ALL_PINS) {
				link.stopListenAnalogPin(pinListening);
			}
		}
		
		return retvalue;
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
		boolean retvalue = false;
		int pinListening = listener.getPinListening();
		synchronized (digitalReadChangeListeners) {
			Set<DigitalReadChangeListener> pinListeningSet = digitalReadChangeListeners.get(pinListening);
			if(pinListeningSet == null) {
				pinListeningSet = Collections.synchronizedSet(new HashSet<DigitalReadChangeListener>());
				digitalReadChangeListeners.put(pinListening, pinListeningSet);
			}
			retvalue = pinListeningSet.add(listener);
			if(pinListening != DigitalReadChangeListener.ALL_PINS) {
				link.startListenDigitalPin(pinListening);
			}
		}
		
		return retvalue;
	}

	/**
	 * Remove a DigitalReadChangeListener from the event notification set.
	 * Call a stopListenDigitalPin if this is the last remove element.
	 * @param listener
	 * @return true if this set contained the specified DigitalReadChangeListener
	 * @see ConnectionContactImpl
	 */
	public boolean removeDigitalReadChangeListener(DigitalReadChangeListener listener) {
		boolean retvalue = true;
		int pinListening = listener.getPinListening();
		synchronized (digitalReadChangeListeners) {
			Set<DigitalReadChangeListener> pinListeningSet = digitalReadChangeListeners.get(pinListening);
			if(pinListeningSet != null) {
				retvalue = pinListeningSet.remove(listener);
				if(pinListeningSet.isEmpty() && pinListening != DigitalReadChangeListener.ALL_PINS) {
					link.stopListenDigitalPin(pinListening);
				}
			}
		}
		
		return retvalue;
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
		Iterator<RawDataListener> it = rawDataListeners.iterator();
		while(it.hasNext()) {
			it.next().parseInput(id, numBytes, message);
		}
	}

	private void fireAnalogReadChangeEvent(AnalogReadChangeEvent event) {
		int pin = event.getPin();
		Set<AnalogReadChangeListener> pinListeningSet = analogReadChangeListeners.get(pin);
		if(pinListeningSet != null) {
			Iterator<AnalogReadChangeListener> it = pinListeningSet.iterator();
			while(it.hasNext()) {
				it.next().stateChanged(event);
			}
		}
		pinListeningSet = analogReadChangeListeners.get(AnalogReadChangeListener.ALL_PINS);
		if(pinListeningSet != null) {
			Iterator<AnalogReadChangeListener> it = pinListeningSet.iterator();
			while(it.hasNext()) {
				it.next().stateChanged(event);
			}
		}
	}

	private void fireDigitalReadChangeEvent(DigitalReadChangeEvent event) {
		int pin = event.getPin();
		Set<DigitalReadChangeListener> pinListeningSet = digitalReadChangeListeners.get(pin);
		if(pinListeningSet != null) {
			Iterator<DigitalReadChangeListener> it = pinListeningSet.iterator();
			while(it.hasNext()) {
				it.next().stateChanged(event);
			}
		}
		pinListeningSet = digitalReadChangeListeners.get(DigitalReadChangeListener.ALL_PINS);
		if(pinListeningSet != null) {
			Iterator<DigitalReadChangeListener> it = pinListeningSet.iterator();
			while(it.hasNext()) {
				it.next().stateChanged(event);
			}
		}
	}

	@Override
	public void disconnected(String id) {
		logger.fine("disconnected()");
		DisconnectionEvent event = new DisconnectionEvent(id);
		Iterator<ConnectionListener> iterator = getConnectionListenersIterator();
		while(iterator.hasNext()) {
			iterator.next().disconnected(event);
		}
	}
	
	@Override
	public void connected(String id, String portName) {
		logger.fine("connected()");
		ConnectionEvent event = new ConnectionEvent(id, portName);
		Iterator<ConnectionListener> iterator = getConnectionListenersIterator();
		while(iterator.hasNext()) {
			iterator.next().connected(event);
		}
	}

}
