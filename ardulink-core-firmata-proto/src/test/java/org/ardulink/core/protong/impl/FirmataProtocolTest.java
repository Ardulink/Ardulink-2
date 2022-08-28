package org.ardulink.core.protong.impl;

import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessors.parse;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.ANALOG_INPUT;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.DIGITAL_INPUT;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.DIGITAL_OUTPUT;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.I2C;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.INPUT_PULLUP;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.PWM;
import static org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode.SERVO;
import static org.ardulink.util.Bytes.hexStringToBytes;
import static org.ardulink.util.anno.LapsedWith.JDK7;
import static org.firmata4j.firmata.parser.FirmataToken.ANALOG_MESSAGE;
import static org.firmata4j.firmata.parser.FirmataToken.DIGITAL_MESSAGE;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.api.FromDeviceMessageReady;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageNoTone;
import org.ardulink.core.messages.impl.DefaultToDeviceMessagePinStateChange;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageStartListening;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageStopListening;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageTone;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.FirmataProtocol;
import org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin;
import org.ardulink.core.proto.impl.FirmataProtocol.FirmataPin.Mode;
import org.ardulink.util.Bytes;
import org.ardulink.util.Joiner;
import org.ardulink.util.Lists;
import org.ardulink.util.MapBuilder;
import org.ardulink.util.anno.LapsedWith;
import org.junit.Ignore;
import org.junit.Test;

public class FirmataProtocolTest {

	private ByteStreamProcessor sut = new FirmataProtocol().newByteStreamProcessor();

	private byte[] bytes;
	private List<FromDeviceMessage> messages;

	@Test
	public void canReadFirmwareStartupInfo() throws IOException {
		givenMessage((byte) 0xF0, (byte) 0x79, (byte) 0x01, (byte) 0x02, (byte) 0x41, (byte) 0x0, (byte) 0xF7);
		whenMessageIsProcessed();
		assertThat(messages.size(), is(1));
		assertThat(messages.get(0), instanceOf(FromDeviceMessageReady.class));
	}

