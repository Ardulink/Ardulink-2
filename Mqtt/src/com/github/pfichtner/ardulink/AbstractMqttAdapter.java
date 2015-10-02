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
package com.github.pfichtner.ardulink;

import static com.github.pfichtner.ardulink.util.Integers.tryParse;
import static com.github.pfichtner.ardulink.util.Preconditions.checkArgument;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.zu.ardulink.protocol.IProtocol.POWER_HIGH;
import static org.zu.ardulink.protocol.IProtocol.POWER_LOW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public abstract class AbstractMqttAdapter {

	public interface Handler {
		boolean handle(String topic, String message);
	}

	/**
	 * Does handle mqtt messages for digital pins.
	 */
	public static class DigitalHandler implements Handler {

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
					this.link.sendPowerPinSwitch(pin.intValue(),
							parseBoolean(message) ? POWER_HIGH : POWER_LOW);
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * Does handle mqtt messages for analog pins.
	 */
	public static class AnalogHandler implements Handler {

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

	/**
	 * Does handle mqtt messages for controlling Ardulink (start/stop digital
	 * listeners).
	 */
	public static class ControlHandlerDigital implements Handler {

		private final Link link;
		private final Pattern pattern;

		public ControlHandlerDigital(Link link, Config config) {
			this.link = link;
			this.pattern = config.getTopicPatternDigitalControl();
		}

		@Override
		public boolean handle(String topic, String message) {
			Matcher matcher = pattern.matcher(topic);
			if (matcher.matches()) {
				Integer pin = tryParse(matcher.group(1));
				if (pin != null) {
					if (parseBoolean(message)) {
						this.link.startListenDigitalPin(pin);
					} else {
						this.link.stopListenDigitalPin(pin);
					}
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * Does handle mqtt messages for controlling Ardulink (start/stop analog
	 * listeners).
	 */
	public static class ControlHandlerAnalog implements Handler {

		private final Link link;
		private final Pattern pattern;

		public ControlHandlerAnalog(Link link, Config config) {
			this.link = link;
			this.pattern = config.getTopicPatternAnalogControl();
		}

		@Override
		public boolean handle(String topic, String message) {
			Matcher matcher = pattern.matcher(topic);
			if (matcher.matches()) {
				Integer pin = tryParse(matcher.group(1));
				if (pin != null) {
					if (parseBoolean(message)) {
						this.link.startListenAnalogPin(pin);
					} else {
						this.link.stopListenAnalogPin(pin);
					}
					return true;
				}
			}
			return false;
		}

	}

	private final Link link;

	private final Config config;

	private final List<Handler> handlers;

	public AbstractMqttAdapter(Link link, Config config) {
		this(link, config, handlers(link, config));
	}

	private static List<Handler> handlers(Link link, Config config) {
		List<Handler> handlers = new ArrayList<Handler>(asList(
				new DigitalHandler(link, config), new AnalogHandler(link,
						config)));
		if (config.getTopicPatternAnalogControl() != null) {
			handlers.add(new ControlHandlerAnalog(link, config));
		}
		if (config.getTopicPatternDigitalControl() != null) {
			handlers.add(new ControlHandlerDigital(link, config));
		}
		return handlers;
	}

	public AbstractMqttAdapter(Link link, Config config,
			Collection<Handler> handlers) {
		this.link = link;
		this.config = config;
		this.handlers = unmodifiableList(new ArrayList<Handler>(handlers));
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
