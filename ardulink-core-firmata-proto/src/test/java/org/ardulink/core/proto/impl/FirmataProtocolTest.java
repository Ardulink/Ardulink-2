package org.ardulink.core.proto.impl;

import static java.lang.Math.pow;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageNoTone.toDeviceMessageNoTone;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessagePinStateChange.toDeviceMessagePinStateChange;
import static org.ardulink.core.messages.impl.DefaultToDeviceMessageTone.toDeviceMessageTone;
import static org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessors.parse;
import static org.ardulink.core.proto.impl.FirmataProtocol.FIRMATA_ENABLED_PROPERTY_FEATURE;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.ANALOG_INPUT;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.DIGITAL_INPUT;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.DIGITAL_OUTPUT;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.I2C;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.INPUT_PULLUP;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.PWM;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.SERVO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.firmata4j.firmata.parser.FirmataToken.ANALOG_MESSAGE;
import static org.firmata4j.firmata.parser.FirmataToken.DIGITAL_MESSAGE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessageInfo;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageStartListening;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageStopListening;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.Protocols;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin;
import org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.ExpectedToFail;
import org.junitpioneer.jupiter.SetSystemProperty;

class FirmataProtocolTest {

	private final ByteStreamProcessor sut = new FirmataProtocol().newByteStreamProcessor();

	private byte[] bytes;
	private List<FromDeviceMessage> messages = new ArrayList<>();

	@Test
	@SetSystemProperty(key = FIRMATA_ENABLED_PROPERTY_FEATURE, value = "anyNonNullValue")
	void firmataIsAvailableIfSystemPropertyIsSet() {
		assertThat(loadFirmata()).isNotEmpty();
	}

	@Test
	@ClearSystemProperty(key = FIRMATA_ENABLED_PROPERTY_FEATURE)
	void firmataIsAbsentIfSystemPropertyIsNotSet() {
		assertThat(loadFirmata()).isEmpty();
	}

	private static Optional<Protocol> loadFirmata() {
		return Protocols.tryProtoByName("Firmata");
	}

	@Test
	void canReadFirmwareStartupResponseAndRequestsCapabilities() throws IOException {
		givenMessage(0xF0, 0x79, 0x01, 0x02, 0x41, 0x0, 0xF7);
		whenMessageIsProcessed();
		assertThat(messages).singleElement().isInstanceOf(FromDeviceMessageInfo.class);
	}

	@Test
	@ExpectedToFail
	void doesRequestsCapabilitiesOnlyOnFirstFirmwareStartupResponse() throws IOException {
		// TODO it's unclear for now, how we can send messages to the arduino since BSP
		// is incoming only.
		// TODO Idea: Let the BSP implement a Pushback-Interface, let StreamConnection
		// combine input/output
		@SuppressWarnings("unused")
		final int[] CAPABILITIES_QUERY = { 0xF0, 0x6B, 0xF7 };

		givenMessage(0xF0, 0x79, 0x01, 0x02, 0x41, 0x0, 0xF7);
		whenMessageIsProcessed();
		givenMessage(capabilitiesQuery());
		whenMessageIsProcessed();
		givenMessage(0xF0, 0x79, 0x01, 0x02, 0x41, 0x0, 0xF7);
		whenMessageIsProcessed();
		assertThat(messages).hasSize(2).allSatisfy(e -> assertThat(e).isInstanceOf(FromDeviceMessageInfo.class));
	}

