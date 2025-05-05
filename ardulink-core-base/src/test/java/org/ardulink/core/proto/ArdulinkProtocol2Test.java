package org.ardulink.core.proto;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessors.parse;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.util.Maps.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
		Entry<String, String> entry1 = entry("UniqueID", "123-45678-9012");
		Entry<String, String> entry2 = entry("foo", "bar");
		givenMessage(format("alp://rply/ok?id=1&%s=%s&%s=%s", entry1.getKey(), entry1.getValue(), entry2.getKey(),
				entry2.getValue()));
		whenMessageIsProcessed();
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessageReply.class,
				m -> assertThat(m.getParameters()).contains(entry1, entry2));
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
		Map<String, Object> params = Map.of("key1", "value1", "key2", "value2");
		givenMessage("alp://rply/ok?id=" + id + "&" + Joiner.on("&").withKeyValueSeparator("=").join(params));
		whenMessageIsProcessed();
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessageReply.class, m -> {
			assertSoftly(s -> {
				s.assertThat(m.isOk()).isTrue();
				s.assertThat(m.getId()).isEqualTo(id);
				// expected in same order defined
				s.assertThat(m.getParameters()).containsExactlyInAnyOrderEntriesOf(params);
			});
		});
	}

	@ParameterizedTest
	@MethodSource("alpAred12")
	void bufferOverflow(String message) throws IOException {
		givenMessage(message);
		assertThatRuntimeException() //
				.isThrownBy(this::whenMessageIsProcessed) //
				.withMessageContaining("buffer");
	}

	@ParameterizedTest
	@MethodSource("alp")
	void noBufferOverflowButNoMessageProcessed(String message) throws IOException {
		givenMessage(message);
		whenMessageIsProcessed();
		assertThat(messages).isEmpty();
	}

	static Stream<String> alpAred12() {
		return shortenFromTo("alp://ared/1/2?id=", "alp://");
	}

	static Stream<String> alp() {
		return shortenFromTo("alp:/", "");
	}

	private static Stream<String> shortenFromTo(String from, String to) {
		return Stream.iterate(from, not(to::equals), s -> s.substring(0, s.length() - 1))
				.map(s -> s.concat("x".repeat(65)));
	}

	private void givenMessage(String in) {
		this.message = lf(in);
	}

	private void givenMessages(String in1, String in2) {
		this.message = lf(in1) + lf(in2);
	}

	private void thenMessageIs(Pin pin, Object value) {
		assertThat(messages).singleElement().isInstanceOfSatisfying(FromDeviceMessagePinStateChanged.class, m -> {
			assertSoftly(s -> {
				s.assertThat(m.getPin()).isEqualTo(pin);
				s.assertThat(m.getValue()).isEqualTo(value);
			});
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
