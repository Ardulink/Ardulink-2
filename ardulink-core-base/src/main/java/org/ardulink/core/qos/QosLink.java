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

package org.ardulink.core.qos;

import static org.ardulink.core.qos.ResponseAwaiter.onLink;
import static org.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.events.RplyListener;

/**
 * [ardulinktitle] [ardulinkversion] Arduino sends ok/ko messages directly after
 * receiving the work message. So there is no need for a queue because if the
 * next message read is not the ok/ko response it never will arrive.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class QosLink implements Link {

	private final Link delegate;
	private final long timeout;
	private final TimeUnit timeUnit;

	public QosLink(Link link) throws IOException {
		this(link, -1, null);
	}

	public QosLink(Link link, long timeout, TimeUnit timeUnit)
			throws IOException {
		this.delegate = link;
		this.timeout = timeout;
		this.timeUnit = timeUnit;
	}

	public void close() throws IOException {
		delegate.close();
	}

	public Link addListener(EventListener listener) throws IOException {
		return delegate.addListener(listener);
	}

	public Link removeListener(EventListener listener) throws IOException {
		return delegate.removeListener(listener);
	}

	public Link addRplyListener(RplyListener listener) throws IOException {
		return delegate.addRplyListener(listener);
	}

	public Link removeRplyListener(RplyListener listener) throws IOException {
		return delegate.removeRplyListener(listener);
	}

	public long startListening(Pin pin) throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.startListening(pin)));
	}

	public long stopListening(Pin pin) throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.stopListening(pin)));
	}

	public long switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.switchAnalogPin(analogPin, value)));
	}

	public long switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.switchDigitalPin(digitalPin, value)));
	}

	public long sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.sendKeyPressEvent(keychar, keycode, keylocation,
						keymodifiers, keymodifiersex)));
	}

	public long sendTone(Tone tone) throws IOException {
		return extractId(newAwaiter().waitForResponse(delegate.sendTone(tone)));
	}

	public long sendNoTone(AnalogPin analogPin) throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.sendNoTone(analogPin)));
	}

	public long sendCustomMessage(String... messages) throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.sendCustomMessage(messages)));
	}

	private ResponseAwaiter newAwaiter() throws IOException {
		ResponseAwaiter awaiter = onLink(delegate);
		return timeout > 0 && timeUnit != null ? awaiter.withTimeout(timeout,
				timeUnit) : awaiter;
	}

	private long extractId(RplyEvent rplyEvent) {
		checkState(rplyEvent.isOk(), "Response status is not ok");
		return rplyEvent.getId();
	}

}