	@Test
	void canReadCapabilities() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {
		givenMessage(capabilitiesQuery());
		whenMessageIsProcessed();

		// TODO verify via behavior check
		Field declaredField = sut.getClass().getDeclaredField("pins");
		declaredField.setAccessible(true);

		@SuppressWarnings("unchecked")
		Map<Integer, FirmataPin> pins = (Map<Integer, FirmataPin>) declaredField.get(sut);
		assertThat(pins).hasSize(20);
		int cnt = 0;

		assertSupportedModes(cnt++, pins);
		assertSupportedModes(cnt++, pins);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, PWM, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, PWM, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, PWM, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, SERVO);

		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, PWM, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, PWM, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, PWM, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, ANALOG_INPUT, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, ANALOG_INPUT, SERVO);

		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, ANALOG_INPUT, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, ANALOG_INPUT, SERVO);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, ANALOG_INPUT, SERVO, I2C);
		assertSupportedModes(cnt++, pins, DIGITAL_INPUT, INPUT_PULLUP, DIGITAL_OUTPUT, ANALOG_INPUT, SERVO, I2C);
	}

	private static byte[] capabilitiesQuery() {
		return toBytes(0xF0, 0x6C, 0x7F, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01, 0x01, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B,
				0x01, 0x01, 0x01, 0x03, 0x08, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01, 0x01, 0x04, 0x0E, 0x7F,
				0x00, 0x01, 0x0B, 0x01, 0x01, 0x01, 0x03, 0x08, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01, 0x01,
				0x03, 0x08, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01, 0x01, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B,
				0x01, 0x01, 0x01, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01, 0x01, 0x03, 0x08, 0x04, 0x0E, 0x7F,
				0x00, 0x01, 0x0B, 0x01, 0x01, 0x01, 0x03, 0x08, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01, 0x01,
				0x03, 0x08, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01, 0x01, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B,
				0x01, 0x01, 0x01, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01, 0x01, 0x02, 0x0A, 0x04, 0x0E, 0x7F,
				0x00, 0x01, 0x0B, 0x01, 0x01, 0x01, 0x02, 0x0A, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01, 0x01,
				0x02, 0x0A, 0x04, 0x0E, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01, 0x01, 0x02, 0x0A, 0x04, 0x0E, 0x7F, 0x00,
				0x01, 0x0B, 0x01, 0x01, 0x01, 0x02, 0x0A, 0x04, 0x0E, 0x06, 0x01, 0x7F, 0x00, 0x01, 0x0B, 0x01, 0x01,
				0x01, 0x02, 0x0A, 0x04, 0x0E, 0x06, 0x01, 0x7F, 0xF7);
	}

	private static void assertSupportedModes(int index, Map<Integer, FirmataPin> pins, Mode... expected) {
		assertThat(pins.get(index).getSupportedMode()).containsExactly(expected);
	}

	@Test
	void canReadAnalogPinViaFirmataProto() throws IOException {
		byte command = ANALOG_MESSAGE;
		byte pinNumber = 15;
		byte valueLow = 127;
		byte valueHigh = 42;

		AnalogPin pin = analogPin(pinNumber);
		givenMessage(command |= pinNumber, valueLow, valueHigh);
		whenMessageIsProcessed();
		thenMessageIs(pin, valueHigh << 7 | valueLow);
	}

	@Test
	public void canReadDigitalPinViaFirmataProto() throws IOException {
		byte command = DIGITAL_MESSAGE;
		byte port = 4;
		byte valueLow = 0b0100101;
		byte valueHigh = 0b1;
		givenMessage(command |= port, valueLow, valueHigh);
		whenMessageIsProcessed();

		// TODO PF onDigitalMappingReceive
		AtomicInteger pin = new AtomicInteger((byte) pow(2, port + 1));
		assertMessage(messages, Arrays.asList(true, false, true, false, false, true, false, true).stream()
				.collect(toMap(__ -> digitalPin(pin.getAndIncrement()), identity())));
	}

	// -------------------------------------------------------------------------

	@Test
	void canSetDigitalPin() {
		DigitalPin pin = digitalPin(13);
		try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
			softly.assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, true))).containsExactly(0x91, 0x20, 0x00);
			softly.assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, false))).containsExactly(0x91, 0x00,
					0x00);
		}
	}

	@Test
	void canSetPwmPin() throws IOException {
		givenMessage(capabilitiesQuery());
		whenMessageIsProcessed();

		AnalogPin pin = analogPin(9);
		assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, 42))).containsExactly(0xF4, 0x09, 0x03, /**/ 0xE9,
				0x2A, 0x00);
		assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, 255))).containsExactly(0xE9, 0x7F, 0x01);
		assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, 42))).containsExactly(0xE9, 0x2A, 0x00);
	}

	@Test
	void canSetPwmPinViaExtendedMessage() throws IOException {
		givenMessage(capabilitiesQuery());
		whenMessageIsProcessed();

		AnalogPin pin = analogPin(9);
		assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, 42))).containsExactly(0xF4, 0x09, 0x03, /**/ 0xE9,
				0x2A, 0x00);
		assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, 16 * 1024 - 1))).containsExactly(0xE9, 0x7F, 0x7F);
		assertThat(sut.toDevice(toDeviceMessagePinStateChange(pin, 16 * 1024))).containsExactly(0xF0, 0x6F, 0x09, 0x00,
				0x00, 0x01, 0x00, 0xF7);
	}

	// -------------------------------------------------------------------------

	/**
	 * <a href=
	 * "https://github.com/firmata/protocol/blob/master/proposals/tone-proposal.md">Not
	 * part of Firmata! This is a proposal</a>
	 */
	@Test
	void canSendToneAndNoTone() {
		AnalogPin pin = analogPin(1);
		try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
			Tone tone = Tone.forPin(pin).withHertz(234).withDuration(5, SECONDS);
			softly.assertThat(sut.toDevice(toDeviceMessageTone(tone))) //
					.containsExactly(0xF0, 0x5F, 0x00, pin.pinNum(), 0x6A, 0x01, 0x08, 0x27, 0xF7);
			softly.assertThat(sut.toDevice(toDeviceMessageNoTone(pin))) //
					.containsExactly(0xF0, 0x5F, 0x01, pin.pinNum(), 0xF7);
		}
	}

	// -------------------------------------------------------------------------

	@Test
	void canEnableDisableAnalogListening() {
		byte pinNumber = 2;
		Pin pin = analogPin(pinNumber);
		try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
			softly.assertThat(sut.toDevice(new DefaultToDeviceMessageStartListening(pin))).containsExactly(0xC2, 0x01);
			softly.assertThat(sut.toDevice(new DefaultToDeviceMessageStopListening(pin))).containsExactly(0xC2, 0x00);
		}
	}

	@Test
	void canEnableDisableDigitalListening() {
		byte pinNumber = 12;
		Pin pin = digitalPin(pinNumber);
		try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
			softly.assertThat(sut.toDevice(new DefaultToDeviceMessageStartListening(pin))).containsExactly(0xD1, 0x01);
			softly.assertThat(sut.toDevice(new DefaultToDeviceMessageStopListening(pin))).containsExactly(0xD1, 0x00);
		}
	}

	// -------------------------------------------------------------------------

	private void givenMessage(int... ints) {
		givenMessage(toBytes(ints));
	}

	private static byte[] toBytes(int... ints) {
		byte[] bytes = new byte[ints.length];
		for (int i = 0; i < ints.length; i++) {
			bytes[i] = (byte) ints[i];
		}
		return bytes;
	}

	private void givenMessage(byte... bytes) {
		this.bytes = bytes;
	}

	private void thenMessageIs(Pin pin, Object value) {
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessagePinStateChanged.class, e -> {
			try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
				softly.assertThat(e.getPin()).isEqualTo(pin);
				softly.assertThat(e.getValue()).isEqualTo(value);
			}
		});
	}

	private void assertMessage(List<FromDeviceMessage> messages, Map<Pin, Object> expectedStates) {
		assertThat(messages).hasSameSizeAs(expectedStates.entrySet());
		for (FromDeviceMessage message : messages) {
			FromDeviceMessagePinStateChanged pinStateChanged = (FromDeviceMessagePinStateChanged) message;
			Object object = expectedStates.get(pinStateChanged.getPin());
			try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
				softly.assertThat(object).withFailMessage("No expected state for pin " + pinStateChanged.getPin())
						.isNotNull();
				softly.assertThat(pinStateChanged.getValue()).withFailMessage("Pin " + pinStateChanged.getPin())
						.isEqualTo(object);
			}
		}
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
