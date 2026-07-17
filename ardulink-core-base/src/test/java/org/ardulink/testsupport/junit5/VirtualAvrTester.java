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
package org.ardulink.testsupport.junit5;

import static com.github.pfichtner.testcontainers.virtualavr.VirtualAvrConnection.PinReportMode.ANALOG;
import static com.github.pfichtner.testcontainers.virtualavr.VirtualAvrConnection.PinReportMode.DIGITAL;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.FilteredEventListenerAdapter.filter;
import static org.testcontainers.shaded.com.google.common.base.Objects.equal;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardulink.core.Link;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListenerAdapter;
import org.ardulink.core.linkmanager.LinkManager.Configurer;

import com.github.pfichtner.testcontainers.virtualavr.VirtualAvrConnection;
import com.github.pfichtner.testcontainers.virtualavr.VirtualAvrContainer;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public final class VirtualAvrTester {

	private VirtualAvrTester() {
		super();
	}

	public static void testSerialPinSwitching(VirtualAvrContainer<?> virtualAvr, Configurer configurer)
			throws IOException {
		try (Link link = configurer.newLink()) {
			testSerialPinSwitching(virtualAvr, link);
		}
	}

	public static void testSerialPinSwitching(VirtualAvrContainer<?> virtualAvr, Link link) throws IOException {
		VirtualAvrConnection avr = virtualAvr.avr();
		switchAnalog(avr, link);
		switchDigital(avr, link);
	}

	private static void switchAnalog(VirtualAvrConnection avr, Link link) throws IOException {
		int analogPin = 10;
		int analogValue = 42;
		avr.pinReportMode(String.valueOf(analogPin), ANALOG);
		link.switchAnalogPin(analogPin(analogPin), analogValue);
		await().until(() -> equal(avr.lastStates().get(String.valueOf(analogPin)), analogValue));
	}

	private static void switchDigital(VirtualAvrConnection avr, Link link) throws IOException {
		int digitalPin = 12;
		boolean digitalValue = true;
		avr.pinReportMode(String.valueOf(digitalPin), DIGITAL);
		link.switchDigitalPin(digitalPin(digitalPin), digitalValue);
		await().until(() -> equal(avr.lastStates().get(String.valueOf(digitalPin)), digitalValue));
	}

	// ---------------------------------------------------------------------------------------------------

	public static void testSerialPinListening(VirtualAvrContainer<?> virtualAvr, Configurer configurer)
			throws IOException {
		try (Link link = configurer.newLink()) {
			testSerialPinListening(virtualAvr, link);
		}
	}

	public static void testSerialPinListening(VirtualAvrContainer<?> virtualAvr, Link link) throws IOException {
		VirtualAvrConnection avr = virtualAvr.avr();
		listenAnalog(avr, link);
		listenDigital(avr, link);
	}

	private static void listenAnalog(VirtualAvrConnection avr, Link link) throws IOException {
		int ardulinkPin = 2;
		int analogValue = 42;
		AtomicInteger received = new AtomicInteger();
		EventListenerAdapter listener = filter(analogPin(ardulinkPin), new EventListenerAdapter() {
			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				received.set(event.getValue());
			}
		});
		link.addListener(listener);
		try {
			String avrPin = "A" + ardulinkPin;
			avr.pinReportMode(avrPin, ANALOG);
			avr.pinState(avrPin, analogValue);
			await().until(() -> received.get() == analogValue);
		} finally {
			link.removeListener(listener);
		}
	}

	private static void listenDigital(VirtualAvrConnection avr, Link link) throws IOException {
		String avrPin = "2";
		int ardulinkPin = 2;
		boolean digitalValue = true;
		AtomicBoolean received = new AtomicBoolean();
		EventListenerAdapter listener = filter(digitalPin(ardulinkPin), new EventListenerAdapter() {
			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				received.set(event.getValue());
			}
		});
		link.addListener(listener);
		try {
			avr.pinReportMode(avrPin, DIGITAL);
			avr.pinState(avrPin, digitalValue);
			await().until(() -> received.get() == digitalValue);
		} finally {
			link.removeListener(listener);
		}
	}

}
