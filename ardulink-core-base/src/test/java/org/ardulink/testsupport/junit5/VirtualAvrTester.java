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
import static org.testcontainers.shaded.com.google.common.base.Objects.equal;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.io.IOException;

import org.ardulink.core.Link;
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

	public static void testSerialPinSwitching(VirtualAvrContainer<?> virtualAvr, Configurer configurer) throws IOException {
		try (Link link = configurer.newLink()) {
			testPinSwitching(virtualAvr, link);
		}
	}

	public static void testPinSwitching(VirtualAvrContainer<?> virtualAvr, Link link) throws IOException {
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

}
