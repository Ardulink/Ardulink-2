package org.ardulink.core.virtual;

import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import org.ardulink.core.proto.api.MessageIdHolders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualLink extends AbstractListenerLink {

	private final Logger logger = LoggerFactory.getLogger(VirtualLink.class);

	private final SecureRandom secureRandom = new SecureRandom();

	private final Thread thread = new Thread() {
		@Override
		public void run() {
			while (true) {
				sendRandomMessagesAndSleep();
			}
		}
	};

	private final Map<Pin, Object> listeningPins = new ConcurrentHashMap<Pin, Object>();

	private VirtualLinkConfig config;

	public VirtualLink(VirtualLinkConfig config) {
		super();
		this.config = config;
		this.thread.setDaemon(true);
		this.thread.start();
	}

	protected void sendRandomMessagesAndSleep() {
		sendRandomPinStates();
		config.delay();
	}

	protected void sendRandomPinStates() {
		for (Entry<Pin, Object> entry : listeningPins.entrySet()) {
			Pin pin = entry.getKey();
			if (pin.is(ANALOG)) {
				fireStateChanged(new DefaultAnalogPinValueChangedEvent(
						(AnalogPin) pin, getRandomAnalog()));
			} else if (pin.is(DIGITAL)) {
				fireStateChanged(new DefaultDigitalPinValueChangedEvent(
						(DigitalPin) pin, getRandomDigital()));
			}
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		this.thread.interrupt();
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
	public long stopListening(Pin pin) throws IOException {
		this.listeningPins.remove(pin);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		logger.info("{} set to {}", analogPin, value);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		logger.info("{} set to {}", digitalPin, value);
		return MessageIdHolders.NO_ID.getId();
	}

	@Override
	public long sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		logger.info("key pressed ({} {} {} {} {})", keychar, keycode,
				keylocation, keymodifiers, keymodifiersex);
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
