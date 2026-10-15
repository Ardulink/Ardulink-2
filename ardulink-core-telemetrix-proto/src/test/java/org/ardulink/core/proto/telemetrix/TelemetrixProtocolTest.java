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

package org.ardulink.core.proto.telemetrix;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageCustom.toDeviceMessageCustom;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageKeyPress.toDeviceMessageKeyPress;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessagePinStateChange.toDeviceMessagePinStateChange;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessagePing.toDeviceMessagePing;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageStartListening.toDeviceMessageStartListening;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageStopListening.toDeviceMessageStopListening;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageTone.toDeviceMessageTone;
import static org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessors.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessageInfo;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageNoTone;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class TelemetrixProtocolTest {

	private final ByteStreamProcessor sut = new TelemetrixProtocol().newByteStreamProcessor();

	private byte[] bytes;
	private List<FromDeviceMessage> messages = new ArrayList<>();

	// -------------------------------------------------------------------------
	// outgoing

	@Test
	void canSendPing() {
		assertThat(sut.toDevice(toDeviceMessagePing())).containsExactly(5);
	}

	@Test
	void canSetDigitalPin() {
		DigitalPin pin = digitalPin(13);
		assertSoftly(s -> {
			s.assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, true))).containsExactly(1, 13, 1, 0, 0, 2, 13,
					1);
			s.assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, false))).containsExactly(2, 13, 0);
		});
	}

	@Test
	void reSendsOutputModeAfterListeningWasEnabled() {
		DigitalPin pin = digitalPin(13);
		assertSoftly(s -> {
			s.assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, true))).containsExactly(1, 13, 1, 0, 0, 2, 13,
					1);
			s.assertThat(sut.toDevice(toDeviceMessageStartListening(pin))).containsExactly(1, 13, 0, 1, 0);
			s.assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, false))).containsExactly(1, 13, 1, 0, 0, 2, 13,
					0);
		});
	}

	@Test
	void canSetPwmPin() {
		AnalogPin pin = analogPin(9);
		assertSoftly(s -> {
			s.assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, 42))).containsExactly(1, 9, 1, 0, 0, 3, 9, 42);
			s.assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, 255))).containsExactly(3, 9, 255);
		});
	}

	@Test
	void canEnableDisableAnalogListening() {
		Pin pin = analogPin(2);
		assertSoftly(s -> {
			s.assertThat(sut.toDevice(toDeviceMessageStartListening(pin))).containsExactly(1, 2, 3, 1, 0);
			s.assertThat(sut.toDevice(toDeviceMessageStopListening(pin))).containsExactly(1, 2, 3, 0, 0);
		});
	}

	@Test
	void canEnableDisableDigitalListening() {
		Pin pin = digitalPin(12);
		assertSoftly(s -> {
			s.assertThat(sut.toDevice(toDeviceMessageStartListening(pin))).containsExactly(1, 12, 0, 1, 0);
			s.assertThat(sut.toDevice(toDeviceMessageStopListening(pin))).containsExactly(1, 12, 0, 0, 0);
		});
	}

	@Test
	void doesNotSupportKeyPressToneNoToneAndCustomMessages() {
		assertSoftly(s -> {
			s.assertThatThrownBy(() -> sut.toDevice(toDeviceMessageKeyPress('a', 0, 0, 0, 0)))
					.isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("key press events");
			s.assertThatThrownBy(
					() -> sut.toDevice(toDeviceMessageTone(Tone.forPin(analogPin(9)).withHertz(440).endless())))
					.isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("does not support tones");
			s.assertThatThrownBy(() -> sut.toDevice(DefaultToDeviceMessageNoTone.toDeviceMessageNoTone(analogPin(2))))
					.isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("does not support tones");
			s.assertThatThrownBy(() -> sut.toDevice(toDeviceMessageCustom("custom")))
					.isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("custom messages");
		});
	}

	// -------------------------------------------------------------------------
	// incoming

	@Test
	void canReadAnalogPin() throws IOException {
		givenMessage(3, 15, 42, 127);
		whenMessageIsProcessed();
		thenMessageIs(analogPin(15), 42 << 8 | 127);
	}

	@Test
	void canReadDigitalPin() throws IOException {
		givenMessage(2, 5, 1);
		whenMessageIsProcessed();
		thenMessageIs(digitalPin(5), true);
	}

	@Test
	void canReadDigitalPinLow() throws IOException {
		givenMessage(2, 5, 0);
		whenMessageIsProcessed();
		thenMessageIs(digitalPin(5), false);
	}

	@Test
	void canReadFirmwareReport() throws IOException {
		givenMessage(5, 0, 1);
		whenMessageIsProcessed();
		assertThat(messages).singleElement().isInstanceOf(FromDeviceMessageInfo.class);
	}

	@Test
	void canReadIAmHereReport() throws IOException {
		givenMessage(6, 42);
		whenMessageIsProcessed();
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessageInfo.class,
				i -> assertThat(i.parameters()).containsExactlyInAnyOrderEntriesOf(Map.of("arduinoId", 42)));
	}

	@Test
	void ignoresLoopBackAndDebugReports() throws IOException {
		givenMessage(0, 7, /**/ 99, 1, 0, 42);
		whenMessageIsProcessed();
		assertThat(messages).isEmpty();
	}

	@Test
	void ignoresUnknownReportIds() throws IOException {
		givenMessage(7, 7);
		whenMessageIsProcessed();
		assertThat(messages).isEmpty();
	}

	// -------------------------------------------------------------------------

	private void givenMessage(int... ints) {
		byte[] bytes = new byte[ints.length];
		for (int i = 0; i < ints.length; i++) {
			bytes[i] = (byte) ints[i];
		}
		this.bytes = bytes;
	}

	private void thenMessageIs(Pin pin, Object value) {
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessagePinStateChanged.class,
				e -> assertSoftly(s -> {
					s.assertThat(e.getPin()).isEqualTo(pin);
					s.assertThat(e.getValue()).isEqualTo(value);
				}));
	}

	private void whenMessageIsProcessed() throws IOException {
		// read in "random" (two) junks
		messages = new ArrayList<>();
		InputStream stream = new ByteArrayInputStream(bytes);
		messages.addAll(parse(sut, read(stream, 2)));
		messages.addAll(parse(sut, read(stream, stream.available())));
	}

	private static byte[] read(InputStream stream, int length) throws IOException {
		byte[] bytes = new byte[length];
		stream.read(bytes);
		return bytes;
	}

}
