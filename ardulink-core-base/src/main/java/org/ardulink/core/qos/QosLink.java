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
import org.ardulink.core.events.CustomListener;
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

	// TODO Introduce an ResponseAwaiter which does not deregister the
	// ReplyListener all the time

	private static final long NO_TIMEOUT = 0;
	private static final TimeUnit NO_TIMEOUT_UNIT = null;

	private final Link delegate;
	private final long timeout;
	private final TimeUnit timeUnit;

	public QosLink(Link link) throws IOException {
		this(link, NO_TIMEOUT, NO_TIMEOUT_UNIT);
	}

	public QosLink(Link link, long timeout, TimeUnit timeUnit)
			throws IOException {
		this.delegate = link;
		this.timeout = timeout;
		this.timeUnit = timeUnit;
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public Link addListener(EventListener listener) throws IOException {
		return delegate.addListener(listener);
	}

	@Override
	public Link removeListener(EventListener listener) throws IOException {
		return delegate.removeListener(listener);
	}

	@Override
	public Link addRplyListener(RplyListener listener) throws IOException {
		return delegate.addRplyListener(listener);
	}

	@Override
	public Link removeRplyListener(RplyListener listener) throws IOException {
		return delegate.removeRplyListener(listener);
	}

	@Override
	public Link addCustomListener(CustomListener listener) throws IOException {
		return delegate.addCustomListener(listener);
	}

	@Override
	public Link removeCustomListener(CustomListener listener) throws IOException {
		return delegate.removeCustomListener(listener);
	}

	@Override
	public long startListening(Pin pin) throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.startListening(pin)));
	}

	@Override
	public long stopListening(Pin pin) throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.stopListening(pin)));
	}

	@Override
	public long switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.switchAnalogPin(analogPin, value)));
	}

	@Override
	public long switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.switchDigitalPin(digitalPin, value)));
	}

	@Override
	public long sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.sendKeyPressEvent(keychar, keycode, keylocation,
						keymodifiers, keymodifiersex)));
	}

	@Override
	public long sendTone(Tone tone) throws IOException {
		return extractId(newAwaiter().waitForResponse(delegate.sendTone(tone)));
	}

	@Override
	public long sendNoTone(AnalogPin analogPin) throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.sendNoTone(analogPin)));
	}

	@Override
	public long sendCustomMessage(String... messages) throws IOException {
		return extractId(newAwaiter().waitForResponse(
				delegate.sendCustomMessage(messages)));
	}

	private ResponseAwaiter newAwaiter() throws IOException {
		ResponseAwaiter awaiter = onLink(delegate);
		return timeout == NO_TIMEOUT || timeUnit == NO_TIMEOUT_UNIT ? awaiter : awaiter
				.withTimeout(timeout, timeUnit);
	}

	private long extractId(RplyEvent rplyEvent) {
		checkState(rplyEvent.isOk(), "Response status is not ok");
		return rplyEvent.getId();
	}
	
	@Deprecated // quick fix for https://github.com/Ardulink/Ardulink-2/issues/43
	public Link getDelegate() {
		return delegate;
	}

}
