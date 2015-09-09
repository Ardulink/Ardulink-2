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

package org.zu.ardulink.event;

import org.zu.ardulink.protocol.IProtocol;

/**
 * [ardulinktitle] [ardulinkversion]
 * Abstract class with info about message incoming to ardulink from arduino board.
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public abstract class IncomingMessageEvent {
	
	public static final int UNDEFINED_PIN = -1;
	public static final int UNDEFINED_VALUE = -1;
	public static final int POWER_HIGH = IProtocol.POWER_HIGH;
	public static final int POWER_LOW = IProtocol.POWER_LOW;
	
	private int pin = UNDEFINED_PIN;
	private int value = UNDEFINED_VALUE;
	private String incomingMessage = null;

	public IncomingMessageEvent() {
	}
	
	public IncomingMessageEvent(int pin, int value, String incomingMessage) {
		super();
		this.pin = pin;
		this.value = value;
		this.incomingMessage = incomingMessage;
	}

	public int getPin() {
		return pin;
	}
	public void setPin(int pin) {
		this.pin = pin;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getIncomingMessage() {
		return incomingMessage;
	}
	public void setIncomingMessage(String incomingMessage) {
		this.incomingMessage = incomingMessage;
	}
}
