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

package org.ardulink.core;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_SWITCH;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ardulink.core.Connection.Listener;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.EventListenerAdapter;
import org.ardulink.core.events.FilteredEventListenerAdapter;
import org.ardulink.core.events.PinValueChangedEvent;
import org.ardulink.util.Joiner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Timeout(value = 5, unit = SECONDS)
class ConnectionBasedLinkTest {

	private final class StringBuilderListenerAdapter implements Listener {

		private final StringBuilder received = new StringBuilder();
		private final StringBuilder sent = new StringBuilder();

		@Override
		public void received(byte[] bytes) throws IOException {
			received.append(new String(bytes));
		}

		@Override
		public void sent(byte[] bytes) throws IOException {
			sent.append(new String(bytes));
		}
	}

	private static final class DigitalPinValueCollector extends EventListenerAdapter {

		private final List<PinValueChangedEvent> digitalEvents = new ArrayList<>();

		@Override
		public void stateChanged(DigitalPinValueChangedEvent event) {
			digitalEvents.add(event);
		}
	}

	@RegisterExtension
	IOStreamWatchExtension streamEx = new IOStreamWatchExtension();

	@Test
	void canSendAnalogValue() throws IOException {
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		streamEx.link().switchAnalogPin(analogPin(pin), value);
		assertToArduinoWasSent(alpProtocolMessage(POWER_PIN_INTENSITY).forPin(pin).withValue(value));
	}

	@Test
	void canSendDigitalValue() throws IOException {
		int pin = anyPositive(int.class);
		streamEx.link().switchDigitalPin(digitalPin(pin), true);
		assertToArduinoWasSent(alpProtocolMessage(POWER_PIN_SWITCH).forPin(pin).withState(true));
	}

	@Test
	void doesSendStartListeningAnalogCommangToArduino() throws IOException {
		int pin = anyPositive(int.class);
		streamEx.link().addListener(new FilteredEventListenerAdapter(analogPin(pin), null));
		assertToArduinoWasSent(alpProtocolMessage(START_LISTENING_ANALOG).forPin(pin).withoutValue());
	}

	@Test
	void doesSendStopListeningAnalogCommangToArduino() throws IOException {
		int pin = anyPositive(int.class);
		FilteredEventListenerAdapter l1 = new FilteredEventListenerAdapter(analogPin(pin), null);
		FilteredEventListenerAdapter l2 = new FilteredEventListenerAdapter(analogPin(pin), null);
		streamEx.link().addListener(l1);
		streamEx.link().addListener(l2);
		String m1 = alpProtocolMessage(START_LISTENING_ANALOG).forPin(pin).withoutValue();
		assertToArduinoWasSent(m1, m1);
		streamEx.link().removeListener(l1);
		streamEx.link().removeListener(l2);
		String m2 = alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(pin).withoutValue();
		assertToArduinoWasSent(m1, m1, m2);
	}

