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

import static org.zu.ardulink.util.Preconditions.checkState;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zu.ardulink.connection.Connection;
import org.zu.ardulink.connection.serial.SerialConnection;
import org.zu.ardulink.event.AnalogReadChangeListener;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DigitalReadChangeListener;
import org.zu.ardulink.event.IncomingMessageEvent;
import org.zu.ardulink.protocol.ALProtocol;
import org.zu.ardulink.protocol.IProtocol;
import org.zu.ardulink.protocol.LoggerReplyMessageCallback;
import org.zu.ardulink.protocol.MessageInfo;
import org.zu.ardulink.protocol.ProtocolHandler;
import org.zu.ardulink.protocol.ReplyMessageCallback;

/**
 * [ardulinktitle] [ardulinkversion]
 * This class represents the connection between the computer and the Arduino board.
 * At the moment the connection is only a serial connection to USB.
 * Any java class must use this class to communicate with Arduino. 
 * To get a class instance you can call a static method to get the default link
 * or require the creation of a specific link.
 * In this way it is possible to simultaneously connect to several Arduino boards.
 * Each link can define a specific protocol or use the protocol ardulink (ALProtocol).
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class Link {
	/**
	 * Default baud rate for serial connections
	 */
	public static final int DEFAULT_BAUDRATE = 115200;
	
	/**
	 * Default Link name
	 * @see getDefaultInstance()
	 * @see getInstance(String linkName)
	 */
	public static final String DEFAULT_LINK_NAME = "DEFAULT_LINK";
	
	private static final Map<String, Link> links = Collections.synchronizedMap(new HashMap<String, Link>());
	
	static {
		createInstance(DEFAULT_LINK_NAME, ALProtocol.NAME);
	}
	
	private final ConnectionContact connectionContact = new ConnectionContact(this);
	private Connection connection;
	private final String name;
	
	private final LoggerReplyMessageCallback loggerCallback = new LoggerReplyMessageCallback();
	private IProtocol protocol;
	
	/**
	 * @return the default Link class instance. It uses ALProtocol.
	 */
	public static Link getDefaultInstance() {
		return getInstance(DEFAULT_LINK_NAME);
	}

	/**
	 * @param linkName the link name. If null the method returns the default instance.
	 * @return a link previously created with createInstance method.
	 */
	public static Link getInstance(String linkName) {
		if(linkName == null) {
			linkName = DEFAULT_LINK_NAME;
		}
		return links.get(linkName);
	}

	/**
	 * Creates a Link instance with a specific name. Link created has the current protocol implementation.
	 * @param linkName
	 * @return Link created
	 * @see ProtocolHandler
	 */
	public static Link createInstance(String linkName) {
		return createInstance(linkName, ProtocolHandler.getCurrentProtocolImplementation().getProtocolName());
	}
	
	/**
	 * Creates a Link instance with a specific name and a specific protocol.
	 * @param linkName
	 * @param protocolName
	 * @return Link created
	 * @see ProtocolHandler
	 */
	public static Link createInstance(String linkName, String protocolName) {
		Link link = getInstance(linkName);
		checkState(link == null, "Instance %s already created.", linkName);
		IProtocol protocol = ProtocolHandler.getProtocolImplementation(protocolName);
		link = new Link(linkName, protocol);
		links.put(linkName, link);
		return link;
	}

	/**
	 * Creates a Link instance with a specific name and a specific connection (default is serial connection)
	 * @param linkName
	 * @param connection
	 * @return Link created
	 */
	public static Link createInstance(String linkName, Connection connection) {
		return createInstance(linkName, ProtocolHandler.getCurrentProtocolImplementation().getProtocolName(), connection);
	}

	/**
	 * Creates a Link instance with a specific name and a specific protocol and a specific connection (default is serial connection)
	 * @param linkName
	 * @param protocolName
	 * @param connection
	 * @return Link created
	 */
	public static Link createInstance(String linkName, String protocolName, Connection connection) {
		Link link = getInstance(linkName);
		if(link == null) {
			IProtocol protocol = ProtocolHandler.getProtocolImplementation(protocolName);
			link = new Link(linkName, protocol, connection);
			links.put(linkName, link);
		}
		return link;
	}
	
	public static Link destroyInstance(String linkName) {
		Link retvalue = links.remove(linkName);
		if(retvalue != null) {
			retvalue.disconnect();
			retvalue.connection = null;
			retvalue.protocol = null;
		}
		return retvalue; 
	}

	private Link(String linkName, IProtocol protocol) {
		this.connection = new SerialConnection(linkName, connectionContact, protocol.getIncomingMessageDivider());
		this.name = linkName;
		this.protocol = protocol;
	}
	
	private Link(String linkName, IProtocol protocol, Connection connection) {
		this.connection = connection;
		connection.setConnectionContact(connectionContact);
		this.name = linkName;
		this.protocol = protocol;
	}

	/**
	 * Connects to Arduino board.
	 * @param params
	 * @return
	 */
	public boolean connect(Object... params) {
		if(connection.isConnected()) {
			connection.disconnect();
		}
		
		boolean retvalue = connection.connect(params);

		return retvalue;
	}

	/**
	 * It works for serial connection links
	 * @return ports available
	 * @see SerialConnection
	 */
	public List<String> getPortList() {
		return connection.getPortList();
	}

	/**
	 * Disconnect from arduino board.
	 * @return true if disconnected
	 */
	public boolean disconnect() {
		boolean retvalue = false;
		// if(connection.isConnected()) {
			retvalue = connection.disconnect();
		// }

		return retvalue;
	}

	/**
	 * @return true if arduino board is connected
	 * @see SerialConnection
	 */
	public boolean isConnected() {
		return connection.isConnected();
	}

	/**
	 * Writes data to arduino
	 * @param message
	 * @return
	 * @see SerialConnection
	 */
	public boolean writeSerial(String message) {
		return connection.writeSerial(message);
	}

	/**
	 * Writes data to arduino
	 * @param numBytes
	 * @param message
	 * @return
	 * @see SerialConnection
	 */
	public boolean writeSerial(int numBytes, int[] message) {
		return connection.writeSerial(numBytes, message);
	}
	
	/**
	 * Register a ConnectionListener to receive events about connection status.
	 * @param connectionListener
	 * @return true if this set did not already contain the specified connectionListener
	 * @see ConnectionContact
	 */
	public boolean addConnectionListener(ConnectionListener connectionListener) {
		return connectionContact.addConnectionListener(connectionListener);
	}

	/**
	 * Remove a ConnectionListener from the event notification set.
	 * @param connectionListener
	 * @return true if this set contained the specified connectionListener
	 * @see ConnectionContact
	 */
	public boolean removeConnectionListener(ConnectionListener connectionListener) {
		return connectionContact.removeConnectionListener(connectionListener);
	}
	
	/**
	 * Register a RawDataListener to receive data from Arduino.
	 * @param rawDataListener
	 * @return true if this set did not already contain the specified rawDataListener
	 * @see ConnectionContact
	 */
	public boolean addRawDataListener(RawDataListener rawDataListener) {
		return connectionContact.addRawDataListener(rawDataListener);
	}

	/**
	 * Remove a RawDataListener from the data notification set.
	 * @param rawDataListener
	 * @return
	 * @see ConnectionContact
	 */
	public boolean removeRawDataListener(RawDataListener rawDataListener) {
		return connectionContact.removeRawDataListener(rawDataListener);
	}

	/**
	 * Register an AnalogReadChangeListener to receive events about analog pin change state.
	 * With this method ardulink is able to receive information from arduino board
	 * @param listener
	 * @return true if this set did not already contain the specified AnalogReadChangeListener
	 * @see ConnectionContact
	 */
	public boolean addAnalogReadChangeListener(AnalogReadChangeListener listener) {
		return connectionContact.addAnalogReadChangeListener(listener);
	}

	/**
	 * Remove a AnalogReadChangeListener from the event notification set.
	 * @param listener
	 * @return true if this set contained the specified AnalogReadChangeListener
	 * @see ConnectionContact
	 */
	public boolean removeAnalogReadChangeListener(AnalogReadChangeListener listener) {
		return connectionContact.removeAnalogReadChangeListener(listener);
	}

	/**
	 * Register an DigitalReadChangeListener to receive events about digital pin change state.
	 * With this method ardulink is able to receive information from arduino board
	 * @param listener
	 * @return true if this set did not already contain the specified DigitalReadChangeListener
	 * @see ConnectionContact
	 */
	public boolean addDigitalReadChangeListener(DigitalReadChangeListener listener) {
		return connectionContact.addDigitalReadChangeListener(listener);
	}

	/**
	 * Remove a DigitalReadChangeListener from the event notification set.
	 * @param listener
	 * @return true if this set contained the specified DigitalReadChangeListener
	 * @see ConnectionContact
	 */
	public boolean removeDigitalReadChangeListener(DigitalReadChangeListener listener) {
		return connectionContact.removeDigitalReadChangeListener(listener);
	}

	/**
	 * @return a simple LoggerCallback implementation just to log messages reply from arduino.
	 */
	public LoggerReplyMessageCallback getLoggerCallback() {
		return loggerCallback;
	}

	/**
	 * @return the link name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Call protocol sendKeyPressEvent with this Link (and without a call back implementation to manage reply message)
	 * @param keychar
	 * @param keycode
	 * @param keylocation
	 * @param keymodifiers
	 * @param keymodifiersex
	 * @return the MessageInfo class
	 */
	public MessageInfo sendKeyPressEvent(char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex) {
		return protocol.sendKeyPressEvent(this, keychar, keycode, keylocation, keymodifiers, keymodifiersex);
	}

	/**
	 * Call protocol sendPowerPinIntensity with this Link (and without a call back implementation to manage reply message)
	 * This method request arduino to perform an analogWrite function call.
	 * @param pin
	 * @param intensity range 0-255
	 * @return the MessageInfo class
	 */
	public MessageInfo sendPowerPinIntensity(int pin, int intensity) {
		return protocol.sendPowerPinIntensity(this, pin, intensity);
	}

	/**
	 * Call protocol sendPowerPinSwitch with this Link (and without a call back implementation to manage reply message)
	 * This method request arduino to perform a digitalWrite function call.
	 * @param pin
	 * @param power IProtocol.POWER_HIGH or IProtocol.POWER_LOW
	 * @return the MessageInfo class
	 */
	public MessageInfo sendPowerPinSwitch(int pin, int power) {
		return protocol.sendPowerPinSwitch(this, pin, power);
	}

	/**
	 * Call protocol sendToneMessage with this Link (and without a call back implementation to manage reply message)
	 * This method request arduino to perform a tone function call.
	 * @param pin
	 * @param frequency
	 * @return the MessageInfo class
	 */
	public MessageInfo sendToneMessage(int pin, int frequency) {
		return protocol.sendToneMessage(this, pin, frequency);
	}

	/**
	 * Call protocol sendToneMessage with this Link (and without a call back implementation to manage reply message)
	 * This method request arduino to perform a tone function call.
	 * @param pin
	 * @param frequency
	 * @param duration
	 * @return the MessageInfo class
	 */
	public MessageInfo sendToneMessage(int pin, int frequency, int duration) {
		return protocol.sendToneMessage(this, pin, frequency, duration);
	}

	/**
	 * Call protocol sendNoToneMessage with this Link (and without a call back implementation to manage reply message)
	 * This method request arduino to perform a noTone function call.
	 * @param pin
	 * @return the MessageInfo class
	 */
	public MessageInfo sendNoToneMessage(int pin) {
		return protocol.sendNoToneMessage(this, pin);
	}

	/**
	 * Call protocol sendCustomMessage with this Link (and without a call back implementation to manage reply message)
	 * Custom message should be used to request specific actions in custom sketches
	 * @param message
	 * @return the MessageInfo class
	 */
	public MessageInfo sendCustomMessage(String message) {
		return protocol.sendCustomMessage(this, message);
	}

	/**
	 * Call protocol sendKeyPressEvent with this Link.
	 * Arduino should send a reply message back. You can manage this callback with a specific implementation. 
	 * @param keychar
	 * @param keycode
	 * @param keylocation
	 * @param keymodifiers
	 * @param keymodifiersex
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo sendKeyPressEvent(char keychar, int keycode,	int keylocation, int keymodifiers, int keymodifiersex, ReplyMessageCallback callback) {
		return protocol.sendKeyPressEvent(this, keychar, keycode, keylocation, keymodifiers, keymodifiersex, callback);
	}

	/**
	 * Call protocol sendPowerPinIntensity with this Link.
	 * This method request arduino to perform an analogWrite function call.
	 * Arduino should send a reply message back. You can manage this callback with a specific implementation. 
	 * @param pin
	 * @param intensity
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo sendPowerPinIntensity(int pin, int intensity, ReplyMessageCallback callback) {
		return protocol.sendPowerPinIntensity(this, pin, intensity, callback);
	}

	/**
	 * Call protocol sendPowerPinSwitch with this Link.
	 * This method request arduino to perform a digitalWrite function call.
	 * Arduino should send a reply message back. You can manage this callback with a specific implementation. 
	 * @param pin
	 * @param power IProtocol.HIGH or IProtocol.LOW
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo sendPowerPinSwitch(int pin, int power, ReplyMessageCallback callback) {
		return protocol.sendPowerPinSwitch(this, pin, power, callback);
	}
	
	/**
	 * Call protocol sendToneMessage with this Link
	 * This method request arduino to perform a tone function call.
	 * Arduino should send a reply message back. You can manage this callback with a specific implementation. 
	 * @param pin
	 * @param frequency
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo sendToneMessage(int pin, int frequency, ReplyMessageCallback callback) {
		return protocol.sendToneMessage(this, pin, frequency, callback);
	}

	/**
	 * Call protocol sendToneMessage with this Link
	 * This method request arduino to perform a tone function call.
	 * Arduino should send a reply message back. You can manage this callback with a specific implementation. 
	 * @param pin
	 * @param frequency
	 * @param duration
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo sendToneMessage(int pin, int frequency, int duration, ReplyMessageCallback callback) {
		return protocol.sendToneMessage(this, pin, frequency, duration,	callback);
	}

	/**
	 * Call protocol sendNoToneMessage with this Link
	 * This method request arduino to perform a noTone function call.
	 * Arduino should send a reply message back. You can manage this callback with a specific implementation. 
	 * @param pin
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo sendNoToneMessage(int pin, ReplyMessageCallback callback) {
		return protocol.sendNoToneMessage(this, pin, callback);
	}

	/**
	 * Call protocol sendCustomMessage with this Link.
	 * Custom message should be used to request specific actions in custom sketches
	 * @param message
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo sendCustomMessage(String message, ReplyMessageCallback callback) {
		return protocol.sendCustomMessage(this, message, callback);
	}

	/**
	 * Call protocol startListenDigitalPin with this Link (and without a call back implementation to manage reply message).
	 * This method request arduino to send messages about digital pin change value. It's called when a DigitalReadChangeListener is added
	 * so you don't need to call this method directly.
	 * @param pin
	 * @return the MessageInfo class
	 */
	public MessageInfo startListenDigitalPin(int pin) {
		return protocol.startListenDigitalPin(this, pin);
	}

	/**
	 * Call protocol stopListenDigitalPin with this Link (and without a call back implementation to manage reply message).
	 * This method request arduino to stop send messages about digital pin change value. It's called when the last DigitalReadChangeListener is removed
	 * so you don't need to call this method directly.
	 * @param pin
	 * @return the MessageInfo class
	 */
	public MessageInfo stopListenDigitalPin(int pin) {
		return protocol.stopListenDigitalPin(this, pin);
	}

	/**
	 * Call protocol startListenAnalogPin with this Link (and without a call back implementation to manage reply message).
	 * This method request arduino to send messages about analog pin change value. It's called when an AnalogReadChangeListener is added
	 * so you don't need to call this method directly.
	 * @param pin
	 * @return the MessageInfo class
	 */
	public MessageInfo startListenAnalogPin(int pin) {
		return protocol.startListenAnalogPin(this, pin);
	}

	/**
	 * Call protocol stopListenAnalogPin with this Link (and without a call back implementation to manage reply message).
	 * This method request arduino to stop send messages about analog pin change value. It's called when the last AnalogReadChangeListener is removed
	 * so you don't need to call this method directly.
	 * @param pin
	 * @return the MessageInfo class
	 */
	public MessageInfo stopListenAnalogPin(int pin) {
		return protocol.stopListenAnalogPin(this, pin);
	}

	/**
	 * As startListenDigitalPin(int pin) but with the possibility to add a callback.
	 * @param pin
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo startListenDigitalPin(int pin, ReplyMessageCallback callback) {
		return protocol.startListenDigitalPin(this, pin, callback);
	}

	/**
	 * As stopListenDigitalPin(int pin) but with the possibility to add a callback.
	 * @param pin
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo stopListenDigitalPin(int pin, ReplyMessageCallback callback) {
		return protocol.stopListenDigitalPin(this, pin, callback);
	}

	/**
	 * As startListenAnalogPin(int pin) but with the possibility to add a callback.
	 * @param pin
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo startListenAnalogPin(int pin, ReplyMessageCallback callback) {
		return protocol.startListenAnalogPin(this, pin, callback);
	}

	/**
	 * As stopListenAnalogPin(int pin) but with the possibility to add a callback.
	 * @param pin
	 * @param callback
	 * @return the MessageInfo class
	 */
	public MessageInfo stopListenAnalogPin(int pin, ReplyMessageCallback callback) {
		return protocol.stopListenAnalogPin(this, pin, callback);
	}

	/**
	 * @return the protocol name
	 */
	public String getProtocolName() {
		return protocol.getProtocolName();
	}

	/**
	 * Parse a message sent from arduino. This method should not called directly.
	 * It calls the specific protocol parseMessage.
	 * @param realMsg
	 * @return
	 */
	public IncomingMessageEvent parseMessage(int[] realMsg) {
		return protocol.parseMessage(realMsg);
	}

	public Connection getConnection() {
		return connection;
	}
}
