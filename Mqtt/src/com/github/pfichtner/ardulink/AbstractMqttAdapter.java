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

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static org.zu.ardulink.util.Integers.tryParse;
import static org.zu.ardulink.util.Preconditions.checkArgument;
import static org.zu.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zu.ardulink.util.ListBuilder;

import com.github.pfichtner.ardulink.Config.DefaultConfig;
import com.github.pfichtner.ardulink.compactors.AnalogReadChangeListenerToleranceAdapter;
import com.github.pfichtner.ardulink.compactors.SlicedAnalogReadChangeListenerAdapter;
import com.github.pfichtner.ardulink.compactors.TimeSliceCompactorAvg;
import com.github.pfichtner.ardulink.compactors.TimeSliceCompactorLast;
import com.github.pfichtner.ardulink.compactors.TimeSlicer;
import com.github.pfichtner.ardulink.compactors.Tolerance;
import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.events.AnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DigitalPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.EventListener;
import com.github.pfichtner.ardulink.core.events.EventListenerAdapter;
import com.github.pfichtner.ardulink.core.events.FilteredEventListenerAdapter;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 *         [adsense]
 */
public abstract class AbstractMqttAdapter {

	private static final Logger logger = LoggerFactory
			.getLogger(AbstractMqttAdapter.class);

	public interface Handler {
		boolean handle(String topic, String message) throws IOException;
	}

	public static abstract class AbstractPinHandler implements Handler {

		private final Pattern pattern;

		public AbstractPinHandler(Pattern pattern) {
			this.pattern = checkNotNull(pattern, "Pattern must not be null");
		}

		@Override
		public boolean handle(String topic, String message) throws IOException {
			Matcher matcher = this.pattern.matcher(topic);
			if (matcher.matches() && matcher.groupCount() > 0) {
				Integer pin = tryParse(matcher.group(1));
				if (pin != null) {
					return handlePin(pin.intValue(), message);
				}
			}
			return false;
		}

		protected abstract boolean handlePin(int pin, String message)
				throws IOException;

	}

	/**
	 * Does handle mqtt messages for digital pins.
	 */
	public static class DigitalHandler extends AbstractPinHandler {

		private final Link link;

		public DigitalHandler(Link link, Config config) {
			super(config.getTopicPatternDigitalWrite());
			this.link = link;
		}

		@Override
		protected boolean handlePin(int pin, String message) throws IOException {
			this.link.switchDigitalPin(digitalPin(pin), parseBoolean(message));
			return true;
		}

	}

	/**
	 * Does handle mqtt messages for analog pins.
	 */
	public static class AnalogHandler extends AbstractPinHandler {

		private final Link link;

		public AnalogHandler(Link link, Config config) {
			super(config.getTopicPatternAnalogWrite());
			this.link = link;
		}

		@Override
		protected boolean handlePin(int pin, String message) throws IOException {
			Integer intensity = tryParse(message);
			if (intensity == null) {
				return false;
			}
			this.link.switchAnalogPin(analogPin(pin), intensity.intValue());
			return true;
		}

	}

	/**
	 * Does handle mqtt messages for controlling Ardulink (start/stop digital
	 * listeners).
	 */
	public static class ControlHandlerDigital extends AbstractPinHandler {

		public static class Builder {

			private final Pattern pattern;

			public Builder(Config config) {
				this.pattern = config.getTopicPatternDigitalControl();
			}

			public boolean patternIsValid() {
				return this.pattern != null;
			}

			public ControlHandlerDigital build(Link link) {
				return new ControlHandlerDigital(link, this.pattern);
			}

		}

		private final Link link;

		public ControlHandlerDigital(Link link, Pattern pattern) {
			super(pattern);
			this.link = link;
		}

		@Override
		protected boolean handlePin(int pin, String message) throws IOException {
			if (parseBoolean(message)) {
				this.link.startListening(digitalPin(pin));
			} else {
				this.link.stopListening(digitalPin(pin));
			}
			return true;
		}

	}

