/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.core.proto.telemetrix;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessageInfo.fromDeviceMessageInfo;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessagePinStateChanged.fromDeviceMessagePinStateChanged;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.ToDeviceMessageCustom;
import org.ardulink.core.messages.api.ToDeviceMessageKeyPress;
import org.ardulink.core.messages.api.ToDeviceMessageNoTone;
import org.ardulink.core.messages.api.ToDeviceMessagePinStateChange;
import org.ardulink.core.messages.api.ToDeviceMessagePing;
import org.ardulink.core.messages.api.ToDeviceMessageStartListening;
import org.ardulink.core.messages.api.ToDeviceMessageStopListening;
import org.ardulink.core.messages.api.ToDeviceMessageTone;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.AbstractByteStreamProcessor;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.util.ByteArray;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 * Implementation of the protocol used by the legacy
 * <a href="https://github.com/MrYsLab/telemetrix">arduino-telemetrix.ino</a>
 * sketch. The sketch and the protocol are quite minimal: apart from setting pin
 * modes and reading/writing pins there is no support for key presses, tones or
 * custom messages.
 *
 * Reporting is controlled via the reporting byte of <code>SET_PIN_MODE</code>
 * instead of <code>MODIFY_REPORTING</code>. The latter declares a command
 * length of only one byte while the reference client sends three, which leaves
 * the remaining bytes in the serial buffer and desynchronizes the command
 * stream.
 */
public class TelemetrixProtocol implements Protocol {

	public static final String NAME = "Telemetrix";

	// commands received by the device
	private enum Command {

		SERIAL_LOOP_BACK(0), //
		SET_PIN_MODE(1), //
		DIGITAL_WRITE(2), //
		ANALOG_WRITE(3), //
		GET_FIRMWARE_VERSION(5), //
		ARE_U_THERE(6);

		private final byte code;

		Command(int code) {
			this.code = (byte) code;
		}

		byte code() {
			return code;
		}

	}

	// reports sent by the device. Their ids reuse the command ids (e.g. the
	// digital report shares the digital write id), the debug report has no
	// command counterpart. argCount is the number of argument bytes following
	// the report id.
	private enum Report {

		LOOP_BACK(Command.SERIAL_LOOP_BACK, 1), //
		DIGITAL(Command.DIGITAL_WRITE, 2), //
		ANALOG(Command.ANALOG_WRITE, 3), //
		FIRMWARE(Command.GET_FIRMWARE_VERSION, 2), //
		I_AM_HERE(Command.ARE_U_THERE, 1), //
		DEBUG_PRINT(99, 3);

		private final byte code;
		private final int argCount;

		private static final Map<Byte, Report> byCode = EnumSet.allOf(Report.class).stream() //
				.collect(toUnmodifiableMap(Report::code, identity()));

		Report(Command command, int argCount) {
			this(command.code(), argCount);
		}

		Report(int code, int argCount) {
			this.code = (byte) code;
			this.argCount = argCount;
		}

		byte code() {
			return code;
		}

		int argCount() {
			return argCount;
		}

		static Report from(byte code) {
			return byCode.get(code);
		}

	}

	// pin modes (see Arduino.h and arduino-telemetrix.ino)
	private static final byte AT_INPUT = 0;
	private static final byte AT_OUTPUT = 1;
	private static final byte AT_ANALOG = 3;

	private static final byte REPORTING_OFF = 0;
	private static final byte REPORTING_ON = 1;

	@Override
	public String getName() {
		return NAME;
	}

	private static class TelemetrixByteStreamProcessor extends AbstractByteStreamProcessor {

		private final Map<Integer, Byte> outputPins = new ConcurrentHashMap<>();

		// parser state, the report currently being read, null while waiting
		// for a report id
		private Report report;
		private final ByteArray reportArgs = new ByteArray();

		@Override
		public void process(byte b) {
			if (report == null) {
				startReport(b);
				return;
			}
			reportArgs.append(b);
			if (reportArgs.length() == report.argCount()) {
				dispatch(report, reportArgs.copy());
				report = null;
			}
		}

