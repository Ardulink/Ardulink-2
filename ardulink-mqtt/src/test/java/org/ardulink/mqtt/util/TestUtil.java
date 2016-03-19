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
package org.ardulink.mqtt.util;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ardulink.util.StopWatch;

import org.ardulink.mqtt.MqttMain;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class TestUtil {

	private TestUtil() {
		super();
	}

	public static MqttMain startAsync(MqttMain mqttMain) throws Exception {
		mqttMain.connectToMqttBroker();
		return waitUntilIsConnected(mqttMain, 5, SECONDS);
	}

	public static MqttMain waitUntilIsConnected(MqttMain mqttMain, int value,
			TimeUnit timeUnit) throws InterruptedException {
		StopWatch stopWatch = new StopWatch().start();
		while (!mqttMain.isConnected()) {
			timeUnit.sleep(value);
			if (stopWatch.getTime(timeUnit) > value) {
				throw new IllegalStateException("Could not connect within "
						+ value + " " + timeUnit);
			}
		}
		return mqttMain;
	}

	public static <T> List<T> listWithSameOrder(T... t) {
		return Arrays.asList(t);
	}

	public static AnalogPinValueChangedEvent analogPinChanged(final int pin,
			final int value) {
		return new DefaultAnalogPinValueChangedEvent(analogPin(pin),
				Integer.valueOf(value));
	}

	public static DigitalPinValueChangedEvent digitalPinChanged(final int pin,
			final boolean value) {
		return new DefaultDigitalPinValueChangedEvent(digitalPin(pin),
				Boolean.valueOf(value));
	}

}
