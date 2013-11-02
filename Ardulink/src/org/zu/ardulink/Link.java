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

import gnu.io.net.Network;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zu.ardulink.event.AnalogReadChangeListener;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DigitalReadChangeListener;
import org.zu.ardulink.event.IncomingMessageEvent;
import org.zu.ardulink.protocol.IProtocol;
import org.zu.ardulink.protocol.LoggerReplyMessageCallback;
import org.zu.ardulink.protocol.MessageInfo;
import org.zu.ardulink.protocol.ProtocolHandler;
import org.zu.ardulink.protocol.ReplyMessageCallback;

public class Link {
	public static final int DEFAULT_BAUDRATE = 115200;
	public static final String DEFAULT_LINK_NAME = "DEFAULT_LINK";
	
	private static Map<String, Link> links = Collections.synchronizedMap(new HashMap<String, Link>());
	
	static {
		createInstance(DEFAULT_LINK_NAME);
	}
	
	private NetworkInterfaceImpl networkInterface = new NetworkInterfaceImpl(this);
	private Network network = null;
	private String connectedPortName = null;
	private int connectedBaudRate = DEFAULT_BAUDRATE;
	private String name;
	
	private LoggerReplyMessageCallback loggerCallback = new LoggerReplyMessageCallback();
	private IProtocol protocol;
	
	public static Link getDefaultInstance() {
		return getInstance(DEFAULT_LINK_NAME);
	}

	public static Link getInstance(String linkName) {
		if(linkName == null) {
			linkName = DEFAULT_LINK_NAME;
		}
		return links.get(linkName);
	}

	public static Link createInstance(String linkName) {
		return createInstance(linkName, ProtocolHandler.getCurrentProtocolImplementation().getProtocolName());
	}

	public static Link createInstance(String linkName, String protocolName) {
		Link link = getInstance(linkName);
		if(link == null) {
			IProtocol protocol = ProtocolHandler.getProtocolImplementation(protocolName);
			link = new Link(linkName, protocol);
			links.put(linkName, link);
		} else {
			throw new RuntimeException("Instance " + linkName + " already created.");
		}
		return link;
	}

	private Link(String linkName, IProtocol protocol) {
		this.network = new Network(linkName, networkInterface);
		this.name = linkName;
		this.protocol = protocol;
	}
	
	public boolean connect(String portName) {
		return connect(portName, DEFAULT_BAUDRATE);
	}

	public boolean connect(String portName, int baudRate) {
		if(network.isConnected()) {
			network.disconnect();
		}
		
		boolean retvalue = network.connect(portName, baudRate);
		if(retvalue == true) {
			connectedPortName = portName;
			connectedBaudRate = baudRate;
		}
		return retvalue;
	}

	public List<String> getPortList() {
		return network.getPortList();
	}

	public boolean disconnect() {
		boolean retvalue = false;
		if(network.isConnected()) {
			retvalue = network.disconnect();
			connectedPortName = null;
			connectedBaudRate = DEFAULT_BAUDRATE;
		}

		return retvalue;
	}

	public boolean isConnected() {
		return network.isConnected();
	}

	public boolean writeSerial(String message) {
		return network.writeSerial(message);
	}

	public boolean writeSerial(int numBytes, int[] message) {
		return network.writeSerial(numBytes, message);
	}
	
	public boolean addConnectionListener(ConnectionListener connectionListener) {
		return networkInterface.addConnectionListener(connectionListener);
	}

	public boolean removeConnectionListener(ConnectionListener connectionListener) {
		return networkInterface.removeConnectionListener(connectionListener);
	}
	
	public boolean addAnalogReadChangeListener(AnalogReadChangeListener listener) {
		return networkInterface.addAnalogReadChangeListener(listener);
	}

	public boolean removeAnalogReadChangeListener(AnalogReadChangeListener listener) {
		return networkInterface.removeAnalogReadChangeListener(listener);
	}

	public boolean addDigitalReadChangeListener(DigitalReadChangeListener listener) {
		return networkInterface.addDigitalReadChangeListener(listener);
	}

	public boolean removeDigitalReadChangeListener(DigitalReadChangeListener listener) {
		return networkInterface.removeDigitalReadChangeListener(listener);
	}

	public LoggerReplyMessageCallback getLoggerCallback() {
		return loggerCallback;
	}

	public String getConnectedPortName() {
		return connectedPortName;
	}

	public int getConnectedBaudRate() {
		return connectedBaudRate;
	}

	public String getName() {
		return name;
	}

	public MessageInfo sendKeyPressEvent(char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex) {
		return protocol.sendKeyPressEvent(this, keychar, keycode, keylocation, keymodifiers, keymodifiersex);
	}

	public MessageInfo sendPowerPinIntensity(int pin, int intensity) {
		return protocol.sendPowerPinIntensity(this, pin, intensity);
	}

	public MessageInfo sendPowerPinSwitch(int pin, int power) {
		return protocol.sendPowerPinSwitch(this, pin, power);
	}

	public MessageInfo sendKeyPressEvent(char keychar, int keycode,	int keylocation, int keymodifiers, int keymodifiersex, ReplyMessageCallback callback) {
		return protocol.sendKeyPressEvent(this, keychar, keycode, keylocation, keymodifiers, keymodifiersex, callback);
	}

	public MessageInfo sendPowerPinIntensity(int pin, int intensity, ReplyMessageCallback callback) {
		return protocol.sendPowerPinIntensity(this, pin, intensity, callback);
	}

	public MessageInfo sendPowerPinSwitch(int pin, int power, ReplyMessageCallback callback) {
		return protocol.sendPowerPinSwitch(this, pin, power, callback);
	}

	public MessageInfo startListenDigitalPin(int pin) {
		return protocol.startListenDigitalPin(this, pin);
	}

	public MessageInfo stopListenDigitalPin(int pin) {
		return protocol.stopListenDigitalPin(this, pin);
	}

	public MessageInfo startListenAnalogPin(int pin) {
		return protocol.startListenAnalogPin(this, pin);
	}

	public MessageInfo stopListenAnalogPin(int pin) {
		return protocol.stopListenAnalogPin(this, pin);
	}

	public MessageInfo startListenDigitalPin(int pin, ReplyMessageCallback callback) {
		return protocol.startListenDigitalPin(this, pin, callback);
	}

	public MessageInfo stopListenDigitalPin(int pin, ReplyMessageCallback callback) {
		return protocol.stopListenDigitalPin(this, pin, callback);
	}

	public MessageInfo startListenAnalogPin(int pin,
			ReplyMessageCallback callback) {
		return protocol.startListenAnalogPin(this, pin, callback);
	}

	public MessageInfo stopListenAnalogPin(int pin,
			ReplyMessageCallback callback) {
		return protocol.stopListenAnalogPin(this, pin, callback);
	}

	public String getProtocolName() {
		return protocol.getProtocolName();
	}

	public IncomingMessageEvent parseMessage(int[] realMsg) {
		return protocol.parseMessage(realMsg);
	}
}
