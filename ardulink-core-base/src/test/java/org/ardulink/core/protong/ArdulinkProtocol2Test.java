package org.ardulink.core.protong;

import static java.lang.Integer.parseInt;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
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

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.messages.api.FromDeviceMessageReady;
import org.ardulink.core.messages.api.FromDeviceMessageReply;
import org.ardulink.core.proto.api.ProtocolNG;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor.FromDeviceListener;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.util.Lists;
import org.ardulink.util.MapBuilder;
import org.ardulink.util.anno.LapsedWith;
import org.junit.Test;

public class ArdulinkProtocol2Test {

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

	private final MessageCollector messageCollector = new MessageCollector();

	@Test
	public void canReadAnalogPinViaArdulinkProto() throws IOException {
		int pin = 42;
		int value = 21;
		String message = alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value) + "\n";
		process(new ArdulinkProtocol2(), message);
		assertMessage(messageCollector.getMessages(), analogPin(pin), (Object) value);
	}

	@Test
	public void canReadDigitalPinViaArdulinkProto() throws IOException {
		int pin = 42;
		boolean value = true;
		String message = alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(value) + "\n";
		process(new ArdulinkProtocol2(), message);
		assertMessage(messageCollector.getMessages(), digitalPin(pin), (Object) value);
	}

	@Test
	public void canReadRplyViaArdulinkProto() throws IOException {
		String message = "alp://rply/ok?id=1&UniqueID=456-2342-2342&ciao=boo" + "\n";
		process(new ArdulinkProtocol2(), message);
		assertThat(messageCollector.getMessages().size(), is(1));
		FromDeviceMessageReply replyMessage = (FromDeviceMessageReply) messageCollector.getMessages().get(0);
		assertThat(replyMessage.getParameters(), is((Object) MapBuilder.<String, String>newMapBuilder()
				.put("UniqueID", "456-2342-2342").put("ciao", "boo").build()));
	}
	
	@Test
	public void canReadReadyViaArdulinkProto() throws IOException {
		String message = "alp://ready/" + "\n";
		process(new ArdulinkProtocol2(), message);
		assertThat(messageCollector.getMessages().size(), is(1));
		assertThat(messageCollector.getMessages().get(0), instanceOf(FromDeviceMessageReady.class));
	}

	private void assertMessage(List<FromDeviceMessage> messages, Pin pin, Object value) {
		assertThat(messages.size(), is(1));
		FromDeviceMessagePinStateChanged pinStateChanged = (FromDeviceMessagePinStateChanged) messages.get(0);
		assertThat(pinStateChanged.getPin(), is(pin));
		assertThat(pinStateChanged.getValue(), is(value));
	}

	@LapsedWith(module = "JDK7", value = "binary literals")
	private static byte binary(String string) {
		return (byte) parseInt(string, 2);
	}

	private void process(ProtocolNG protocol, String bytes) throws IOException {
		InputStream stream = new ByteArrayInputStream(bytes.getBytes());
		ByteStreamProcessor processor = byteStreamProcessor(protocol);
		processor.addListener(messageCollector);

		processor.process(read(stream, 2));
		processor.process(read(stream, 5));
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
