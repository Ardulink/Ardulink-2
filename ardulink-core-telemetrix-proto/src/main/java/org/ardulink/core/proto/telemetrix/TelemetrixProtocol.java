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
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessageInfo.fromDeviceMessageInfo;
import static org.ardulink.core.messages.impl.DefaultFromDeviceMessagePinStateChanged.fromDeviceMessagePinStateChanged;

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
 * sketch. The sketch and the protocol are quite minimal: apart from setting
 * pin modes and reading/writing pins there is no support for key presses,
 * tones or custom messages.
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
	private static final byte SERIAL_LOOP_BACK = 0;
	private static final byte SET_PIN_MODE = 1;
	private static final byte DIGITAL_WRITE = 2;
	private static final byte ANALOG_WRITE = 3;
	private static final byte GET_FIRMWARE_VERSION = 5;
	private static final byte ARE_U_THERE = 6;

	// reports sent by the device
	private static final byte DIGITAL_REPORT = DIGITAL_WRITE;
	private static final byte ANALOG_REPORT = ANALOG_WRITE;
	private static final byte FIRMWARE_REPORT = GET_FIRMWARE_VERSION;
	private static final byte I_AM_HERE = ARE_U_THERE;
	private static final byte DEBUG_PRINT = 99;

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

		// parser state, -1 means "waiting for a report id"
		private int reportId = -1;
		private final ByteArray reportArgs = new ByteArray();

		@Override
		public void process(byte b) {
			if (reportId < 0) {
				startReport(b);
				return;
			}
			reportArgs.append(b);
			if (reportArgs.length() == reportArgCount((byte) reportId)) {
				dispatch((byte) reportId, reportArgs.copy());
				reportArgs.clear();
				reportId = -1;
			}
		}

		private void startReport(byte reportId) {
			int argCount = reportArgCount(reportId);
			if (argCount < 0) {
				return;
			}
			if (argCount == 0) {
				dispatch(reportId, new byte[0]);
			} else {
				reportArgs.clear();
				this.reportId = reportId;
			}
		}

		private static int reportArgCount(byte reportId) {
			switch (reportId) {
			case SERIAL_LOOP_BACK:
				return 1;
			case DIGITAL_REPORT:
				return 2;
			case ANALOG_REPORT:
				return 3;
			case FIRMWARE_REPORT:
				return 2;
			case I_AM_HERE:
				return 1;
			case DEBUG_PRINT:
				return 3;
			default:
				return -1;
			}
		}

		private void dispatch(byte reportId, byte[] args) {
			switch (reportId) {
			case DIGITAL_REPORT:
				fireEvent(fromDeviceMessagePinStateChanged(digitalPin(unsigned(args[0])), args[1] != 0));
				break;
			case ANALOG_REPORT:
				fireEvent(fromDeviceMessagePinStateChanged(analogPin(unsigned(args[0])),
						(unsigned(args[1]) << 8) | unsigned(args[2])));
				break;
			case FIRMWARE_REPORT:
			case I_AM_HERE:
				fireEvent(fromDeviceMessageInfo());
				break;
			default:
				// SERIAL_LOOP_BACK and DEBUG_PRINT do not map to ardulink events
				break;
			}
		}

		private static int unsigned(byte b) {
			return b & 0xff;
		}

		@Override
		public byte[] toDevice(ToDeviceMessagePing ping) {
			return new byte[] { GET_FIRMWARE_VERSION };
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
			byte mode = pin.is(ANALOG) ? AT_ANALOG : AT_INPUT;
			return new byte[] { SET_PIN_MODE, (byte) pin.pinNum(), mode, reporting };
		}

		@Override
		public byte[] toDevice(ToDeviceMessagePinStateChange pinStateChange) {
			ByteArray message = new ByteArray();
			Pin pin = pinStateChange.getPin();
			int pinNum = pin.pinNum();
			ensureOutputMode(message, pinNum);
			if (pin.is(ANALOG)) {
				message.append(new byte[] { ANALOG_WRITE, (byte) pinNum, (byte) ((Integer) pinStateChange.getValue()).intValue() });
			} else {
				message.append(new byte[] { DIGITAL_WRITE, (byte) pinNum, booleanToByte(TRUE.equals(pinStateChange.getValue())) });
			}
			return message.copy();
		}

		private void ensureOutputMode(ByteArray message, int pinNum) {
			if (outputPins.putIfAbsent(pinNum, AT_OUTPUT) == null) {
				message.append(new byte[] { SET_PIN_MODE, (byte) pinNum, AT_OUTPUT, REPORTING_OFF, REPORTING_OFF });
			}
		}

		private static byte booleanToByte(boolean value) {
			return value ? (byte) 1 : (byte) 0;
		}

		@Override
		public byte[] toDevice(ToDeviceMessageKeyPress keyPress) {
			throw new UnsupportedOperationException("Telemetrix does not support key press events");
		}

		@Override
		public byte[] toDevice(ToDeviceMessageTone tone) {
			throw new UnsupportedOperationException("Telemetrix does not support tones");
		}

		@Override
		public byte[] toDevice(ToDeviceMessageNoTone noTone) {
			throw new UnsupportedOperationException("Telemetrix does not support tones");
		}

		@Override
		public byte[] toDevice(ToDeviceMessageCustom custom) {
			throw new UnsupportedOperationException("Telemetrix does not support custom messages");
		}

	}

	@Override
	public ByteStreamProcessor newByteStreamProcessor() {
		return new TelemetrixByteStreamProcessor();
	}

}
