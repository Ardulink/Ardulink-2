package org.ardulink.core.protong.impl;

import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.firmata4j.firmata.parser.FirmataToken.ANALOG_MESSAGE;
import static org.firmata4j.firmata.parser.FirmataToken.DIGITAL_MESSAGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.proto.api.ProtocolNG;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor.FromDeviceListener;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor.RawListener;
import org.ardulink.util.ByteArray;
import org.ardulink.util.Lists;
import org.ardulink.util.MapBuilder;
import org.ardulink.util.anno.LapsedWith;
import org.hamcrest.Matcher;
import org.junit.Test;

public class FirmataProtocolTest {

	private static final class MessageCollector implements FromDeviceListener {

		private final List<FromDeviceMessage> messages = Lists.newArrayList();

		@Override
		public void handle(FromDeviceMessage message) {
			messages.add(message);
		}

		public List<FromDeviceMessage> getMessages() {
			return messages;
		}
	}

	private static final class RawCollector implements RawListener {

		private final ByteArray bytes = new ByteArray();

		@Override
		public void handle(byte[] fromDevice) {
			bytes.append(fromDevice, fromDevice.length);
		}

		public byte[] getBytes() {
			return bytes.copy();
		}

	}

	private final MessageCollector messageCollector = new MessageCollector();
	private final RawCollector rawCollector = new RawCollector();

	@Test
	public void canReadAnalogPinViaFirmataProto() throws IOException {
		byte command = ANALOG_MESSAGE;
		byte pin = 15;
		byte valueLow = 127;
		byte valueHigh = 42;
		byte[] bytes = new byte[] { command |= pin, valueLow, valueHigh };
		process(new FirmataProtocol(), bytes);
		assertMessage(messageCollector.getMessages(), analogPin(pin), valueHigh << 7 | valueLow);
		assertRaw(is(bytes));
	}

	@Test
	public void canReadDigitalPinViaFirmataProto() throws IOException {
		byte command = DIGITAL_MESSAGE;
		byte port = 4;
		byte valueLow = binary("010" + "0101");
		byte valueHigh = binary("1");
		byte[] bytes = new byte[] { command |= port, valueLow, valueHigh };
		process(new FirmataProtocol(), bytes);
		byte pin = (byte) pow(2, port + 1);
		assertMessage(messageCollector.getMessages(), MapBuilder.<Pin, Object>newMapBuilder() //
				.put(digitalPin(pin++), true).put(digitalPin(pin++), false) //
				.put(digitalPin(pin++), true).put(digitalPin(pin++), false) //
				.put(digitalPin(pin++), false).put(digitalPin(pin++), true) //
				.put(digitalPin(pin++), false).put(digitalPin(pin++), true) //
				.build());
		assertRaw(is(bytes));
	}

	private void assertMessage(List<FromDeviceMessage> messages, Pin pin, Object value) {
		assertThat(messages.size(), is(1));
		FromDeviceMessagePinStateChanged pinStateChanged = (FromDeviceMessagePinStateChanged) messages.get(0);
		assertThat(pinStateChanged.getPin(), is(pin));
		assertThat(pinStateChanged.getValue(), is(value));
	}

	private void assertRaw(Matcher<byte[]> matcher) {
		assertThat(rawCollector.getBytes(), matcher);
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

	@LapsedWith(module = "JDK7", value = "binary literals")
	private static byte binary(String string) {
		return (byte) parseInt(string, 2);
	}

	private void process(ProtocolNG protocol, byte[] bytes) throws IOException {
		InputStream stream = new ByteArrayInputStream(bytes);
		ByteStreamProcessor processor = byteStreamProcessor(protocol);
		processor.addListener(messageCollector);
		processor.addListener(rawCollector);

		processor.process(read(stream, 2));
		processor.process(read(stream, stream.available()));
	}

	private static ByteStreamProcessor byteStreamProcessor(ProtocolNG protocol) {
		return protocol.newByteStreamProcessor();
	}

	private static byte[] read(InputStream stream, int length) throws IOException {
		byte[] bytes = new byte[length];
		stream.read(bytes);
		return bytes;
	}

}
