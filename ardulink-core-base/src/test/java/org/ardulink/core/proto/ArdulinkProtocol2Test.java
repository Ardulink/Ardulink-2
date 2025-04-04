package org.ardulink.core.proto;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessors.parse;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessageCustom;
import org.ardulink.core.messages.api.FromDeviceMessageInfo;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.api.FromDeviceMessageReply;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.ardulink.ArdulinkProtocol2;
import org.ardulink.util.Joiner;
import org.junit.jupiter.api.Test;

class ArdulinkProtocol2Test {

	private List<FromDeviceMessage> messages;
	private String message;

	@Test
	void canReadAnalogPinViaArdulinkProto() throws IOException {
		int pin = 42;
		int value = 21;
		givenMessage(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value));
		whenMessageIsProcessed();
		thenMessageIs(analogPin(pin), value);
	}

	@Test
	void canReadDigitalPinViaArdulinkProto() throws IOException {
		int pin = 42;
		boolean value = true;
		givenMessage(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(value));
		whenMessageIsProcessed();
		thenMessageIs(digitalPin(pin), value);
	}

	@Test
	void canReadRplyViaArdulinkProto() throws IOException {
		givenMessage("alp://rply/ok?id=1&UniqueID=456-2342-2342&ciao=boo");
		whenMessageIsProcessed();
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessageReply.class,
				m -> assertThat(m.getParameters())
						.containsExactlyInAnyOrderEntriesOf(Map.of("UniqueID", "456-2342-2342", "ciao", "boo")));
	}

	@Test
	void canReadInfoViaArdulinkProto() throws IOException {
		givenMessage("alp://info/");
		whenMessageIsProcessed();
		assertThat(messages).singleElement().isInstanceOf(FromDeviceMessageInfo.class);
	}

	@Test
	void doesRecoverFromMisformedContent() throws IOException {
		givenMessages("xxx", "alp://info/");
		whenMessageIsProcessed();
		assertThat(messages).singleElement().isInstanceOf(FromDeviceMessageInfo.class);
	}

	@Test
	void ardulinkProtocol2ReceiveCustomEvent() throws IOException {
		givenMessage("alp://cevnt/foo=bar/some=42");
		whenMessageIsProcessed();
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessageCustom.class,
				m -> assertThat(m.getMessage()).isEqualTo("foo=bar/some=42"));
	}

	@Test
	void ardulinkProtocol2ReceiveRply() throws IOException {
		long id = 1;
		Map<String, Object> params = Map.of("key1", "value1","key2", "value2");
		givenMessage("alp://rply/ok?id=" + id + "&" + Joiner.on("&").withKeyValueSeparator("=").join(params));
		whenMessageIsProcessed();
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessageReply.class, m -> {
			assertThat(m.isOk()).isTrue();
			assertThat(m.getId()).isEqualTo(id);
			// expected in same order defined
			assertThat(m.getParameters()).containsExactlyInAnyOrderEntriesOf(params);
		});
	}

	private void givenMessage(String in) {
		this.message = lf(in);
	}

	private void givenMessages(String in1, String in2) {
		this.message = lf(in1) + lf(in2);
	}

	private void thenMessageIs(Pin pin, Object value) {
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessagePinStateChanged.class, m -> {
			assertThat(m.getPin()).isEqualTo(pin);
			assertThat(m.getValue()).isEqualTo(value);
		});
	}

	private void whenMessageIsProcessed() throws IOException {
		whenMessageIsProcessed(new ArdulinkProtocol2());
	}

	private void whenMessageIsProcessed(Protocol protocol) throws IOException {
		ByteStreamProcessor processor = byteStreamProcessor(protocol);
		// read in "random" (three) junks
		messages = new ArrayList<>();
		InputStream stream = new ByteArrayInputStream(message.getBytes());
		messages.addAll(parse(processor, read(stream, 2)));
		messages.addAll(parse(processor, read(stream, 5)));
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

	private static String lf(String string) {
		return string + "\n";
	}

}
