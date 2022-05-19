package org.ardulink.core.protong.impl;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.util.Integers.tryParse;
import static org.firmata4j.firmata.parser.FirmataEventType.ANALOG_MESSAGE_RESPONSE;
import static org.firmata4j.firmata.parser.FirmataEventType.DIGITAL_MESSAGE_RESPONSE;
import static org.firmata4j.firmata.parser.FirmataEventType.PIN_ID;
import static org.firmata4j.firmata.parser.FirmataEventType.PIN_VALUE;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.messages.impl.DefaultFromDeviceMessagePinStateChanged;
import org.ardulink.core.proto.api.ProtocolNG;
import org.ardulink.core.proto.api.bytestreamproccesors.AbstractByteStreamProcessor;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.firmata4j.Consumer;
import org.firmata4j.firmata.parser.WaitingForMessageState;
import org.firmata4j.fsm.Event;
import org.firmata4j.fsm.FiniteStateMachine;

public class FirmataProtocol implements ProtocolNG {

	@Override
	public String getName() {
		return "Firmata";
	}

	private static class FirmataByteStreamProcessor extends AbstractByteStreamProcessor {

		private final FiniteStateMachine delegate = new FiniteStateMachine(WaitingForMessageState.class);

		public FirmataByteStreamProcessor() {
			Consumer<Event> adapter = adapter();
			delegate.addHandler(ANALOG_MESSAGE_RESPONSE, adapter);
			delegate.addHandler(DIGITAL_MESSAGE_RESPONSE, adapter);
		}

		private Consumer<Event> adapter() {
			return new Consumer<Event>() {

				@Override
				public void accept(Event evt) {
					fireEvent(convert(evt));
				}

				private FromDeviceMessage convert(Event event) {
					String pinNumber = String.valueOf(event.getBodyItem(PIN_ID));
					Pin pin = createPin(event.getType(), tryParse(pinNumber).getOrThrow("Cannot parse %s", pinNumber));
					Object value = event.getBodyItem(PIN_VALUE);
					return new DefaultFromDeviceMessagePinStateChanged(pin, convertValue(pin, value));
				}

				private Object convertValue(Pin pin, Object value) {
					return pin.is(DIGITAL) ? value.equals(1) ? true : false : value;
				}

				private Pin createPin(String type, Integer pin) {
					if (ANALOG_MESSAGE_RESPONSE.equals(type)) {
						return analogPin(pin);
					} else if (DIGITAL_MESSAGE_RESPONSE.equals(type)) {
						return digitalPin(pin);
					} else {
						throw new IllegalStateException("Unknown pin type " + type);
					}
				}
			};
		}

		@Override
		public void process(byte[] read) {
			delegate.process(read);
		}

	}

	@Override
	public ByteStreamProcessor newByteStreamProcessor() {
		return new FirmataByteStreamProcessor();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageStartListening startListening) {
		throw notYetImplemented();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageStopListening stopListening) {
		throw notYetImplemented();
	}

	@Override
	public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
		throw notYetImplemented();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageKeyPress keyPress) {
		throw notYetImplemented();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageTone tone) {
		throw notYetImplemented();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageNoTone noTone) {
		throw notYetImplemented();
	}

	@Override
	public byte[] toDevice(ToDeviceMessageCustom custom) {
		throw notYetImplemented();
	}

	private static UnsupportedOperationException notYetImplemented() {
		return new UnsupportedOperationException("not yet implemented");
	}

}