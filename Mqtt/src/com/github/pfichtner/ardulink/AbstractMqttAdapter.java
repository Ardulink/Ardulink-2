package com.github.pfichtner.ardulink;

import static com.github.pfichtner.ardulink.util.Preconditions.checkArgument;
import static java.lang.Boolean.parseBoolean;
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

import com.github.pfichtner.ardulink.compactors.AnalogReadChangeListenerToleranceAdapter;
import com.github.pfichtner.ardulink.compactors.SlicedAnalogReadChangeListenerAdapter;
import com.github.pfichtner.ardulink.compactors.TimeSliceCompactorAvg;
import com.github.pfichtner.ardulink.compactors.TimeSliceCompactorLast;
import com.github.pfichtner.ardulink.compactors.TimeSlicer;
import com.github.pfichtner.ardulink.compactors.Tolerance;

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
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
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

	public enum CompactStrategy {
		LAST_WINS, AVERAGE;
	}

	public class AnalogReadChangeListenerConfigurer {

		private AnalogReadChangeListener active;

		public AnalogReadChangeListenerConfigurer(int pin) {
			active = newAnalogReadChangeListener(pin);
		}

		public AnalogReadChangeListenerConfigurer tolerance(Tolerance tolerance) {
			if (!tolerance.isZero()) {
				decorate(new AnalogReadChangeListenerToleranceAdapter(
						tolerance, active));
			}
			return this;
		}

		public AnalogReadChangeListenerConfigurer compact(
				CompactStrategy strategy, TimeSlicer timeSlicer) {
			SlicedAnalogReadChangeListenerAdapter worker = createWorker(strategy);
			timeSlicer.add(worker);
			decorate(worker);
			return this;
		}

		private SlicedAnalogReadChangeListenerAdapter createWorker(
				CompactStrategy strategy) {
			switch (strategy) {
			case AVERAGE:
				return new TimeSliceCompactorAvg(active);
			case LAST_WINS:
				return new TimeSliceCompactorLast(active);
			default:
				throw new IllegalStateException("Unsupported strategy "
						+ strategy);
			}
		}

		public void decorate(AnalogReadChangeListener newActive) {
			this.active = newActive;
		}

		public AnalogReadChangeListener getActive() {
			return active;
		}

		public void add() {
			link.addAnalogReadChangeListener(active);
		}

	}

	public AnalogReadChangeListenerConfigurer configureAnalogReadChangeListener(
			int pin) {
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
		return new AnalogReadChangeListenerConfigurer(pin);
	}

	public void enableAnalogPinChangeEvents(final int pin) {
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
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
