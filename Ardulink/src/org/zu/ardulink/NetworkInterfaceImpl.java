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

import gnu.io.net.Network_iface;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;
import org.zu.ardulink.event.ConnectionEvent;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DigitalReadChangeEvent;
import org.zu.ardulink.event.DigitalReadChangeListener;
import org.zu.ardulink.event.DisconnectionEvent;
import org.zu.ardulink.event.IncomingMessageEvent;

/**
 * Implements the Raphael Blatter's Network interface to integrate RXTX library with Ardulink http://www.ardulink.org/.
 * 
 * This class has methods to manage events.
 * 
 * @author Luciano Zu
 *
 */
public class NetworkInterfaceImpl implements Network_iface {

	private static Logger logger = Logger.getLogger(NetworkInterfaceImpl.class.getName());
	
	private Link link;

	private Set<ConnectionListener> connectionListeners = Collections.synchronizedSet(new HashSet<ConnectionListener>());
	private Map<Integer, Set<AnalogReadChangeListener>> analogReadChangeListeners = Collections.synchronizedMap(new HashMap<Integer, Set<AnalogReadChangeListener>>());
	private Map<Integer, Set<DigitalReadChangeListener>> digitalReadChangeListeners = Collections.synchronizedMap(new HashMap<Integer, Set<DigitalReadChangeListener>>());

	public boolean addConnectionListener(ConnectionListener connectionListener) {
		return connectionListeners.add(connectionListener);
	}

	public boolean removeConnectionListener(ConnectionListener connectionListener) {
		return connectionListeners.remove(connectionListener);
	}
	
	private Iterator<ConnectionListener> getConnectionListenersIterator() {
		return connectionListeners.iterator();
	}
	
	public NetworkInterfaceImpl(Link link) {
		super();
		this.link = link;
	}

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
			if(pinListeningSet.size() == 1  && pinListening != AnalogReadChangeListener.ALL_PINS) {
				link.startListenAnalogPin(pinListening);
			}
		}
		
		return retvalue;
	}

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

	public boolean removeDigitalReadChangeListener(DigitalReadChangeListener listener) {
		boolean retvalue = true;
		int pinListening = listener.getPinListening();
		synchronized (digitalReadChangeListeners) {
			Set<DigitalReadChangeListener> pinListeningSet = digitalReadChangeListeners.get(pinListening);
			if(pinListeningSet != null) {
				retvalue = pinListeningSet.remove(listener);
			}
			if(pinListeningSet.isEmpty() && pinListening != DigitalReadChangeListener.ALL_PINS) {
				link.stopListenDigitalPin(pinListening);
			}
		}
		
		return retvalue;
	}
	
	@Override
	public void writeLog(String id, String text) {
		logger.info(text);
	}

	@Override
	public void parseInput(String id, int numBytes, int[] message) {
		logger.fine("Message from Arduino has arrived.");
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
	public void networkDisconnected(String id) {
		logger.fine("networkDisconnected()");
		DisconnectionEvent event = new DisconnectionEvent(id);
		Iterator<ConnectionListener> iterator = getConnectionListenersIterator();
		while(iterator.hasNext()) {
			iterator.next().disconnected(event);
		}
	}
	
	@Override
	public void networkConnected(String id, String portName) {
		logger.fine("networkConnected()");
		ConnectionEvent event = new ConnectionEvent(id, portName);
		Iterator<ConnectionListener> iterator = getConnectionListenersIterator();
		while(iterator.hasNext()) {
			iterator.next().connected(event);
		}
	}

}
