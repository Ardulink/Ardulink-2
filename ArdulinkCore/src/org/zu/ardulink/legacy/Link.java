package org.zu.ardulink.legacy;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.github.pfichtner.ardulink.core.Tone;
import com.github.pfichtner.ardulink.core.convenience.Links;
import com.github.pfichtner.ardulink.core.events.EventListener;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

/**
 * An adapter for the old legacy link. Users should migrate to new new API.
 * 
 * @author Peter Fichtner
 * @deprecated
 */
public abstract class Link {

	public static class LegacyLinkAdapter extends Link {

		private final com.github.pfichtner.ardulink.core.Link delegate;
		private Configurer configurer;

		public LegacyLinkAdapter(
				com.github.pfichtner.ardulink.core.Link delegate) {
			this.delegate = delegate;
		}

		public LegacyLinkAdapter(Configurer configurer) throws Exception {
			this(Links.getLink(configurer));
			this.configurer = configurer;
		}

		@Override
		public com.github.pfichtner.ardulink.core.Link getDelegate() {
			return delegate;
		}

		@Override
		public Object[] getChoiceValues(String key) {
			return configurer == null ? new Object[0] : configurer
					.getAttribute(key).getChoiceValues();
		}

		@Override
		public void addAnalogReadChangeListener(EventListener listener) {
			addListener(listener);
		}

		@Override
		public void addDigitalReadChangeListener(EventListener listener) {
			addListener(listener);
		}

		private void addListener(EventListener listener) {
			try {
				delegate.addListener(listener);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void removeAnalogReadChangeListener(EventListener listener) {
			removeListener(listener);
		}

		@Override
		public void removeDigitalReadChangeListener(EventListener listener) {
			removeListener(listener);
		}

		private void removeListener(EventListener listener) {
			try {
				this.delegate.removeListener(listener);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void sendKeyPressEvent(char keyChar, int keyCode,
				int keyLocation, int modifiers, int modifiersEx) {
			try {
				this.delegate.sendKeyPressEvent(keyChar, keyCode, keyLocation,
						modifiers, modifiersEx);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void sendPowerPinIntensity(int pin, int powerValue) {
			try {
				this.delegate.switchAnalogPin(analogPin(pin), powerValue);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void sendPowerPinSwitch(int pin, boolean state) {
			try {
				this.delegate.switchDigitalPin(digitalPin(pin), state);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void sendToneMessage(int pin, Integer frequency) {
			try {
				this.delegate.sendTone(Tone.forPin(analogPin(pin))
						.withHertz(frequency).endless());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void sendToneMessage(int pin, Integer frequency, Integer duration) {
			try {
				this.delegate.sendTone(Tone.forPin(analogPin(pin))
						.withHertz(frequency)
						.withDuration(duration, MILLISECONDS));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void sendNoToneMessage(int pin) {
			try {
				this.delegate.sendNoTone(analogPin(pin));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void sendCustomMessage(String... messages) {
			try {
				this.delegate.sendCustomMessage(messages);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean disconnect() {
			try {
				delegate.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

	}

	public static final Link NO_LINK = new Link() {

		@Override
		public com.github.pfichtner.ardulink.core.Link getDelegate() {
			return null;
		}

		@Override
		public Object[] getChoiceValues(String attribute) {
			return null;
		}

		@Override
		public void sendCustomMessage(String... messages) {
			// do nothing
		}

		@Override
		public void addAnalogReadChangeListener(EventListener listener) {
			// do nothing
		}

		@Override
		public void removeAnalogReadChangeListener(EventListener listener) {
			// do nothing
		}

		@Override
		public void addDigitalReadChangeListener(EventListener listener) {
			// do nothing
		}

		@Override
		public void removeDigitalReadChangeListener(EventListener listener) {
			// do nothing
		}

		@Override
		public void sendKeyPressEvent(char keyChar, int keyCode,
				int keyLocation, int modifiers, int modifiersEx) {
			// do nothing
		}

		@Override
		public void sendPowerPinIntensity(int pin, int powerValue) {
			// do nothing
		}

		@Override
		public void sendPowerPinSwitch(int pin, boolean b) {
			// do nothing
		}

		@Override
		public void sendToneMessage(int pin, Integer frequency) {
			// do nothing
		}

		@Override
		public void sendToneMessage(int pin, Integer frequency, Integer duration) {
			// do nothing
		}

		@Override
		public void sendNoToneMessage(int pin) {
			// do nothing
		}

		@Override
		public boolean disconnect() {
			return false;
		}
	};

	private static Link defaultInstance;

	public static Link getDefaultInstance() {
		synchronized (Link.class) {
			if (defaultInstance == null) {
				defaultInstance = new LegacyLinkAdapter(Links.getDefault());
			}
		}
		return defaultInstance;
	}

	public static Link createInstance(String type) {
		try {
			URI uri = new URI("ardulink://" + type);
			Configurer configurer = LinkManager.getInstance()
					.getConfigurer(uri);
			return new LegacyLinkAdapter(configurer);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public abstract com.github.pfichtner.ardulink.core.Link getDelegate();

	public abstract Object[] getChoiceValues(String attribute);

	public abstract void sendCustomMessage(String... messages);

	public abstract void addAnalogReadChangeListener(EventListener listener);

	public abstract void removeAnalogReadChangeListener(EventListener listener);

	public abstract void addDigitalReadChangeListener(EventListener listener);

	public abstract void removeDigitalReadChangeListener(EventListener listener);

	public abstract void sendKeyPressEvent(char keyChar, int keyCode,
			int keyLocation, int modifiers, int modifiersEx);

	public abstract void sendPowerPinIntensity(int pin, int powerValue);

	public abstract void sendPowerPinSwitch(int pin, boolean b);

	public abstract void sendToneMessage(int pin, Integer frequency);

	public abstract void sendToneMessage(int pin, Integer frequency,
			Integer duration);

	public abstract void sendNoToneMessage(int pin);

	public abstract boolean disconnect();

}
