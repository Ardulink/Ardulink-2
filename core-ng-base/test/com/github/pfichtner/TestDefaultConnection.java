package com.github.pfichtner;

import static com.github.pfichtner.Pin.analogPin;
import static com.github.pfichtner.Pin.digitalPin;
import static com.github.pfichtner.hamcrest.EventMatchers.eventFor;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_INTENSITY;
import static com.github.pfichtner.proto.impl.ALProtoBuilder.ALPProtocolKey.POWER_PIN_SWITCH;
import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.github.pfichtner.events.AnalogPinValueChangedEvent;
import com.github.pfichtner.events.DigitalPinValueChangedEvent;
import com.github.pfichtner.events.EventListenerAdapter;
import com.github.pfichtner.proto.impl.ArdulinkProtocol;

public class TestDefaultConnection {

	private static final int TIMEOUT = 5 * 1000;
	private static final ArdulinkProtocol AL_PROTO = new ArdulinkProtocol();

	private PipedOutputStream arduinosOutputStream;
	private final ByteArrayOutputStream os = new ByteArrayOutputStream();
	private Link link;
	private final AtomicInteger bytesRead = new AtomicInteger();

	@Before
	public void setup() throws IOException {
		PipedInputStream pis = new PipedInputStream();
		this.arduinosOutputStream = new PipedOutputStream(pis);
		this.link = new Link(new DefaultConnection(pis, os), AL_PROTO) {
			@Override
			protected void received(byte[] bytes) {
				super.received(bytes);
				TestDefaultConnection.this.bytesRead.addAndGet(bytes.length);
			}
		};
	}

	@Test(timeout = TIMEOUT)
	public void canSendAnalogValue() throws IOException {
		int pin = anyPositive(int.class);
		int value = anyPositive(int.class);
		this.link.switchAnalogPin(analogPin(pin), value);
		assertThat(toArduinoWasSent(),
				is(alpProtocolMessage(POWER_PIN_INTENSITY).forPin(pin)
						.withValue(value)));
	}

	@Test(timeout = TIMEOUT)
	public void canSendDigitalValue() throws IOException {
		int pin = anyPositive(int.class);
		this.link.switchDigitalPin(digitalPin(pin), true);
		assertThat(toArduinoWasSent(), is(alpProtocolMessage(POWER_PIN_SWITCH)
				.forPin(pin).withState(true)));
	}

	private String toArduinoWasSent() {
		return this.os.toString();
	}

	@Test(timeout = TIMEOUT)
	public void canReceiveAnalogPinChange() throws IOException {
		final List<AnalogPinValueChangedEvent> analogEvents = new ArrayList<AnalogPinValueChangedEvent>();
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
		waitUntilRead(this.bytesRead, message.length() - 1);
		assertThat(analogEvents, is(eventFor(analogPin(pin)).withValue(value)));
	}

	@Test(timeout = TIMEOUT)
	public void canReceiveDigitalPinChange() throws IOException {
		final List<DigitalPinValueChangedEvent> digitalEvents = new ArrayList<DigitalPinValueChangedEvent>();
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
		waitUntilRead(this.bytesRead, message.length() - 1);
		assertThat(digitalEvents,
				is(is(eventFor(digitalPin(pin)).withValue(true))));
	}

	private int anyPositive(Class<? extends Number> numClass) {
		return new Random(System.currentTimeMillis()).nextInt(MAX_VALUE);
	}

	private void simulateArdunoSend(String message) throws IOException {
		this.arduinosOutputStream.write(message.getBytes());
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