	@Test
	public void canReadCapabilities() throws IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {

		byte[] capabilities = hexStringToBytes((""
				+ "F0 6C 7F 7F 00 01 0B 01 01 01 04 0E 7F 00 01 0B 01 01 01 03 08 04 0E 7F 00 01 0B 01 01 01 04 0E 7F "
				+ "00 01 0B 01 01 01 03 08 04 0E 7F 00 01 0B 01 01 01 03 08 04 0E 7F 00 01 0B 01 01 01 04 0E 7F 00 01 "
				+ "0B 01 01 01 04 0E 7F 00 01 0B 01 01 01 03 08 04 0E 7F 00 01 0B 01 01 01 03 08 04 0E 7F 00 01 0B 01 "
				+ "01 01 03 08 04 0E 7F 00 01 0B 01 01 01 04 0E 7F 00 01 0B 01 01 01 04 0E 7F 00 01 0B 01 01 01 02 0A "
				+ "04 0E 7F 00 01 0B 01 01 01 02 0A 04 0E 7F 00 01 0B 01 01 01 02 0A 04 0E 7F 00 01 0B 01 01 01 02 0A "
				+ "04 0E 7F 00 01 0B 01 01 01 02 0A 04 0E 06 01 7F 00 01 0B 01 01 01 02 0A 04 0E 06 01 7F F7")
				.replace(" ", ""));

		givenMessage(capabilities);
		whenMessageIsProcessed();

		// TODO verify via behavior check
		Field declaredField = sut.getClass().getDeclaredField("pins");
		declaredField.setAccessible(true);

		@SuppressWarnings("unchecked")
		Map<Integer, FirmataPin> pins = (Map<Integer, FirmataPin>) declaredField.get(sut);
		assertThat(pins.size(), is(20));
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

	@Test
	public void queriesCapabilitiesAfterConnecting() throws Exception {
		fail();
	}

	private void assertSupportedModes(int index, Map<Integer, FirmataPin> pins, Mode... expected) {
		Set<Mode> set = expected.length == 0 ? EnumSet.noneOf(Mode.class) : EnumSet.copyOf(Arrays.asList(expected));
		assertThat(pins.get(index).getSupportedMode(), is(set));
	}

	@Test
	@Ignore("not yet implemented")
	public void canReadAnalogPinViaFirmataProto() throws IOException {
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
		byte valueLow = binary("010" + "0101");
		byte valueHigh = binary("1");
		givenMessage(command |= port, valueLow, valueHigh);
		whenMessageIsProcessed();
		byte pin = (byte) pow(2, port + 1);

		// TODO PF onDigitalMappingReceive

		assertMessage(messages, MapBuilder.<Pin, Object>newMapBuilder() //
				.put(digitalPin(pin++), true).put(digitalPin(pin++), false) //
				.put(digitalPin(pin++), true).put(digitalPin(pin++), false) //
				.put(digitalPin(pin++), false).put(digitalPin(pin++), true) //
				.put(digitalPin(pin++), false).put(digitalPin(pin++), true) //
				.build());
	}

	// -------------------------------------------------------------------------

	@Test
	public void canSetDigitalPin() {
		DigitalPin pin = digitalPin(12);
		assertThat(bytesToHexString(sut.toDevice(new DefaultToDeviceMessagePinStateChange(pin, true))), is("92 04 00"));
		assertThat(bytesToHexString(sut.toDevice(new DefaultToDeviceMessagePinStateChange(pin, false))),
				is("92 00 00"));
	}

	@Test
	public void canSetAnalogPin() {
		int value = 42;
		AnalogPin pin = analogPin(10);
		DefaultToDeviceMessagePinStateChange toDeviceMessage = new DefaultToDeviceMessagePinStateChange(pin, value);
		assertThat(bytesToHexString(sut.toDevice(toDeviceMessage)), is("E2 2A 00"));
		// TODO Verify the EXTENDED_ANALOG (for higher pin numbers/values)
	}

	// -------------------------------------------------------------------------

	/**
	 * <a href=
	 * "https://github.com/firmata/protocol/blob/master/proposals/tone-proposal.md">Not
	 * part of Firmata! This is a proposal</a>
	 */
	@Test
	public void canSendTone() {
		byte pinNumber = 1;
		ToDeviceMessageTone toDeviceMessage = new DefaultToDeviceMessageTone(
				Tone.forPin(analogPin(pinNumber)).withHertz(234).withDuration(5, SECONDS));
		assertThat(sut.toDevice(toDeviceMessage), is(new byte[] { (byte) 0xF0, (byte) 0x5F, (byte) 0x00, pinNumber,
				(byte) 0x6A, (byte) 0x01, (byte) 0x08, (byte) 0x27, (byte) 0xF7 }));
	}

	/**
	 * <a href=
	 * "https://github.com/firmata/protocol/blob/master/proposals/tone-proposal.md">Not
	 * part of Firmata! This is a proposal</a>
	 */
	@Test
	public void canSendNoTone() {
		byte pinNumber = 1;
		ToDeviceMessageNoTone toDeviceMessage = new DefaultToDeviceMessageNoTone(analogPin(pinNumber));
		assertThat(sut.toDevice(toDeviceMessage),
				is(new byte[] { (byte) 0xF0, (byte) 0x5F, (byte) 0x01, pinNumber, (byte) 0xF7 }));
	}

	// -------------------------------------------------------------------------

	@Test
	public void canEnableDisableAnalogListening() {
		byte pinNumber = 2;
		Pin pin = analogPin(pinNumber);
		assertThat(bytesToHexString(sut.toDevice(new DefaultToDeviceMessageStartListening(pin))), is("C0 01"));
		assertThat(bytesToHexString(sut.toDevice(new DefaultToDeviceMessageStopListening(pin))), is("C0 00"));
	}

	@Test
	public void canEnableDisableDigitalListening() {
		byte pinNumber = 12;
		Pin pin = digitalPin(pinNumber);
		assertThat(bytesToHexString(sut.toDevice(new DefaultToDeviceMessageStartListening(pin))), is("D2 01"));
		assertThat(bytesToHexString(sut.toDevice(new DefaultToDeviceMessageStopListening(pin))), is("D2 00"));
	}

	// -------------------------------------------------------------------------

	private void givenMessage(byte... bytes) {
		this.bytes = bytes;
	}

	private void thenMessageIs(Pin pin, Object value) {
		assertThat(messages.size(), is(1));
		FromDeviceMessagePinStateChanged pinStateChanged = (FromDeviceMessagePinStateChanged) messages.get(0);
		assertThat(pinStateChanged.getPin(), is(pin));
		assertThat(pinStateChanged.getValue(), is(value));
	}

	private void assertMessage(List<FromDeviceMessage> messages, Map<Pin, Object> expectedStates) {
		assertThat(messages.size(), is(expectedStates.size()));
		for (FromDeviceMessage message : messages) {
			FromDeviceMessagePinStateChanged pinStateChanged = (FromDeviceMessagePinStateChanged) message;
			Object object = expectedStates.get(pinStateChanged.getPin());
			assertThat("No expected state for pin " + pinStateChanged.getPin(), object, notNullValue());
			assertThat("Pin " + pinStateChanged.getPin(), pinStateChanged.getValue(), is(object));
		}
	}

	@LapsedWith(module = JDK7, value = "binary literals")
	private static byte binary(String string) {
		return (byte) parseInt(string, 2);
	}

	private static String bytesToHexString(byte[] bytes) {
		return Joiner.on(" ").join(Bytes.bytesToHex(bytes));
	}

	private void whenMessageIsProcessed() throws IOException {
		process(bytes);
	}

	private void process(byte[] bytes) throws IOException {
		// read in "random" (two) junks
		messages = Lists.newArrayList();
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
