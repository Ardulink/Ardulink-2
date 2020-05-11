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

package org.ardulink.testsupport.mock;

import java.io.IOException;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.PinValueChangedEvent;
import org.mockito.internal.util.MockUtil;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class TestSupport {

	private TestSupport() {
		super();
	}

	public static Link getMock(Link link) {
		return isMock(link) || link == null ? link : getMock(extractDelegated(link));
	}

	private static boolean isMock(Link link) {
		return new MockUtil().isMock(link);
	}

	public static Link extractDelegated(Link link) {
		return ((LinkDelegate) link).getDelegate();
	}

	public static AbstractListenerLink createAbstractListenerLink(final PinValueChangedEvent... fireEvents) {
		return new AbstractListenerLink() {

			@Override
			public Link addListener(EventListener listener) throws IOException {
				Link link = super.addListener(listener);
				for (PinValueChangedEvent event : fireEvents) {
					if (event instanceof AnalogPinValueChangedEvent) {
						fireStateChanged((AnalogPinValueChangedEvent) event);
					} else if (event instanceof DigitalPinValueChangedEvent) {
						fireStateChanged((DigitalPinValueChangedEvent) event);
					}
				}
				return link;
			}

			@Override
			public long switchDigitalPin(DigitalPin digitalPin, boolean value) throws IOException {
				return 0;
			}

			@Override
			public long switchAnalogPin(AnalogPin analogPin, int value) throws IOException {
				return 0;
			}

			@Override
			public long stopListening(Pin pin) throws IOException {
				return 0;
			}

			@Override
			public long startListening(Pin pin) throws IOException {
				return 0;
			}

			@Override
			public long sendTone(Tone tone) throws IOException {
				return 0;
			}

			@Override
			public long sendNoTone(AnalogPin analogPin) throws IOException {
				return 0;
			}

			@Override
			public long sendKeyPressEvent(char keychar, int keycode, int keylocation, int keymodifiers,
					int keymodifiersex) throws IOException {
				return 0;
			}

			@Override
			public long sendCustomMessage(String... messages) throws IOException {
				return 0;
			}
		};
	}

}
