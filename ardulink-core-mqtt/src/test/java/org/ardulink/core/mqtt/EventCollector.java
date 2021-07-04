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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.util.anno.LapsedWith.JDK8;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ardulink.util.ListMultiMap;
import org.ardulink.util.StopWatch;
import org.ardulink.util.anno.LapsedWith;
import org.hamcrest.Matcher;
import org.ardulink.core.Pin.Type;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.PinValueChangedEvent;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class EventCollector implements EventListener {

	private final ListMultiMap<Type, PinValueChangedEvent> events = new ListMultiMap<Type, PinValueChangedEvent>();

	@Override
	public void stateChanged(AnalogPinValueChangedEvent event) {
		events.put(ANALOG, event);
	}

	@Override
	public void stateChanged(DigitalPinValueChangedEvent event) {
		events.put(DIGITAL, event);
	}

	@LapsedWith(module = JDK8, value = "org.awaitability")
	public void awaitEvents(Type type, Matcher<? super List<PinValueChangedEvent>> matcher) {
		StopWatch stopWatch = new StopWatch().start();
		while (stopWatch.getTime(SECONDS) < 10) {
			List<PinValueChangedEvent> list = events.asMap().get(type);
			try {
				assertThat(list == null ? Collections.<PinValueChangedEvent>emptyList() : list, matcher);
				return;
			} catch (AssertionError e) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

}