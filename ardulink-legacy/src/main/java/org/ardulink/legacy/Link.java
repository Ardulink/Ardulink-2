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

package org.ardulink.legacy;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.ardulink.core.ConnectionListener;
import org.ardulink.core.Tone;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.Configurer;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * An adapter for the old legacy link (Ardulink 1 compatibility issue).
 * Users should migrate to new new API.
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Deprecated
public abstract class Link {

	public static class LegacyLinkAdapter extends Link {

		private final org.ardulink.core.Link delegate;
		private Configurer configurer;

		public LegacyLinkAdapter(
				org.ardulink.core.Link delegate) {
			this.delegate = delegate;
		}

		@Override
		public void addConnectionListener(ConnectionListener connectionListener) {
			delegate.addConnectionListener(connectionListener);
		}

		@Override
		public void removeConnectionListener(ConnectionListener connectionListener) {
			delegate.removeConnectionListener(connectionListener);
		}

		public LegacyLinkAdapter(Configurer configurer) throws Exception {
			this(Links.getLink(configurer));
			this.configurer = configurer;
		}

		@Override
		public org.ardulink.core.Link getDelegate() {
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
		public org.ardulink.core.Link getDelegate() {
			return null;
		}

		@Override
		public Object[] getChoiceValues(String attribute) {
			return new Object[0];
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

		@Override
		public void addConnectionListener(ConnectionListener connectionListener) {
			// do nothing
		}

		@Override
		public void removeConnectionListener(ConnectionListener connectionListener) {
			// do nothing
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

	public abstract org.ardulink.core.Link getDelegate();

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

	public abstract void addConnectionListener(
			ConnectionListener connectionListener);

	public abstract void removeConnectionListener(
			ConnectionListener connectionListener);


}
