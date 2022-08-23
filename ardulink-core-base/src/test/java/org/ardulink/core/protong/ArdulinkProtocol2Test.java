package org.ardulink.core.protong;

import static java.lang.Integer.parseInt;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessors.parse;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessageCustom;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.api.FromDeviceMessageReady;
import org.ardulink.core.messages.api.FromDeviceMessageReply;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.util.Joiner;
import org.ardulink.util.Lists;
import org.ardulink.util.MapBuilder;
import org.ardulink.util.anno.LapsedWith;
import org.junit.Test;

public class ArdulinkProtocol2Test {

	private List<FromDeviceMessage> messages;
	private String message;

	@Test
	public void canReadAnalogPinViaArdulinkProto() throws IOException {
		int pin = 42;
		int value = 21;
		givenMessage(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value));
		whenMessageIsProcessed();
		thenMessageIs(analogPin(pin), value);
	}

	@Test
	public void canReadDigitalPinViaArdulinkProto() throws IOException {
		int pin = 42;
		boolean value = true;
		givenMessage(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(value));
		whenMessageIsProcessed();
		thenMessageIs(digitalPin(pin), value);
	}

	@Test
	public void canReadRplyViaArdulinkProto() throws IOException {
		givenMessage("alp://rply/ok?id=1&UniqueID=456-2342-2342&ciao=boo");
		whenMessageIsProcessed();
		assertThat(messages.size(), is(1));
		FromDeviceMessageReply replyMessage = (FromDeviceMessageReply) messages.get(0);
		assertThat(replyMessage.getParameters(), is(MapBuilder.<String, Object>newMapBuilder()
				.put("UniqueID", "456-2342-2342").put("ciao", "boo").build()));
	}

	@Test
	public void canReadReadyViaArdulinkProto() throws IOException {
		givenMessage("alp://ready/");
		whenMessageIsProcessed();
		assertThat(messages.size(), is(1));
		assertThat(messages.get(0), instanceOf(FromDeviceMessageReady.class));
	}

	@Test
	public void doesRecoverFromMisformedContent() throws IOException {
		givenMessages("alp://XXXXXreadyXXXXX/", "alp://ready/");
		whenMessageIsProcessed();
		assertThat(messages.size(), is(1));
		assertThat(messages.get(0), instanceOf(FromDeviceMessageReady.class));
	}

	@Test
	public void ardulinkProtocol2ReceiveCustomEvent() throws IOException {
		givenMessage("alp://cevnt/foo=bar/some=42");
		whenMessageIsProcessed();
		assertThat(messages.size(), is(1));
		assertThat(messages.get(0), instanceOf(FromDeviceMessageCustom.class));
		FromDeviceMessageCustom customMessage = (FromDeviceMessageCustom) messages.get(0);
		assertThat(customMessage.getMessage(), is("foo=bar/some=42"));
	}

	@Test
	public void ardulinkProtocol2ReceiveRply() throws IOException {
		long id = 1;
		Map<String, Object> params = MapBuilder.<String, Object>newMapBuilder().put("UniqueID", "ABC-1234-5678")
				.put("boo", "ciao").build();
		givenMessage("alp://rply/ok?id=" + id + "&" + Joiner.on("&").withKeyValueSeparator("=").join(params));
		whenMessageIsProcessed();
		assertThat(messages.size(), is(1));
		assertThat(messages.get(0), instanceOf(FromDeviceMessageReply.class));
		FromDeviceMessageReply replyMessage = (FromDeviceMessageReply) messages.get(0);
		assertThat(replyMessage.isOk(), is(true));
		assertThat(replyMessage.getId(), is(id));
		assertThat(replyMessage.getParameters(), is(params));
	}

	private void givenMessage(String in) {
		this.message = lf(in);
	}

	private void givenMessages(String in1, String in2) {
		this.message = lf(in1) + lf(in2);
	}

	private void thenMessageIs(Pin pin, Object value) {
		assertThat(messages.size(), is(1));
		FromDeviceMessagePinStateChanged pinStateChanged = (FromDeviceMessagePinStateChanged) messages.get(0);
		assertThat(pinStateChanged.getPin(), is(pin));
		assertThat(pinStateChanged.getValue(), is(value));
	}

	@LapsedWith(module = "JDK7", value = "binary literals")
	private static byte binary(String string) {
		return (byte) parseInt(string, 2);
	}

	private void whenMessageIsProcessed() throws IOException {
		whenMessageIsProcessed(new ArdulinkProtocol2());
	}

	private void whenMessageIsProcessed(Protocol protocol) throws IOException {
		ByteStreamProcessor processor = byteStreamProcessor(protocol);
		// read in "random" (three) junks
		messages = Lists.newArrayList();
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
