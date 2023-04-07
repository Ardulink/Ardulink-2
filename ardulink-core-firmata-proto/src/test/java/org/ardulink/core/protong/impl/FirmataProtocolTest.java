package org.ardulink.core.protong.impl;

import static java.lang.Math.pow;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessors.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.firmata4j.firmata.parser.FirmataToken.ANALOG_MESSAGE;
import static org.firmata4j.firmata.parser.FirmataToken.DIGITAL_MESSAGE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.FirmataProtocol;
import org.ardulink.util.Lists;
import org.ardulink.util.MapBuilder;
import org.junit.jupiter.api.Test;

class FirmataProtocolTest {

	private byte[] bytes;
	private List<FromDeviceMessage> messages;

	@Test
	void canReadAnalogPinViaFirmataProto() throws IOException {
		byte command = ANALOG_MESSAGE;
		byte pin = 15;
		byte valueLow = 127;
		byte valueHigh = 42;
		givenMessage(command |= pin, valueLow, valueHigh);
		whenMessageIsProcessed();
		thenMessageIs(analogPin(pin), valueHigh << 7 | valueLow);
	}

	@Test
	void canReadDigitalPinViaFirmataProto() throws IOException {
		byte command = DIGITAL_MESSAGE;
		byte port = 4;
		byte valueLow = 0b0100101;
		byte valueHigh = 0b1;
		givenMessage(command |= port, valueLow, valueHigh);
		whenMessageIsProcessed();
		byte pin = (byte) pow(2, port + 1);
		assertMessage(messages, MapBuilder.<Pin, Object>newMapBuilder() //
				.put(digitalPin(pin++), true).put(digitalPin(pin++), false) //
				.put(digitalPin(pin++), true).put(digitalPin(pin++), false) //
				.put(digitalPin(pin++), false).put(digitalPin(pin++), true) //
				.put(digitalPin(pin++), false).put(digitalPin(pin++), true) //
				.build());
	}

	private void givenMessage(byte... bytes) {
		this.bytes = bytes;
	}

	private void thenMessageIs(Pin pin, Object value) {
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessagePinStateChanged.class, m -> {
			assertThat(m.getPin()).isEqualTo(pin);
			assertThat(m.getValue()).isEqualTo(value);
		});
	}

	private void assertMessage(List<FromDeviceMessage> messages, Map<Pin, Object> expectedStates) {
		assertThat(messages).hasSize(expectedStates.size()).allSatisfy(m -> {
			assertThat(m).isInstanceOfSatisfying(FromDeviceMessagePinStateChanged.class, f -> {
				Object object = expectedStates.get(f.getPin());
				assertThat(object).isNotNull().withFailMessage("No expected state for pin " + f.getPin());
				assertThat(f.getValue()).isEqualTo(object).withFailMessage("Pin " + f.getPin());
			});
		});
	}

	private void whenMessageIsProcessed() throws IOException {
		process(new FirmataProtocol(), bytes);
	}

	private void process(FirmataProtocol protocol, byte[] bytes) throws IOException {
		ByteStreamProcessor processor = byteStreamProcessor(protocol);
		// read in "random" (two) junks
		messages = Lists.newArrayList();
		InputStream stream = new ByteArrayInputStream(bytes);
		messages.addAll(parse(processor, read(stream, 2)));
		messages.addAll(parse(processor, read(stream, stream.available())));
	}

	private static ByteStreamProcessor byteStreamProcessor(Protocol protocol) {
		return protocol.newByteStreamProcessor();
	}

	private static byte[] read(InputStream stream, int length) throws IOException {
		byte[] bytes = new byte[length];
		stream.read(bytes);
		return bytes;
	}

}
