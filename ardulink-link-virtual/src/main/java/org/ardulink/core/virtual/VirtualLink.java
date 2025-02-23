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
package org.ardulink.core.virtual;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.proto.api.MessageIdHolders;
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
public class VirtualLink extends AbstractListenerLink {

	private final Logger logger = LoggerFactory.getLogger(VirtualLink.class);

	private final SecureRandom secureRandom = new SecureRandom();

	private final Map<Pin, Object> listeningPins = new ConcurrentHashMap<>();

	private final Timer timer = new Timer();

	public VirtualLink(VirtualLinkConfig config) {
		this.timer.scheduleAtFixedRate(task(), 0, MILLISECONDS.convert(config.delay, config.delayUnit));
	}

	private TimerTask task() {
		return new TimerTask() {
			@Override
			public void run() {
				sendRandomPinStates();
			}
		};
	}

	private void sendRandomPinStates() {
		for (Entry<Pin, Object> entry : listeningPins.entrySet()) {
			Pin pin = entry.getKey();
			if (pin.is(ANALOG)) {
				fireStateChanged(analogPinValueChanged((AnalogPin) pin, getRandomAnalog()));
			} else if (pin.is(DIGITAL)) {
				fireStateChanged(digitalPinValueChanged((DigitalPin) pin, getRandomDigital()));
			}
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		this.timer.cancel();
	}

	@Override
	public long startListening(Pin pin) throws IOException {
		this.listeningPins.put(pin, getRandomValue(pin));
		return MessageIdHolders.NO_ID.getId();
	}

	private Object getRandomValue(Pin pin) {
		if (pin.is(ANALOG)) {
			return getRandomAnalog();
		}
		if (pin.is(DIGITAL)) {
			return getRandomDigital();
		}
		throw new IllegalStateException("Cannot handle pin type " + pin.getType() + " of pin " + pin);
	}

	private Integer getRandomAnalog() {
		return secureRandom.nextInt(1024);
	}

	private Boolean getRandomDigital() {
		return secureRandom.nextBoolean();
	}

	@Override
	public long stopListening(Pin pin) throws IOException {
		this.listeningPins.remove(pin);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long switchAnalogPin(AnalogPin analogPin, int value) throws IOException {
		logger.info("{} set to {}", analogPin, value);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long switchDigitalPin(DigitalPin digitalPin, boolean value) throws IOException {
		logger.info("{} set to {}", digitalPin, value);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long sendKeyPressEvent(char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex)
			throws IOException {
		logger.info("key pressed ({} {} {} {} {})", keychar, keycode, keylocation, keymodifiers, keymodifiersex);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long sendTone(Tone tone) throws IOException {
		logger.info("tone {}", tone);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long sendNoTone(AnalogPin analogPin) throws IOException {
		logger.info("no tone on {}", analogPin);
		return MessageIdHolders.NO_ID.getId();

	}

	@Override
	public long sendCustomMessage(String... messages) throws IOException {
		logger.info("custom message {}", Arrays.asList(messages));
		return -1;
	}

}