		private void startReport(byte reportId) {
			Report report = Report.from(reportId);
			if (report == null) {
				return;
			}
			if (report.argCount() == 0) {
				dispatch(report, new byte[0]);
				return;
			}
			reportArgs.clear();
			this.report = report;
		}

		private void dispatch(Report report, byte[] args) {
			switch (report) {
			case DIGITAL:
				fireEvent(fromDeviceMessagePinStateChanged(digitalPin(unsigned(args[0])), args[1] != 0));
				break;
			case ANALOG:
				fireEvent(fromDeviceMessagePinStateChanged(analogPin(unsigned(args[0])),
						(unsigned(args[1]) << 8) | unsigned(args[2])));
				break;
			case FIRMWARE:
			case I_AM_HERE:
				fireEvent(fromDeviceMessageInfo());
				break;
			default:
				// loop back and debug reports do not map to ardulink events
				break;
			}
		}

		private static int unsigned(byte b) {
			return b & 0xff;
		}

		@Override
		public byte[] toDevice(ToDeviceMessagePing ping) {
			return new byte[] { Command.GET_FIRMWARE_VERSION.code() };
		}

		@Override
		public byte[] toDevice(ToDeviceMessageStartListening startListening) {
			return setPinMode(startListening.getPin(), REPORTING_ON);
		}

		@Override
		public byte[] toDevice(ToDeviceMessageStopListening stopListening) {
			return setPinMode(stopListening.getPin(), REPORTING_OFF);
		}

		private byte[] setPinMode(Pin pin, byte reporting) {
			int pinNum = pin.pinNum();
			// the pin is configured as an input (or analog input), so a later
			// write has to re-send the output mode again
			outputPins.remove(pinNum);
			byte mode = pin.is(ANALOG) ? AT_ANALOG : AT_INPUT;
			// SET_PIN_MODE expects four argument bytes (see command_lengths in
			// arduino-telemetrix.ino); the fourth is unused but must be sent,
			// otherwise the sketch consumes the next command's id byte
			return new byte[] { Command.SET_PIN_MODE.code(), (byte) pinNum, mode, reporting, REPORTING_OFF };
		}

		@Override
		public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
			ByteArray message = new ByteArray();
			Pin pin = pinStateChange.getPin();
			int pinNum = pin.pinNum();
			ensureOutputMode(message, pinNum);
			if (pin.is(ANALOG)) {
				message.append(new byte[] { Command.ANALOG_WRITE.code(), (byte) pinNum,
						(byte) ((Integer) pinStateChange.getValue()).intValue() });
			} else {
				message.append(new byte[] { Command.DIGITAL_WRITE.code(), (byte) pinNum,
						booleanToByte(TRUE.equals(pinStateChange.getValue())) });
			}
			return message.copy();
		}

		private void ensureOutputMode(ByteArray message, int pinNum) {
			if (outputPins.putIfAbsent(pinNum, AT_OUTPUT) == null) {
				message.append(new byte[] { Command.SET_PIN_MODE.code(), (byte) pinNum, AT_OUTPUT, REPORTING_OFF,
						REPORTING_OFF });
			}
		}

		private static byte booleanToByte(boolean value) {
			return value ? (byte) 1 : (byte) 0;
		}

		@Override
		public byte[] toDevice(ToDeviceMessageKeyPress keyPress) {
			throw new UnsupportedOperationException(format("%s does not support key press events", NAME));
		}

		@Override
		public byte[] toDevice(ToDeviceMessageTone tone) {
			throw new UnsupportedOperationException(format("%s does not support tones", NAME));
		}

		@Override
		public byte[] toDevice(ToDeviceMessageNoTone noTone) {
			throw new UnsupportedOperationException(format("%s does not support tones", NAME));
		}

		@Override
		public byte[] toDevice(ToDeviceMessageCustom custom) {
			throw new UnsupportedOperationException(format("%s does not support custom messages", NAME));
		}

	}

	@Override
	public ByteStreamProcessor newByteStreamProcessor() {
		return new TelemetrixByteStreamProcessor();
	}

}
