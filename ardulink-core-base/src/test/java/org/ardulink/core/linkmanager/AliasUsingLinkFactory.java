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

package org.ardulink.core.linkmanager;

import static org.ardulink.core.linkmanager.LinkConfig.NO_ATTRIBUTES;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Tone;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.events.CustomListener;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.RplyListener;
import org.ardulink.core.linkmanager.LinkFactory.Alias;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Alias({ "aliasLinkAlias", AliasUsingLinkFactory.ALREADY_TAKEN_NAME })
public class AliasUsingLinkFactory implements LinkFactory<LinkConfig> {

	public static final class LinkImplementation implements Link {

		@Override
		public void close() throws IOException {
			throw new RuntimeException();
		}

		@Override
		public long switchDigitalPin(DigitalPin digitalPin, boolean value) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public long switchAnalogPin(AnalogPin analogPin, int value) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public long stopListening(Pin pin) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public long startListening(Pin pin) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public long sendTone(Tone tone) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public long sendNoTone(AnalogPin analogPin) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public long sendKeyPressEvent(char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex)
				throws IOException {
			throw new RuntimeException();
		}

		@Override
		public long sendCustomMessage(String... messages) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public Link removeRplyListener(RplyListener listener) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public Link removeListener(EventListener listener) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public Link removeCustomListener(CustomListener listener) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public Link addRplyListener(RplyListener listener) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public Link addListener(EventListener listener) throws IOException {
			throw new RuntimeException();
		}

		@Override
		public Link addCustomListener(CustomListener listener) throws IOException {
			throw new RuntimeException();
		}
	}

	protected static final String ALREADY_TAKEN_NAME = DummyLinkFactory.DUMMY_LINK_NAME;

	@Override
	public String getName() {
		return "aliasLink";
	}

	@Override
	public Link newLink(LinkConfig config) {
		return new LinkImplementation();
	}

	@Override
	public LinkConfig newLinkConfig() {
		return NO_ATTRIBUTES;
	}

}
