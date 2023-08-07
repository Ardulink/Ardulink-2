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

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.events.DefaultAnalogPinValueChangedEvent.analogPinValueChanged;
import static org.ardulink.core.events.DefaultDigitalPinValueChangedEvent.digitalPinValueChanged;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ardulink.core.Connection.Listener;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.FilteredEventListenerAdapter;
import org.ardulink.core.events.PinValueChangedEvent;
import org.ardulink.testsupport.junit5.ArduinoStubExt;
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

	private static final class StateChangeCollector implements EventListener {

		private final List<PinValueChangedEvent> digitalEvents = new ArrayList<>();
		private final List<PinValueChangedEvent> analogEvents = new ArrayList<>();

		@Override
		public void stateChanged(AnalogPinValueChangedEvent event) {
			analogEvents.add(event);
		}

		@Override
		public void stateChanged(DigitalPinValueChangedEvent event) {
			digitalEvents.add(event);
		}
	}

	private static EventListener exceptionThrowingListener() {
		return verifyThrowsExceptions(mock(EventListener.class, __ -> {
			throw new RuntimeException();
		}));
	}

	private static EventListener verifyThrowsExceptions(EventListener listener) {
		assertThrows(RuntimeException.class, () -> listener.stateChanged(analogPinValueChanged(analogPin(1), 2)));
		assertThrows(RuntimeException.class, () -> listener.stateChanged(digitalPinValueChanged(digitalPin(3), true)));
		return listener;
	}

	@RegisterExtension
	ArduinoStubExt arduinoStub = new ArduinoStubExt();

	@Test
	void canSendAnalogValue() throws IOException {
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		arduinoStub.link().switchAnalogPin(analogPin(pin), value);
		assertToArduinoWasSent(format("alp://ppin/%d/%d", pin, value));
	}

	@Test
	void canSendDigitalValue() throws IOException {
		int pin = anyPositive(int.class);
		arduinoStub.link().switchDigitalPin(digitalPin(pin), true);
		assertToArduinoWasSent(format("alp://ppsw/%d/%d", pin, 1));
	}

	@Test
	void doesSendStartListeningAnalogCommangToArduino() throws IOException {
		int pin = anyPositive(int.class);
		arduinoStub.link().addListener(new FilteredEventListenerAdapter(analogPin(pin), null));
		assertToArduinoWasSent(format("alp://srla/%d", pin));
	}

	@Test
	void doesSendStopListeningAnalogCommangToArduino() throws IOException {
		ConnectionBasedLink link = arduinoStub.link();
		int pin = anyPositive(int.class);
		FilteredEventListenerAdapter l1 = new FilteredEventListenerAdapter(analogPin(pin), null);
		FilteredEventListenerAdapter l2 = new FilteredEventListenerAdapter(analogPin(pin), null);
		link.addListener(l1);
		link.addListener(l2);
		link.removeListener(l1);
		link.removeListener(l2);
		String startListening = format("alp://srla/%d", pin);
		String stopListening = format("alp://spla/%d", pin);
		assertToArduinoWasSent(startListening, startListening, stopListening);
	}

	@Test
	void canReceiveAnalogPinChange() throws IOException {
		StateChangeCollector listener = new StateChangeCollector();
		arduinoStub.link().addListener(listener);
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		String message = alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value);
		arduinoStub.simulateArduinoSends(lf(message));
		assertThat(listener.analogEvents).contains(analogPinValueChanged(analogPin(pin), value));
	}

	@Test
	void doesSendStartListeningDigitalCommangToArduino() throws IOException {
		int pin = anyPositive(int.class);
		arduinoStub.link().addListener(new FilteredEventListenerAdapter(digitalPin(pin), null));
		assertToArduinoWasSent(format("alp://srld/%d", pin));
	}

	@Test
	void canReceiveDigitalPinChange() throws IOException {
		StateChangeCollector listener = new StateChangeCollector();
		arduinoStub.link().addListener(listener);
		int pin = anyPositive(int.class);
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(true);
		arduinoStub.simulateArduinoSends(lf(message));
		assertThat(listener.digitalEvents).contains(digitalPinValueChanged(digitalPin(pin), true));
	}

	@Test
	void canFilterPins() throws IOException {
		int pin = anyPositive(int.class);
		StateChangeCollector listener = new StateChangeCollector();
		arduinoStub.link().addListener(new FilteredEventListenerAdapter(digitalPin(anyOtherPin(pin)), listener));
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(true);
		arduinoStub.simulateArduinoSends(lf(message));
		assertThat(listener.digitalEvents).isEmpty();
	}

	@Test
	void canSendKbdEvents() throws IOException {
		int cod = 1;
		int loc = 2;
		int mod = 3;
		int mex = 4;
		arduinoStub.link().sendKeyPressEvent('#', cod, loc, mod, mex);
		assertToArduinoWasSent(format("alp://kprs/chr#cod%dloc%dmod%dmex%d", cod, loc, mod, mex));
	}

	@Test
	void canSendToneWithDuration() throws IOException {
		int pin = anyPositive(int.class);
		int hertz = 3000;
		int durationSecs = 5;
		arduinoStub.link().sendTone(Tone.forPin(analogPin(pin)).withHertz(hertz).withDuration(durationSecs, SECONDS));
		assertToArduinoWasSent(format("alp://tone/%d/%d/%d", pin, hertz, SECONDS.toMillis(durationSecs)));
	}

	@Test
	void canSendEndlessTone() throws IOException {
		int pin = anyPositive(int.class);
		int hertz = 3000;
		arduinoStub.link().sendTone(Tone.forPin(analogPin(pin)).withHertz(hertz).endless());
		assertToArduinoWasSent(format("alp://tone/%d/%d/-1", pin, hertz));
	}

	@Test
	void canSendNoTone() throws IOException {
		int pin = anyPositive(int.class);
		arduinoStub.link().sendNoTone(analogPin(pin));
		assertToArduinoWasSent(format("alp://notn/%d", pin));
	}

	@Test
	void canSendCustomMessage() throws IOException {
		arduinoStub.link().sendCustomMessage("1", "2", "3");
		assertToArduinoWasSent("alp://cust/1/2/3");
	}

	@Test
	void canReadRawMessagesRead() throws Exception {
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(anyPositive(int.class)).withState(true);
		assertThat(arduinoStub.withListener(new StringBuilderListenerAdapter(),
				() -> arduinoStub.simulateArduinoSends(lf(message))).received).hasToString(message + "\n");
	}

	@Test
	void canReadRawMessagesSent() throws Exception {
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		assertThat(arduinoStub.withListener(new StringBuilderListenerAdapter(),
				() -> arduinoStub.link().switchAnalogPin(analogPin(pin), value)).sent)
				.hasToString(format("alp://ppin/%d/%d\n", pin, value));
	}

	@Test
	void listenersCannotInference() throws IOException {
		ConnectionBasedLink link = arduinoStub.link();
		link.addListener(exceptionThrowingListener());
		StateChangeCollector listener = new StateChangeCollector();
		link.addListener(listener);
		link.addListener(exceptionThrowingListener());
		int pin = anyPositive(int.class);
		String message1 = alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(anyOtherValueThan(pin));
		String message2 = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(true);
		arduinoStub.simulateArduinoSends(lf(message1), lf(message2));
		assertThat(listener.analogEvents).hasSize(1);
		assertThat(listener.digitalEvents).hasSize(1);
	}

	@Test
	void unparseableInput() throws IOException {
		String message1 = "eXTRaoRdINARy unParSEable dATa";
		arduinoStub.simulateArduinoSends(lf(message1));
		String message2 = alpProtocolMessage(DIGITAL_PIN_READ).forPin(anyPositive(int.class)).withState(true);
		arduinoStub.simulateArduinoSends(lf(message2), lf(message2));
	}

	@Test
	void doesDeregisterAllListenersBeforeClosing() throws IOException {
		ConnectionBasedLink link = arduinoStub.link();
		StateChangeCollector listener = new StateChangeCollector();
		link.addListener(listener);
		link.close();
		link.switchAnalogPin(analogPin(anyPositive(int.class)), anyPositive(int.class));
		assertThat(listener.analogEvents).isEmpty();
		assertThat(listener.digitalEvents).isEmpty();
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
		assertThat(arduinoStub.toArduinoWasSent()).isEqualTo(message + "\n");
	}

	private static String lf(String string) {
		return string + "\n";
	}

}