	@Test
	void canReceiveAnalogPinChange() throws IOException {
		List<PinValueChangedEvent> analogEvents = new ArrayList<>();
		EventListenerAdapter listener = new EventListenerAdapter() {
			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				analogEvents.add(event);
			}
		};
		streamEx.link().addListener(listener);
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		String message = alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value);
		streamEx.simulateArduinoSend(message);
		assertThat(analogEvents).contains(analogPinValueChanged(analogPin(pin), value));
	}

	@Test
	void doesSendStartListeningDigitalCommangToArduino() throws IOException {
		int pin = anyPositive(int.class);
		streamEx.link().addListener(new FilteredEventListenerAdapter(digitalPin(pin), null));
		assertToArduinoWasSent(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(pin).withoutValue());
	}

	@Test
	void canReceiveDigitalPinChange() throws IOException {
		DigitalPinValueCollector listener = new DigitalPinValueCollector();
		streamEx.link().addListener(listener);
		int pin = anyPositive(int.class);
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(true);
		streamEx.simulateArduinoSend(message);
		assertThat(listener.digitalEvents).contains(digitalPinValueChanged(digitalPin(pin), true));
	}

	@Test
	void canFilterPins() throws IOException {
		int pin = anyPositive(int.class);
		DigitalPinValueCollector listener = new DigitalPinValueCollector();
		streamEx.link().addListener(new FilteredEventListenerAdapter(digitalPin(anyOtherPin(pin)), listener));
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(true);
		streamEx.simulateArduinoSend(message);
		assertThat(listener.digitalEvents).isEmpty();
	}

	@Test
	void canSendKbdEvents() throws IOException {
		streamEx.link().sendKeyPressEvent('#', 1, 2, 3, 4);
		assertToArduinoWasSent("alp://kprs/chr#cod1loc2mod3mex4");
	}

	@Test
	void canSendToneWithDuration() throws IOException {
		streamEx.link().sendTone(Tone.forPin(analogPin(2)).withHertz(3000).withDuration(5, SECONDS));
		assertToArduinoWasSent("alp://tone/2/3000/5000");
	}

	@Test
	void canSendEndlessTone() throws IOException {
		streamEx.link().sendTone(Tone.forPin(analogPin(2)).withHertz(3000).endless());
		assertToArduinoWasSent("alp://tone/2/3000/-1");
	}

	@Test
	void canSendNoTone() throws IOException {
		streamEx.link().sendNoTone(analogPin(5));
		assertToArduinoWasSent("alp://notn/5");
	}

	@Test
	void canSendCustomMessageSingleValue() throws IOException {
		String message = "myMessage";
		streamEx.link().sendCustomMessage(message);
		assertToArduinoWasSent("alp://cust/" + message);
	}

	@Test
	void canSendCustomMessageMultiValue() throws IOException {
		streamEx.link().sendCustomMessage("1", "2", "3");
		assertToArduinoWasSent("alp://cust/1/2/3");
	}

	@Test
	void canReadRawMessagesRead() throws Exception {
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(anyPositive(int.class)).withState(true);
		assertThat(streamEx.withListener(new StringBuilderListenerAdapter(),
				() -> streamEx.simulateArduinoSend(message)).received).hasToString(message + "\n");
	}

	@Test
	void canReadRawMessagesSent() throws Exception {
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		assertThat(streamEx.withListener(new StringBuilderListenerAdapter(),
				() -> streamEx.link().switchAnalogPin(analogPin(pin), value)).sent)
				.hasToString(alpProtocolMessage(POWER_PIN_INTENSITY).forPin(pin).withValue(value) + "\n");
	}

	@Test
	void twoListenersMustNotInference() throws IOException {
		streamEx.link().addListener(new EventListener() {
			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				throw new IllegalStateException("Listener tries to inference");
			}

			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				throw new IllegalStateException("Listener tries to inference");
			}
		});
		List<String> events = new ArrayList<>();
		streamEx.link().addListener(new EventListener() {
			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				events.add("AnalogPinValueChangedEvent");
			}

			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				events.add("DigitalPinValueChangedEvent");
			}
		});
		int pin = anyPositive(int.class);
		String message1 = alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(anyOtherValueThan(pin));
		String message2 = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(true);
		streamEx.simulateArduinoSend(message1, message2);
		assertThat(Joiner.on(",").join(events)).isEqualTo("AnalogPinValueChangedEvent,DigitalPinValueChangedEvent");
	}

	@Test
	void unparseableInput() throws IOException {
		String message1 = "eXTRaoRdINARy dATa";
		streamEx.simulateArduinoSend(message1);
		String message2 = alpProtocolMessage(DIGITAL_PIN_READ).forPin(anyPositive(int.class)).withState(true);
		streamEx.simulateArduinoSend(message2, message2);
	}

	@Test
	void doesDeregisterAllListenersBeforeClosing() throws IOException {
		StringBuilder sb = new StringBuilder();
		streamEx.link().addListener(new EventListener() {
			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				sb.append(event);
			}

			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				sb.append(event);
			}
		});
		streamEx.link().close();
		streamEx.link().switchAnalogPin(analogPin(anyPositive(int.class)), anyPositive(int.class));
		assertThat(sb).isEmpty();
	}

	private int anyPositive(Class<? extends Number> numClass) {
		return 42;
	}

	private int anyOtherPin(int pin) {
		return anyOtherValueThan(pin);
	}

	private int anyOtherValueThan(int value) {
		return value + 1;
	}

	private void assertToArduinoWasSent(String... messages) {
		assertToArduinoWasSent(Joiner.on("\n").join(messages));
	}

	private void assertToArduinoWasSent(String message) {
		assertThat(streamEx.toArduinoWasSent()).isEqualTo(message + "\n");
	}

}
