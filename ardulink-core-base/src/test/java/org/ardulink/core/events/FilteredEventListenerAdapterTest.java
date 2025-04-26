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

package org.ardulink.core.events;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.ardulink.core.events.FilteredEventListenerAdapter.filter;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class FilteredEventListenerAdapterTest {

	AnalogPin analogPin42 = analogPin(42);
	DigitalPin digitalPin42 = digitalPin(42);
	EventListener listener = mock(EventListener.class);

	AnalogPinValueChangedEvent analogEvent = analogPinValueChanged(analogPin42, 21);
	DigitalPinValueChangedEvent digitalEvent = digitalPinValueChanged(digitalPin42, true);

	@Test
	void callsListenerOnAnalogPinMatch() {
		EventListenerAdapter sut = filter(analogPin42, listener);
		sut.stateChanged(analogEvent);
		verify(listener).stateChanged(analogEvent);
		verifyNoMoreInteractions(listener);
	}

	@Test
	void doesNotCallsListenerOnAnalogPinMismatch() {
		EventListenerAdapter sut = filter(analogPin42, listener);
		sut.stateChanged(withChangedPin(analogEvent));
		verifyNoInteractions(listener);
	}

	@Test
	void callsListenerOnDigitalPinMatch() {
		EventListenerAdapter sut = filter(digitalPin42, listener);
		sut.stateChanged(digitalEvent);
		verify(listener).stateChanged(digitalEvent);
		verifyNoMoreInteractions(listener);
	}

	@Test
	void doesNotCallsListenerOnDigitalPinMismatch() {
		EventListenerAdapter sut = filter(digitalPin42, listener);
		sut.stateChanged(withChangedPin(digitalEvent));
		verifyNoInteractions(listener);
	}

	static AnalogPinValueChangedEvent withChangedPin(AnalogPinValueChangedEvent event) {
		return analogPinValueChanged(not(event.getPin()), event.getValue());
	}

	static DigitalPinValueChangedEvent withChangedPin(DigitalPinValueChangedEvent event) {
		return digitalPinValueChanged(not(event.getPin()), event.getValue());
	}

	static AnalogPin not(AnalogPin pin) {
		return analogPin(pin.pinNum() + 1);
	}

	static DigitalPin not(DigitalPin pin) {
		return digitalPin(pin.pinNum() + 1);
	}

}
