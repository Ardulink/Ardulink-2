package org.zu.ardulink.connection.proxy.sim;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.ToArduinoPinEvent;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocolN;

public class SoftwareArduinoTest {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	private final AtomicInteger bytesRead = new AtomicInteger();

	@Test
	public void canSwitchDigitalPin() throws IOException, InterruptedException {
		final List<Pin> pins = new ArrayList<Pin>();
		Protocol proto = ArdulinkProtocolN.instance();
		PipedInputStream is = new PipedInputStream();
		PipedOutputStream os = new PipedOutputStream(is);
		try {
			SoftwareArduino arduino = new SoftwareArduino(proto, is) {

				@Override
				protected void received(byte[] bytes) throws Exception {
					super.received(bytes);
					bytesRead.addAndGet(bytes.length);
				}

				@Override
				protected void switchAnalogPin(AnalogPin analogPin,
						Integer value) {
					pins.add(analogPin);
				}

				@Override
				protected void switchDigitalPin(DigitalPin digitalPin,
						Boolean value) {
					pins.add(digitalPin);
				}

			};
			try {
				byte[] message = proto.toArduino(new ToArduinoPinEvent(
						digitalPin(1), true));
				os.write(message);
				waitUntilRead(this.bytesRead, message.length - 1);
				assertThat(pins, hasItem(digitalPin(1)));
			} finally {
				arduino.close();
			}
		} finally {
			os.close();
		}
	}

	@Test
	public void canSwitchAnalogPin() throws IOException, InterruptedException {
		final List<Pin> pins = new ArrayList<Pin>();
		Protocol proto = ArdulinkProtocolN.instance();
		PipedInputStream is = new PipedInputStream();
		PipedOutputStream os = new PipedOutputStream(is);
		try {
			SoftwareArduino arduino = new SoftwareArduino(proto, is) {

				@Override
				protected void received(byte[] bytes) throws Exception {
					super.received(bytes);
					bytesRead.addAndGet(bytes.length);
				}

				@Override
				protected void switchDigitalPin(DigitalPin digitalPin,
						Boolean value) {
					pins.add(digitalPin);

				}

				@Override
				protected void switchAnalogPin(AnalogPin analogPin,
						Integer value) {
					pins.add(analogPin);
				}

			};
			try {
				byte[] message = proto.toArduino(new ToArduinoPinEvent(
						analogPin(2), 222));
				os.write(message);
				waitUntilRead(this.bytesRead, message.length - 1);
				assertThat(pins, hasItem(analogPin(2)));
			} finally {
				arduino.close();
			}
		} finally {
			os.close();
		}
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
