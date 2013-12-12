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

import org.zu.ardulink.Link;
import org.zu.ardulink.event.IncomingMessageEvent;

/**
 * [ardulinktitle] [ardulinkversion]
 * This interface defines all the messages that can be sent to the Arduino
 * and provides a method to analyze all messages from Arduino.
 * 
 * For each message sent the caller can set a callback class. In this way the caller can know asynchronously if 
 * a message has been processed by Arduino board.
 * If callback class is not setted (or is null) then arduino should not send a reply message because caller
 * is not interested in replies.
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * @see ProtocolHandler
 * @see Link
 * @see ALProtocol
 * 
 * [adsense]
 *
 */
public interface IProtocol {

	public static final int POWER_HIGH = 1;
	public static final int POWER_LOW = 0;

	public static final int UNDEFINED_ID = -1;
	public static final int UNDEFINED_REPLY = -1;
	public static final int REPLY_OK = 1;
	public static final int REPLY_KO = 0;
	
	/**
	 * Sends information about which key was pressed.
	 * @param link
	 * @param keychar
	 * @param keycode
	 * @param keylocation
	 * @param keymodifiers
	 * @param keymodifiersex
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo sendKeyPressEvent(Link link, char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex);
	
	/**
	 * Sends the request to set a PWM type pin to a certain intensity. Values must be between 0 and 255.
	 * Arduino should perform an analogWrite(pin, intensity)
	 * @param link
	 * @param pin
	 * @param intensity
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo sendPowerPinIntensity(Link link, int pin, int intensity);
	
	/**
	 * Sends the request to set a pin to HIGH or LOW power.
	 * Arduino should perform a digitalWrite(pin, power)
	 * @param link
	 * @param pin
	 * @param power
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo sendPowerPinSwitch(Link link, int pin, int power);
	
	/**
	 * Sends a custom message used for specific actions in Arduino sketches
	 * @param link
	 * @param message
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo sendCustomMessage(Link link, String message);

	/**
	 * Sends information about which key was pressed.
	 * @param link
	 * @param keychar
	 * @param keycode
	 * @param keylocation
	 * @param keymodifiers
	 * @param keymodifiersex
	 * @param callback
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo sendKeyPressEvent(Link link, char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex, ReplyMessageCallback callback);

	/**
	 * Sends the request to set a PWM type pin to a certain intensity. Values must be between 0 and 255.
	 * Arduino should perform an analogWrite(pin, intensity)
	 * @param link
	 * @param pin
	 * @param intensity
	 * @param callback
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo sendPowerPinIntensity(Link link, int pin, int intensity, ReplyMessageCallback callback);
	
	/**
	 * Sends the request to set a pin to HIGH or LOW power.
	 * Arduino should perform a digitalWrite(pin, power)
	 * @param link
	 * @param pin
	 * @param power
	 * @param callback
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo sendPowerPinSwitch(Link link, int pin, int power, ReplyMessageCallback callback);

	/**
	 * Sends a custom message used for specific actions in Arduino sketches
	 * @param link
	 * @param message
	 * @param callback
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo sendCustomMessage(Link link, String message, ReplyMessageCallback callback);

	/**
	 * When a message arrives from Arduino, NetworkInterfaceImpl and Link classes call this method that parses message and
	 * returns a specific event. If message arrived is a reply message then null is returned and a callback action is taken.
	 * Otherwise the caller take specific action based on specific IncomingMessage.
	 * i.e. if a AnalogReadChangeEvent is raised then the caller fire the event to all the listeners.
	 * @param realMsg
	 * @return IncomingMessageEvent dependent from message parsed, null if message is a reply message.
	 */
	public IncomingMessageEvent parseMessage(int[] realMsg);

	/**
	 * Sends the request to listen on a specific pin. After calling this method, Arduino
	 * should start sending messages about the value read from the specified pin (digitalRead).
	 * @param link
	 * @param pin
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo startListenDigitalPin(Link link, int pin);

	/**
	 * Sends the request to not listen on a specific pin. After calling this method, Arduino
	 * should stop sending messages about the value read from the specified pin (digitalRead).
	 * @param link
	 * @param pin
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo stopListenDigitalPin(Link link, int pin);

	/**
	 * Sends the request to listen on a specific analog pin. After calling this method, Arduino
	 * should start sending messages about the value read from the specified pin (analogRead).
	 * @param link
	 * @param pin
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo startListenAnalogPin(Link link, int pin);

	/**
	 * Sends the request to not listen on a specific analog pin. After calling this method, Arduino
	 * should stop sending messages about the value read from the specified pin (analogRead).
	 * @param link
	 * @param pin
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo stopListenAnalogPin(Link link, int pin);

	/**
	 * Sends the request to listen on a specific pin. After calling this method, Arduino
	 * should start sending messages about the value read from the specified pin (digitalRead).
	 * @param link
	 * @param pin
	 * @param callback
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo startListenDigitalPin(Link link, int pin, ReplyMessageCallback callback);

	/**
	 * Sends the request to not listen on a specific pin. After calling this method, Arduino
	 * should stop sending messages about the value read from the specified pin (digitalRead).
	 * @param link
	 * @param pin
	 * @param callback
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo stopListenDigitalPin(Link link, int pin, ReplyMessageCallback callback);

	/**
	 * Sends the request to listen on a specific analog pin. After calling this method, Arduino
	 * should start sending messages about the value read from the specified pin (analogRead).
	 * @param link
	 * @param pin
	 * @param callback
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo startListenAnalogPin(Link link, int pin, ReplyMessageCallback callback);

	/**
	 * Sends the request to not listen on a specific analog pin. After calling this method, Arduino
	 * should stop sending messages about the value read from the specified pin (analogRead).
	 * @param link
	 * @param pin
	 * @param callback
	 * @return a MessageInfo containing the success or failure (for comunication)
	 */
	public MessageInfo stopListenAnalogPin(Link link, int pin, ReplyMessageCallback callback);
	
	
	/**
	 * 
	 * @return the protocol name
	 */
	public String getProtocolName();
	
}
