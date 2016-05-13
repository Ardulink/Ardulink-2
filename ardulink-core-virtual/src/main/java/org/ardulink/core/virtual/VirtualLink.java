package org.ardulink.core.virtual;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Pin.Type;
import org.ardulink.core.Tone;
import org.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import org.ardulink.core.linkmanager.LinkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualLink extends AbstractListenerLink {

	private final Logger logger = LoggerFactory.getLogger(VirtualLink.class);

	private final SecureRandom secureRandom = new SecureRandom();

	private final Thread thread = new Thread() {

		{
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			while (true) {
				for (Entry<Pin, Object> entry : listeningPins.entrySet()) {
					Pin pin = entry.getKey();
					if (pin.is(Type.ANALOG)) {
						fireStateChanged(new DefaultAnalogPinValueChangedEvent(
								(AnalogPin) pin, getRandomAnalog()));
					} else if (pin.is(Type.DIGITAL)) {
						fireStateChanged(new DefaultDigitalPinValueChangedEvent(
								(DigitalPin) pin, getRandomDigital()));
					}
				}
				try {
					TimeUnit.MILLISECONDS.sleep(250);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

	};

	private final Map<Pin, Object> listeningPins = new HashMap<Pin, Object>();

	public VirtualLink(LinkConfig config) {
		super();
	}

	@Override
	public void close() throws IOException {
		super.close();
		this.thread.interrupt();
	}

	@Override
	public void startListening(Pin pin) throws IOException {
		this.listeningPins.put(pin, getRandomValue(pin));
	}

	private Object getRandomValue(Pin pin) {
		if (pin.is(Type.ANALOG)) {
			return getRandomAnalog();
		}
		if (pin.is(Type.DIGITAL)) {
			return getRandomDigital();
		}
		throw new IllegalStateException("Cannot handle pin type "
				+ pin.getType() + " of pin " + pin);
	}

	private Integer getRandomAnalog() {
		return secureRandom.nextInt(1024);
	}

	private Boolean getRandomDigital() {
		return secureRandom.nextBoolean();
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		this.listeningPins.remove(pin);
	}

	@Override
	public void switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		logger.info("{} set to {}", analogPin, value);
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		logger.info("{} set to {}", digitalPin, value);
	}

	@Override
	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		logger.info("key pressed ({} {} {} {} {})", keychar, keycode,
				keylocation, keymodifiers, keymodifiersex);
	}

	@Override
	public void sendTone(Tone tone) throws IOException {
		logger.info("tone {}", tone);
	}

	@Override
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		logger.info("no tone on {}", analogPin);
	}

	@Override
	public void sendCustomMessage(String... messages) throws IOException {
		logger.info("custom message {}", Arrays.asList(messages));
	}

}
