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
import org.ardulink.util.Lists;
import org.ardulink.util.MapBuilder;
import org.ardulink.util.anno.LapsedWith;
import org.junit.Test;

public class FirmataProtocolTest {

	private static final class Collector implements FromDeviceListener {

		private final List<FromDeviceMessage> messages = Lists.newArrayList();

		@Override
		public void handle(FromDeviceMessage message) {
			messages.add(message);
		}

		public List<FromDeviceMessage> getMessages() {
			return messages;
		}
	}

	@Test
	public void canReadAnalogPinViaFirmataProto() throws IOException {
		byte command = ANALOG_MESSAGE;
		byte pin = 15;
		byte valueLow = 127;
		byte valueHigh = 42;
		byte[] bytes = new byte[] { command |= pin, valueLow, valueHigh };
		List<FromDeviceMessage> messages = process(new FirmataProtocol(), bytes);
		assertMessage(messages, analogPin(pin), valueHigh << 7 | valueLow);
	}

	@Test
	public void canReadDigitalPinViaFirmataProto() throws IOException {
		byte command = DIGITAL_MESSAGE;
		byte port = 4;
		byte valueLow = binary("010" + "0101");
		byte valueHigh = binary("1");
		byte[] bytes = new byte[] { command |= port, valueLow, valueHigh };
		List<FromDeviceMessage> messages = process(new FirmataProtocol(), bytes);

		byte pin = (byte) pow(2, port + 1);
		assertMessage(messages, MapBuilder.<Pin, Object>newMapBuilder() //
				.put(digitalPin(pin++), true).put(digitalPin(pin++), false) //
				.put(digitalPin(pin++), true).put(digitalPin(pin++), false) //
				.put(digitalPin(pin++), false).put(digitalPin(pin++), true) //
				.put(digitalPin(pin++), false).put(digitalPin(pin++), true) //
				.build());
	}

	private void assertMessage(List<FromDeviceMessage> messages, Pin pin, Object value) {
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

	@LapsedWith(module = "JDK7", value = "binary literals")
	private static byte binary(String string) {
		return (byte) parseInt(string, 2);
	}

	private List<FromDeviceMessage> process(ProtocolNG protocol, byte[] bytes) throws IOException {
		InputStream stream = new ByteArrayInputStream(bytes);
		Collector listener = new Collector();
		ByteStreamProcessor processor = byteStreamProcessor(protocol);
		processor.addListener(listener);

		processor.process(read(stream, 2));
		processor.process(read(stream, 5));
		processor.process(read(stream, stream.available()));
		return listener.getMessages();
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
