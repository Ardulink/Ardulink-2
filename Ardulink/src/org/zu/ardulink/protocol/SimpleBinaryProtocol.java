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

package org.zu.ardulink.protocol;

import java.util.logging.Logger;

import org.zu.ardulink.Link;
import org.zu.ardulink.connection.usb.DigisparkUSBConnection;
import org.zu.ardulink.event.IncomingMessageEvent;


/**
 * [ardulinktitle] [ardulinkversion]
 * This is a binary protocol to minimize messages payload. With this protocol tiny devices as Digispark work better
 * than with text protocols as ALProtocol (that is the default).
 * Hovewer this protocol is actually very limited and are implemented only sendPowerPinIntensity and sendPowerPinSwitch
 * methods (without callback feature).
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * @see IProtocol
 * [adsense]
 *
 */
public class SimpleBinaryProtocol implements IProtocol {

	private static Logger logger = Logger.getLogger(SimpleBinaryProtocol.class.getName());

	public static final String NAME = "SimpleBinaryProtocol";

	private static final int POWER_PIN_INTENSITY_MESSAGE = 11;
	private static final int POWER_PIN_SWITCH_MESSAGE = 12;
	
	private static long nextId = 1;

	@Override
	public MessageInfo sendKeyPressEvent(Link link, char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo sendPowerPinIntensity(Link link, int pin, int intensity) {
		MessageInfo retvalue = new MessageInfo();
		synchronized(this) {
			if(link.isConnected()) {
				
				long currentId = nextId++;
				retvalue.setMessageID(currentId);
				
				int[] message = {POWER_PIN_INTENSITY_MESSAGE, 0, 0};
				message[1] = pin;
				message[2] = intensity;
				
				logger.fine("" + message[0] + message[1] + message[2]); 
				
				boolean result = link.writeSerial(message.length, message);
				retvalue.setSent(result);
				retvalue.setMessageSent("unsupported");
			}
		}

		return retvalue;
	}

	@Override
	public MessageInfo sendPowerPinSwitch(Link link, int pin, int power) {
		MessageInfo retvalue = new MessageInfo();
		synchronized(this) {
			if(link.isConnected()) {
				
				long currentId = nextId++;
				retvalue.setMessageID(currentId);
				
				int[] message = {POWER_PIN_SWITCH_MESSAGE, 0, 0};
				message[1] = pin;
				message[2] = power;

				logger.fine("" + message[0] + message[1] + message[2]); 
				
				boolean result = link.writeSerial(message.length, message);
				retvalue.setSent(result);
				retvalue.setMessageSent("unsupported");
			}
		}

		return retvalue;
	}

	@Override
	public MessageInfo sendCustomMessage(Link link, String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo sendKeyPressEvent(Link link, char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex, ReplyMessageCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo sendPowerPinIntensity(Link link, int pin, int intensity, ReplyMessageCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo sendPowerPinSwitch(Link link, int pin, int power, ReplyMessageCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo sendCustomMessage(Link link, String message, ReplyMessageCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IncomingMessageEvent parseMessage(int[] realMsg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo startListenDigitalPin(Link link, int pin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo stopListenDigitalPin(Link link, int pin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo startListenAnalogPin(Link link, int pin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo stopListenAnalogPin(Link link, int pin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo startListenDigitalPin(Link link, int pin, ReplyMessageCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo stopListenDigitalPin(Link link, int pin, ReplyMessageCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo startListenAnalogPin(Link link, int pin, ReplyMessageCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInfo stopListenAnalogPin(Link link, int pin, ReplyMessageCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProtocolName() {
		return NAME;
	}

	@Override
	public ProtocolType getProtocolType() {
		return ProtocolType.BINARY;
	}

	@Override
	public int getIncomingMessageDivider() {
		return DigisparkUSBConnection.DEFAULT_DIVIDER;
	}

	@Override
	public int getOutgoingMessageDivider() {
		return DigisparkUSBConnection.DEFAULT_DIVIDER;
	}
}
