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

package org.ardulink.core.mqtt;

import static java.time.Duration.ofMillis;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.ardulink.core.Pin.Type;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.PinValueChangedEvent;
import org.ardulink.util.ListMultiMap;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class EventCollector implements EventListener {

	private final ListMultiMap<Type, PinValueChangedEvent> events = new ListMultiMap<>();

	private int timeout = 10;
	private TimeUnit timeUnit = SECONDS;

	public EventCollector withTimeout(int timeout, TimeUnit timeUnit) {
		this.timeout = timeout;
		this.timeUnit = timeUnit;
		return this;
	}

	@Override
	public void stateChanged(AnalogPinValueChangedEvent event) {
		events.put(ANALOG, event);
	}

	@Override
	public void stateChanged(DigitalPinValueChangedEvent event) {
		events.put(DIGITAL, event);
	}

	public void awaitEvents(Type type, Predicate<? super List<? extends PinValueChangedEvent>> predicate) {
		await().pollInterval(ofMillis(100)).timeout(timeout, timeUnit)
				.untilAsserted(() -> assertThat(events.asMap().getOrDefault(type, emptyList())).matches(predicate));
	}

}