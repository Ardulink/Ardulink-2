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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
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
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
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

	private static final Protocol proto = ArdulinkProtocol2.instance();

	// TODO PF Migrate to @Rule Arduino
	private PipedOutputStream arduinosOutputStream;
	private final ByteArrayOutputStream os = new ByteArrayOutputStream();
	private Connection connection;
	private ConnectionBasedLink link;
	private final AtomicInteger bytesRead = new AtomicInteger();

	@Before
	public void setup() throws IOException {
		PipedInputStream pis = new PipedInputStream();
		this.arduinosOutputStream = new PipedOutputStream(pis);
		this.connection = new StreamConnection(pis, os, proto);
		this.link = new ConnectionBasedLink(connection, proto) {
			@Override
			protected void received(byte[] bytes) {
				super.received(bytes);
				ConnectionBasedLinkTest.this.bytesRead.addAndGet(bytes.length);
			}
		};
	}

	@After
	public void tearDown() throws IOException {
		if(this.link != null) {
			this.link.close();
		}
	}

	@Test
	public void canSendAnalogValue() throws IOException {
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		this.link.switchAnalogPin(analogPin(pin), value);
		assertThat(toArduinoWasSent(), is("alp://ppin/" + pin + "/" + value
				+ new String(proto.getSeparator())));
	}

	@Test
	public void canSendDigitalValue() throws IOException {
		int pin = anyPositive(int.class);
		this.link.switchDigitalPin(digitalPin(pin), true);
		assertThat(toArduinoWasSent(), is("alp://ppsw/" + pin + "/1\n"));
	}

	@Test
	public void doesSendStartListeningAnalogCommangToArduino()
			throws IOException {
		int pin = anyPositive(int.class);
		this.link.addListener(new FilteredEventListenerAdapter(analogPin(pin),
				null));
		assertThat(toArduinoWasSent(), is("alp://srla/" + pin
				+ new String(proto.getSeparator())));
	}

	@Test
	public void doesSendStopListeningAnalogCommangToArduino()
			throws IOException {
		int pin = anyPositive(int.class);
		FilteredEventListenerAdapter l1 = new FilteredEventListenerAdapter(
				analogPin(pin), null);
		FilteredEventListenerAdapter l2 = new FilteredEventListenerAdapter(
				analogPin(pin), null);
		this.link.addListener(l1);
		this.link.addListener(l2);
		String m1 = "alp://srla/" + pin + new String(proto.getSeparator());
		assertThat(toArduinoWasSent(), is(m1 + m1));
		this.link.removeListener(l1);
		this.link.removeListener(l2);
		String m2 = "alp://spla/" + pin + new String(proto.getSeparator());
		assertThat(toArduinoWasSent(), is(m1 + m1 + m2));
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
		String message = alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.withValue(value);
		simulateArdunoSend(message);
		waitUntilRead(this.bytesRead, message.length());
		assertThat(analogEvents,
				hasItems(eventFor(analogPin(pin)).withValue(value)));
	}

	@Test
	public void doesSendStartListeningDigitalCommangToArduino()
			throws IOException {
		int pin = anyPositive(int.class);
		this.link.addListener(new FilteredEventListenerAdapter(digitalPin(pin),
				null));
		assertThat(toArduinoWasSent(), is("alp://srld/" + pin
				+ new String(proto.getSeparator())));
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
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin)
				.withState(true);
		simulateArdunoSend(message);
		waitUntilRead(this.bytesRead, message.length());
		assertThat(digitalEvents,
				hasItems(eventFor(digitalPin(pin)).withValue(true)));
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
		this.link.addListener(new FilteredEventListenerAdapter(
				digitalPin(anyOtherPin(pin)), listener));
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin)
				.withState(true);
		simulateArdunoSend(message);
		waitUntilRead(this.bytesRead, message.length());
		List<DigitalPinValueChangedEvent> emptyList = Collections.emptyList();
		assertThat(digitalEvents, is(emptyList));
	}

	@Test
	public void canSendKbdEvents() throws IOException {
		this.link.sendKeyPressEvent('#', 1, 2, 3, 4);
		assertThat(toArduinoWasSent(), is("alp://kprs/chr#cod1loc2mod3mex4\n"));
	}

	@Test
	public void canSendToneWithDuration() throws IOException {
		this.link.sendTone(Tone.forPin(analogPin(2)).withHertz(3000)
				.withDuration(5, SECONDS));
		assertThat(toArduinoWasSent(), is("alp://tone/2/3000/5000\n"));
	}

	@Test
	public void canSendEndlessTone() throws IOException {
		this.link.sendTone(Tone.forPin(analogPin(2)).withHertz(3000).endless());
		assertThat(toArduinoWasSent(), is("alp://tone/2/3000/-1\n"));
	}

	@Test
	public void canSendNoTone() throws IOException {
		this.link.sendNoTone(analogPin(5));
		assertThat(toArduinoWasSent(), is("alp://notn/5\n"));
	}

	@Test
	public void canSendCustomMessageSingleValue() throws IOException {
		String message = "myMessage";
		this.link.sendCustomMessage(message);
		assertThat(toArduinoWasSent(), is("alp://cust/" + message + "\n"));
	}

	@Test
	public void canSendCustomMessageMultiValue() throws IOException {
		this.link.sendCustomMessage("1", "2", "3");
		assertThat(toArduinoWasSent(), is("alp://cust/1/2/3\n"));
	}

	@Test
	public void canReadRawMessagesRead() throws IOException {
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(
				anyPositive(int.class)).withState(true);
		final StringBuilder sb = new StringBuilder();
		this.connection.addListener(new ListenerAdapter() {
			@Override
			public void received(byte[] bytes) throws IOException {
				sb.append(new String(bytes));
			}
		});
		simulateArdunoSend(message);
		waitUntilRead(this.bytesRead, message.length());
		assertThat(sb.toString(), is(message));
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
		assertThat(sb.toString(), is("alp://ppin/" + pin + "/" + value
				+ new String(proto.getSeparator())));
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
		final StringBuilder sb = new StringBuilder();
		this.link.addListener(new EventListener() {
			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				sb.append("AnalogPinValueChangedEvent, ");
			}

			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				sb.append("DigitalPinValueChangedEvent");
			}
		});
		int pin = anyPositive(int.class);
		String m1 = alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withState(
				true);
		String m2 = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(
				true);
		simulateArdunoSend(m1);
		simulateArdunoSend(m2);
		waitUntilRead(this.bytesRead, m1.length() + m2.length());
		assertThat(sb.toString(),
				is("AnalogPinValueChangedEvent, DigitalPinValueChangedEvent"));
	}

	@Test
	public void unparseableInput() throws IOException {
		String m1 = "eXTRaoRdINARy dATa";
		simulateArdunoSend(m1);
		String m2 = alpProtocolMessage(DIGITAL_PIN_READ).forPin(
				anyPositive(int.class)).withState(true);
		simulateArdunoSend(m2);
		simulateArdunoSend(m2);
		waitUntilRead(this.bytesRead, 2 * m2.length());
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
		this.link.switchAnalogPin(analogPin(anyPositive(int.class)),
				anyPositive(int.class));
		assertThat(sb.toString(), is(""));
	}

	private int anyPositive(Class<? extends Number> numClass) {
		return new Random(System.currentTimeMillis()).nextInt(MAX_VALUE);
	}

	private int anyOtherPin(int pin) {
		return pin + 1;
	}

	private void simulateArdunoSend(String message) throws IOException {
		this.arduinosOutputStream.write((message).getBytes());
		this.arduinosOutputStream.write(proto.getSeparator());
	}

	private String toArduinoWasSent() {
		return this.os.toString();
	}

	private static void waitUntilRead(AtomicInteger completed, int toRead) {
		while (completed.get() < toRead) {
			try {
				MILLISECONDS.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
