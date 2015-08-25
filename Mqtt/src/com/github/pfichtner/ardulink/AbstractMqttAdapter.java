package com.github.pfichtner.ardulink;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static org.zu.ardulink.protocol.IProtocol.POWER_HIGH;
import static org.zu.ardulink.protocol.IProtocol.POWER_LOW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zu.ardulink.Link;
import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;
import org.zu.ardulink.event.DigitalReadChangeEvent;
import org.zu.ardulink.event.DigitalReadChangeListener;

public abstract class AbstractMqttAdapter {

	public interface Handler {
		boolean handle(String topic, String message);
	}

	/**
	 * Does handle mqtt messages for digital pins.
	 */
	private static class DigitalHandler implements Handler {

		private final Link link;
		private final Pattern pattern;

		public DigitalHandler(Link link, Config config) {
			this.link = link;
			this.pattern = config.getTopicPatternDigitalWrite();
		}

		@Override
		public boolean handle(String topic, String message) {
			Matcher matcher = pattern.matcher(topic);
			if (matcher.matches()) {
				Integer pin = tryParse(matcher.group(1));
				if (pin != null) {
					boolean state = parseBoolean(message);
					this.link.sendPowerPinSwitch(pin.intValue(),
							state ? POWER_HIGH : POWER_LOW);
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * Does handle mqtt messages for analog pins.
	 */
	private static class AnalogHandler implements Handler {

		private final Link link;
		private final Pattern pattern;

		public AnalogHandler(Link link, Config config) {
			this.link = link;
			this.pattern = config.getTopicPatternAnalogWrite();
		}

		@Override
		public boolean handle(String topic, String message) {
			Matcher matcher = pattern.matcher(topic);
			if (matcher.matches()) {
				Integer pin = tryParse(matcher.group(1));
				Integer intensity = tryParse(message);
				if (pin != null && intensity != null) {
					this.link.sendPowerPinIntensity(pin.intValue(),
							intensity.intValue());
					return true;
				}
			}
			return false;
		}

	}

	private final Link link;

	private final Config config;

	private final Handler[] handlers;

	public AbstractMqttAdapter(Link link, Config config) {
		this(link, config, new Handler[] { new DigitalHandler(link, config),
				new AnalogHandler(link, config) });
	}

	public AbstractMqttAdapter(Link link, Config config, Handler[] handlers) {
		this.link = link;
		this.config = config;
		this.handlers = handlers.clone();
	}

	/**
	 * This method should be called by the publisher when a new mqtt message has
	 * arrived.
	 * 
	 * @param topic
	 *            the message's topic
	 * @param message
	 *            the payload
	 */
	public void toArduino(String topic, String message) {
		for (Handler handler : this.handlers) {
			if (handler.handle(topic, message)) {
				return;
			}
		}
	}

	private static Integer tryParse(String string) {
		try {
			return Integer.valueOf(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public void enableDigitalPinChangeEvents(final int pin) {
		this.link.addDigitalReadChangeListener(new DigitalReadChangeListener() {
			@Override
			public void stateChanged(DigitalReadChangeEvent e) {
				fromArduino(
						format(AbstractMqttAdapter.this.config
								.getTopicPatternDigitalRead(), e.getPin()),
						String.valueOf(e.getValue()));
			}

			@Override
			public int getPinListening() {
				return pin;
			}
		});
	}

	public void enableAnalogPinChangeEvents(final int pin, final int tolerance) {
		AnalogReadChangeListener delegate = newAnalogReadChangeListener(pin);
		this.link.addAnalogReadChangeListener(tolerance == 0 ? delegate
				: toleranceAdapter(tolerance, delegate));
	}

	private static AnalogReadChangeListener toleranceAdapter(
			final int tolerance, final AnalogReadChangeListener delegate) {
		return new AnalogReadChangeListener() {

			private Integer cachedValue;

			@Override
			public void stateChanged(AnalogReadChangeEvent e) {
				int newValue = e.getValue();
				if (this.cachedValue == null
						|| abs(this.cachedValue.intValue() - newValue) > tolerance
						|| isHighOrLowValue(newValue)) {
					this.cachedValue = Integer.valueOf(newValue);
					delegate.stateChanged(e);
				}
			}

			private boolean isHighOrLowValue(int value) {
				return value == 0 || value == 255;
			}

			@Override
			public int getPinListening() {
				return delegate.getPinListening();
			}
		};
	}

	public void enableAnalogPinChangeEvents(final int pin) {
		this.link.addAnalogReadChangeListener(newAnalogReadChangeListener(pin));
	}

	private AnalogReadChangeListener newAnalogReadChangeListener(final int pin) {
		return new AnalogReadChangeListener() {
			@Override
			public void stateChanged(AnalogReadChangeEvent e) {
				fromArduino(
						format(AbstractMqttAdapter.this.config
								.getTopicPatternAnalogRead(),
								e.getPin()), String.valueOf(e.getValue()));
			}

			@Override
			public int getPinListening() {
				return pin;
			}
		};
	}

	/**
	 * Called when a message from arduino (ardulink) is received and should be
	 * published to the mqtt broker.
	 * 
	 * @param topic
	 *            the message's topic
	 * @param message
	 *            the payload
	 */
	abstract void fromArduino(String topic, String message);

}
