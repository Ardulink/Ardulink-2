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
package org.ardulink.core.messages.api;

import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.CustomEvent;
import org.ardulink.core.events.CustomListener;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.PinValueChangedEvent;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.events.RplyListener;
import org.ardulink.core.messages.events.api.FromDeviceMessageEvent;
import org.ardulink.core.messages.events.api.FromDeviceMessageListener;
import org.ardulink.core.messages.events.impl.DefaultFromDeviceMessageEvent;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessageCustom;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessageReply;
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
public class LinkMessageAdapter {

	private static final Logger logger = LoggerFactory
			.getLogger(LinkMessageAdapter.class);

	private final Link link;
	private final LinkListener linkListener;

	public LinkMessageAdapter(Link link) throws IOException {
		this.link = link;
		this.linkListener = new LinkListener();
		link.addListener(linkListener);
		link.addCustomListener(linkListener);
		link.addRplyListener(linkListener);
	}

	public LinkMessageAdapter addInMessageListener(FromDeviceMessageListener listener)
			throws IOException {
		checkNotNull(listener, "listener must not be null");
		linkListener.addInMessageListener(listener);
		return this;
	}

	public LinkMessageAdapter removeInMessageListener(FromDeviceMessageListener listener)
			throws IOException {
		checkNotNull(listener, "listener must not be null");
		linkListener.removeInMessageListener(listener);
		return this;
	}

	public void sendMessage(ToDeviceMessage message) throws IOException {

		checkNotNull(message, "OutMessage must not be null");

		if (message instanceof ToDeviceMessageCustom) {
			ToDeviceMessageCustom cMessage = (ToDeviceMessageCustom) message;
			sendCustomMessage(cMessage.getMessages());
		} else if (message instanceof ToDeviceMessageKeyPress) {
			ToDeviceMessageKeyPress cMessage = (ToDeviceMessageKeyPress) message;
			sendKeyPressEvent(cMessage.getKeychar(), cMessage.getKeycode(),
					cMessage.getKeylocation(), cMessage.getKeymodifiers(),
					cMessage.getKeymodifiersex());
		} else if (message instanceof ToDeviceMessagePinStateChange) {
			ToDeviceMessagePinStateChange cMessage = (ToDeviceMessagePinStateChange) message;
			Pin pin = cMessage.getPin();
			if (pin.is(ANALOG)) {
				switchAnalogPin((Pin.AnalogPin) pin,
						(Integer) cMessage.getValue());
			} else if (pin.is(DIGITAL)) {
				switchDigitalPin((Pin.DigitalPin) pin,
						(Boolean) cMessage.getValue());
			} else {
				throw new IllegalStateException("Unknown pin type "
						+ pin.getType());
			}
		} else if (message instanceof ToDeviceMessageStartListening) {
			ToDeviceMessageStartListening cMessage = (ToDeviceMessageStartListening) message;
			startListening(cMessage.getPin());
		} else if (message instanceof ToDeviceMessageStopListening) {
			ToDeviceMessageStopListening cMessage = (ToDeviceMessageStopListening) message;
			stopListening(cMessage.getPin());
		} else if (message instanceof ToDeviceMessageTone) {
			ToDeviceMessageTone cMessage = (ToDeviceMessageTone) message;
			sendTone(cMessage.getTone());
		} else if (message instanceof ToDeviceMessageNoTone) {
			ToDeviceMessageNoTone cMessage = (ToDeviceMessageNoTone) message;
			sendNoTone(cMessage.getAnalogPin());
		} else {
			throw new IllegalStateException(String.format(
					"OutMessage type not supported. %s", message.getClass()
							.getCanonicalName()));
		}

	}

	private void sendCustomMessage(String... messages) throws IOException {
		link.sendCustomMessage(messages);
	}

	private void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		link.sendKeyPressEvent(keychar, keycode, keylocation, keymodifiers,
				keymodifiersex);
	}

	private void switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		link.switchAnalogPin(analogPin, value);
	}

	private void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		link.switchDigitalPin(digitalPin, value);
	}

	private void startListening(Pin pin) throws IOException {
		link.startListening(pin);
	}

	private void stopListening(Pin pin) throws IOException {
		link.stopListening(pin);
	}

	private void sendTone(Tone tone) throws IOException {
		link.sendTone(tone);
	}

	private void sendNoTone(AnalogPin analogPin) throws IOException {
		link.sendNoTone(analogPin);
	}

	private static class LinkListener implements CustomListener, RplyListener,
			EventListener {

		private final List<FromDeviceMessageListener> inMessageListeners = new CopyOnWriteArrayList<FromDeviceMessageListener>();

		@Override
		public void stateChanged(AnalogPinValueChangedEvent event) {
			stateChanged((PinValueChangedEvent) event);
		}

		@Override
		public void stateChanged(DigitalPinValueChangedEvent event) {
			stateChanged((PinValueChangedEvent) event);
		}

		private void stateChanged(PinValueChangedEvent event) {
			FromDeviceMessage inMessage = new DefaultFromDeviceMessagePinStateChanged(
					event.getPin(), event.getValue());
			FromDeviceMessageEvent inMessageEvent = new DefaultFromDeviceMessageEvent(inMessage);

			fireInMessageReceived(inMessageEvent);
		}

		@Override
		public void rplyReceived(RplyEvent event) {
			FromDeviceMessage inMessage = new DefaultFromDeviceMessageReply(event.isOk(),
					event.getId());
			FromDeviceMessageEvent inMessageEvent = new DefaultFromDeviceMessageEvent(inMessage);

			fireInMessageReceived(inMessageEvent);
		}

		@Override
		public void customEventReceived(CustomEvent event) {
			FromDeviceMessage inMessage = new DefaultFromDeviceMessageCustom(event.getValue());
			FromDeviceMessageEvent inMessageEvent = new DefaultFromDeviceMessageEvent(inMessage);

			fireInMessageReceived(inMessageEvent);
		}

		public void addInMessageListener(FromDeviceMessageListener listener)
				throws IOException {
			this.inMessageListeners.add(listener);
		}

		public void removeInMessageListener(FromDeviceMessageListener listener)
				throws IOException {
			this.inMessageListeners.remove(listener);
		}

		public void fireInMessageReceived(FromDeviceMessageEvent event) {
			for (FromDeviceMessageListener listener : this.inMessageListeners) {
				try {
					listener.fromDeviceMessageReceived(event);
				} catch (Exception e) {
					logger.error("InMessageListener {} failure", listener, e);
				}
			}
		}
	}

}