	/**
	 * Does handle mqtt messages for controlling Ardulink (start/stop analog
	 * listeners).
	 */
	public static class ControlHandlerAnalog extends AbstractPinHandler {

		public static class Builder {

			private final Pattern pattern;

			public Builder(Config config) {
				this.pattern = config.getTopicPatternAnalogControl();
			}

			public boolean patternIsValid() {
				return this.pattern != null;
			}

			public ControlHandlerAnalog build(Link link) {
				return new ControlHandlerAnalog(link, this.pattern);
			}

		}

		private final Link link;

		public ControlHandlerAnalog(Link link, Pattern pattern) {
			super(pattern);
			this.link = link;
		}

		@Override
		protected boolean handlePin(int pin, String message) throws IOException {
			if (parseBoolean(message)) {
				this.link.startListening(analogPin(pin));
			} else {
				this.link.stopListening(analogPin(pin));
			}
			return true;
		}

	}

	private final Link link;

	private final Config config;

	private final List<Handler> handlers;

	public AbstractMqttAdapter(Link link, Config config) {
		this(link, config, handlers(link, config));
	}

	private static List<Handler> handlers(Link link, Config config) {
		ListBuilder<Handler> b = ListBuilder.<Handler> newBuilder().addAll(
				new DigitalHandler(link, config),
				new AnalogHandler(link, config));
		ControlHandlerAnalog.Builder ab = new ControlHandlerAnalog.Builder(
				config);
		if (ab.patternIsValid()) {
			b = b.add(ab.build(link));
		}
		ControlHandlerDigital.Builder db = new ControlHandlerDigital.Builder(
				config);
		if (db.patternIsValid()) {
			b = b.add(db.build(link));
		}
		return b.build();
	}

	public AbstractMqttAdapter(Link link, Config config,
			Collection<Handler> handlers) {
		this.link = link;
		// creating a copy so config can't be changed by caller
		this.config = DefaultConfig.copyOf(config);
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
	 * @throws IOException
	 */
	public void toArduino(String topic, String message) throws IOException {
		for (Handler handler : this.handlers) {
			if (handler.handle(topic, message)) {
				logger.info("Message {} {} handled by {}", topic, message,
						handler);
				return;
			}
		}
	}

	public void enableDigitalPinChangeEvents(final int pin) throws IOException {
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
		this.link.addListener(newDigitalReadChangeListener(pin));
	}

	public enum CompactStrategy {
		LAST_WINS, AVERAGE;
	}

	public class AnalogReadChangeListenerConfigurer {

		private EventListener active;

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

		public void decorate(EventListener newActive) {
			this.active = newActive;
		}

		public EventListener getActive() {
			return active;
		}

		public void add() throws IOException {
			link.addListener(active);
		}

	}

	public AnalogReadChangeListenerConfigurer configureAnalogReadChangeListener(
			int pin) {
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
		return new AnalogReadChangeListenerConfigurer(pin);
	}

	public void enableAnalogPinChangeEvents(final int pin) throws IOException {
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
		this.link.addListener(newAnalogReadChangeListener(pin));
	}

	private FilteredEventListenerAdapter newAnalogReadChangeListener(
			final int pin) {
		return new FilteredEventListenerAdapter(analogPin(pin),
				new EventListenerAdapter() {
					@Override
					public void stateChanged(AnalogPinValueChangedEvent event) {
						fromArduino(
								format(AbstractMqttAdapter.this.config
										.getTopicPatternAnalogRead(), event
										.getPin().pinNum()), String
										.valueOf(event.getValue()));
					}

				});

	}

	private FilteredEventListenerAdapter newDigitalReadChangeListener(
			final int pin) {
		return new FilteredEventListenerAdapter(digitalPin(pin),
				new EventListenerAdapter() {
					@Override
					public void stateChanged(DigitalPinValueChangedEvent event) {
						fromArduino(
								format(AbstractMqttAdapter.this.config
										.getTopicPatternDigitalRead(), event
										.getPin().pinNum()), String
										.valueOf(event.getValue()));
					}
				});
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
