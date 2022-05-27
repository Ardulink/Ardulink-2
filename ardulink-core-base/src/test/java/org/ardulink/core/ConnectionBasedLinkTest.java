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

import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.hamcrest.EventMatchers.eventFor;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_SWITCH;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.util.Bytes.concat;
import static org.ardulink.util.anno.LapsedWith.JDK8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardulink.core.Connection.ListenerAdapter;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.EventListenerAdapter;
import org.ardulink.core.events.FilteredEventListenerAdapter;
import org.ardulink.core.events.PinValueChangedEvent;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.util.Joiner;
import org.ardulink.util.Lists;
import org.ardulink.util.anno.LapsedWith;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ConnectionBasedLinkTest {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	// TODO PF Migrate to @Rule Arduino
	private PipedOutputStream arduinosOutputStream;
	private final ByteArrayOutputStream os = new ByteArrayOutputStream();
	private Connection connection;
	private ConnectionBasedLinkNG link;
	private final AtomicInteger bytesNotYetRead = new AtomicInteger();

	@Before
	public void setup() throws IOException {
		PipedInputStream pis = new PipedInputStream();
		this.arduinosOutputStream = new PipedOutputStream(pis);
		ByteStreamProcessor byteStreamProcessor = new ArdulinkProtocol2().newByteStreamProcessor();
		this.connection = new StreamConnection(pis, os, byteStreamProcessor);
		this.connection.addListener(new ListenerAdapter() {
			@Override
			public void received(byte[] bytes) throws IOException {
				ConnectionBasedLinkTest.this.bytesNotYetRead.addAndGet(-bytes.length);
			}
		});
		this.link = new ConnectionBasedLinkNG(connection, byteStreamProcessor);
	}

	@After
	public void tearDown() throws IOException {
		if (this.link != null) {
			this.link.close();
		}
	}

	@Test
	public void canSendAnalogValue() throws IOException {
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		this.link.switchAnalogPin(analogPin(pin), value);
		assertToArduinoWasSent(alpProtocolMessage(POWER_PIN_INTENSITY).forPin(pin).withValue(value));
	}

	@Test
	public void canSendDigitalValue() throws IOException {
		int pin = anyPositive(int.class);
		this.link.switchDigitalPin(digitalPin(pin), true);
		assertToArduinoWasSent(alpProtocolMessage(POWER_PIN_SWITCH).forPin(pin).withState(true));
	}

	@Test
	public void doesSendStartListeningAnalogCommangToArduino() throws IOException {
		int pin = anyPositive(int.class);
		this.link.addListener(new FilteredEventListenerAdapter(analogPin(pin), null));
		assertToArduinoWasSent(alpProtocolMessage(START_LISTENING_ANALOG).forPin(pin).withoutValue());
	}

	@Test
	public void doesSendStopListeningAnalogCommangToArduino() throws IOException {
		int pin = anyPositive(int.class);
		FilteredEventListenerAdapter l1 = new FilteredEventListenerAdapter(analogPin(pin), null);
		FilteredEventListenerAdapter l2 = new FilteredEventListenerAdapter(analogPin(pin), null);
		this.link.addListener(l1);
		this.link.addListener(l2);
		String m1 = alpProtocolMessage(START_LISTENING_ANALOG).forPin(pin).withoutValue();
		assertToArduinoWasSent(m1, m1);
		this.link.removeListener(l1);
		this.link.removeListener(l2);
		String m2 = alpProtocolMessage(STOP_LISTENING_ANALOG).forPin(pin).withoutValue();
		assertToArduinoWasSent(m1, m1, m2);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void canReceiveAnalogPinChange() throws IOException {
		final List<PinValueChangedEvent> analogEvents = new ArrayList<PinValueChangedEvent>();
		EventListenerAdapter listener = new EventListenerAdapter() {
			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				analogEvents.add(event);
			}
		};
		this.link.addListener(listener);
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		String message = alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value);
		simulateArduinoSend(message);
		waitUntilRead();
		assertThat(analogEvents, hasItems(eventFor(analogPin(pin)).withValue(value)));
	}

	@Test
	public void doesSendStartListeningDigitalCommangToArduino() throws IOException {
		int pin = anyPositive(int.class);
		this.link.addListener(new FilteredEventListenerAdapter(digitalPin(pin), null));
		assertToArduinoWasSent(alpProtocolMessage(START_LISTENING_DIGITAL).forPin(pin).withoutValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void canReceiveDigitalPinChange() throws IOException {
		final List<PinValueChangedEvent> digitalEvents = new ArrayList<PinValueChangedEvent>();
		EventListenerAdapter listener = new EventListenerAdapter() {
			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				digitalEvents.add(event);
			}
		};
		this.link.addListener(listener);
		int pin = anyPositive(int.class);
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(true);
		simulateArduinoSend(message);
		waitUntilRead();
		assertThat(digitalEvents, hasItems(eventFor(digitalPin(pin)).withValue(true)));
	}

	@Test
	public void canFilterPins() throws IOException {
		int pin = anyPositive(int.class);
		final List<DigitalPinValueChangedEvent> digitalEvents = new ArrayList<DigitalPinValueChangedEvent>();
		EventListenerAdapter listener = new EventListenerAdapter() {
			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				digitalEvents.add(event);
			}
		};
		this.link.addListener(new FilteredEventListenerAdapter(digitalPin(anyOtherPin(pin)), listener));
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(true);
		simulateArduinoSend(message);
		waitUntilRead();
		List<DigitalPinValueChangedEvent> emptyList = Collections.emptyList();
		assertThat(digitalEvents, is(emptyList));
	}

	@Test
	public void canSendKbdEvents() throws IOException {
		this.link.sendKeyPressEvent('#', 1, 2, 3, 4);
		assertToArduinoWasSent("alp://kprs/chr#cod1loc2mod3mex4");
	}

	@Test
	public void canSendToneWithDuration() throws IOException {
		this.link.sendTone(Tone.forPin(analogPin(2)).withHertz(3000).withDuration(5, SECONDS));
		assertToArduinoWasSent("alp://tone/2/3000/5000");
	}

	@Test
	public void canSendEndlessTone() throws IOException {
		this.link.sendTone(Tone.forPin(analogPin(2)).withHertz(3000).endless());
		assertToArduinoWasSent("alp://tone/2/3000/-1");
	}

	@Test
	public void canSendNoTone() throws IOException {
		this.link.sendNoTone(analogPin(5));
		assertToArduinoWasSent("alp://notn/5");
	}

	@Test
	public void canSendCustomMessageSingleValue() throws IOException {
		String message = "myMessage";
		this.link.sendCustomMessage(message);
		assertToArduinoWasSent("alp://cust/" + message);
	}

	@Test
	public void canSendCustomMessageMultiValue() throws IOException {
		this.link.sendCustomMessage("1", "2", "3");
		assertToArduinoWasSent("alp://cust/1/2/3");
	}

	@Test
	public void canReadRawMessagesRead() throws IOException {
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(anyPositive(int.class)).withState(true);
		final StringBuilder sb = new StringBuilder();
		this.connection.addListener(new ListenerAdapter() {
			@Override
			public void received(byte[] bytes) throws IOException {
				sb.append(new String(bytes));
			}
		});
		simulateArduinoSend(message);
		waitUntilRead();
		assertThat(sb.toString(), is(message + "\n"));
	}

	@Test
	public void canReadRawMessagesSent() throws IOException {
		final StringBuilder sb = new StringBuilder();
		this.connection.addListener(new ListenerAdapter() {
			@Override
			public void sent(byte[] bytes) throws IOException {
				sb.append(new String(bytes));
			}
		});
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		this.link.switchAnalogPin(analogPin(pin), value);
		assertThat(sb.toString(), is(alpProtocolMessage(POWER_PIN_INTENSITY).forPin(pin).withValue(value) + "\n"));
	}

	@Test
	public void twoListenersMustNotInference() throws IOException {
		this.link.addListener(new EventListener() {
			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				throw new IllegalStateException("Listener tries to inference");
			}

			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				throw new IllegalStateException("Listener tries to inference");
			}
		});
		final List<String> events = Lists.newArrayList();
		this.link.addListener(new EventListener() {
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
		String m1 = alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(42);
		String m2 = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(true);
		simulateArduinoSend(m1);
		simulateArduinoSend(m2);
		waitUntilRead();
		assertThat(Joiner.on(",").join(events), is("AnalogPinValueChangedEvent,DigitalPinValueChangedEvent"));
	}

	@Test
	public void unparseableInput() throws IOException {
		String m1 = "eXTRaoRdINARy dATa";
		simulateArduinoSend(m1);
		String m2 = alpProtocolMessage(DIGITAL_PIN_READ).forPin(anyPositive(int.class)).withState(true);
		simulateArduinoSend(m2);
		simulateArduinoSend(m2);
		waitUntilRead();
	}

	@Test
	public void doesDeregisterAllListenersBeforeClosing() throws IOException {
		final StringBuilder sb = new StringBuilder();
		this.link.addListener(new EventListener() {
			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				sb.append(event);
			}

			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				sb.append(event);
			}
		});
		this.link.close();
		this.link.switchAnalogPin(analogPin(anyPositive(int.class)), anyPositive(int.class));
		assertThat(sb.toString(), is(""));
	}

	private int anyPositive(Class<? extends Number> numClass) {
		return new Random(System.currentTimeMillis()).nextInt(MAX_VALUE);
	}

	private int anyOtherPin(int pin) {
		return pin + 1;
	}

	private void simulateArduinoSend(String message) throws IOException {
		this.arduinosOutputStream.write(message.getBytes());
		this.arduinosOutputStream.write('\n');
		this.bytesNotYetRead.addAndGet(message.getBytes().length + 1);
	}

	private void assertToArduinoWasSent(String... messages) {
		assertToArduinoWasSent(Joiner.on("\n").join(Arrays.asList(messages)));
	}

	private void assertToArduinoWasSent(String message) {
		assertThat(toArduinoWasSent(), is(message + "\n"));
	}

	private String toArduinoWasSent() {
		return this.os.toString();
	}

	@LapsedWith(value = JDK8, module = "Awaitility")
	private void waitUntilRead() {
		while (bytesNotYetRead.get() > 0) {
			try {
				MILLISECONDS.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
