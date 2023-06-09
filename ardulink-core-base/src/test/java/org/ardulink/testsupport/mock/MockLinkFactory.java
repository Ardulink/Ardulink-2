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

import static org.mockito.Mockito.spy;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.testsupport.mock.MockLinkFactory.MockLinkConfig;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MockLinkFactory implements LinkFactory<MockLinkConfig> {

	private static class DummyAbstractListenerLink extends AbstractListenerLink {

		@Override
		public long switchDigitalPin(DigitalPin digitalPin, boolean value) {
			return 0;
		}

		@Override
		public long switchAnalogPin(AnalogPin analogPin, int value) {
			return 0;
		}

		@Override
		public long stopListening(Pin pin) {
			return 0;
		}

		@Override
		public long startListening(Pin pin) {
			return 0;
		}

		@Override
		public long sendTone(Tone tone) {
			return 0;
		}

		@Override
		public long sendNoTone(AnalogPin analogPin) {
			return 0;
		}

		@Override
		public long sendKeyPressEvent(char keychar, int keycode, int keylocation, int keymodifiers,
				int keymodifiersex) {
			return 0;
		}

		@Override
		public long sendCustomMessage(String... messages) {
			return 0;
		}
	}

	public static class MockLinkConfig implements LinkConfig {

		public static final String NAME_ATTRIBUTE = "name";

		@Named(NAME_ATTRIBUTE)
		public String name = "default";
	}

	public static final String NAME = "mock";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Link newLink(MockLinkConfig config) {
		return spy(new DummyAbstractListenerLink());
	}

	@Override
	public MockLinkConfig newLinkConfig() {
		return new MockLinkConfig();
	}

}
