### ardulink-core-telemetrix-proto

Contains the Telemetrix protocol (named "Telemetrix"). Use this if you have modules that use the legacy [arduino-telemetrix.ino](https://github.com/MrYsLab/telemetrix/tree/master/arduino_sketches/arduino-telemetrix) sketch as their communication protocol.

#### Wire Format Documentation

This module implements the legacy Telemetrix protocol. It is a plain byte protocol: every message starts with a command/report id followed by a fixed number of argument bytes. There is no support for key presses, tones or custom messages.

##### Host to device

| Feature | Id | Format |
|---------|----|--------|
| Loop back | `0` | `0 <byte>` |
| Set pin mode | `1` | `1 <pin> <mode> <reporting>` |
| Digital write | `2` | `2 <pin> <value>` |
| Analog (PWM) write | `3` | `3 <pin> <value>` |
| Get firmware version | `5` | `5` (used for Ping) |
| Are you there | `6` | `6` |

Pin modes are the AVR constants plus Telemetrix extensions: `AT_INPUT=0`, `AT_OUTPUT=1`, `AT_INPUT_PULLUP=2`, `AT_ANALOG=3`.

Listening is enabled/disabled through the reporting byte of `SET_PIN_MODE`:

| Feature | Format |
|---------|--------|
| Start listening (analog) | `1 <pin> 3 1` |
| Start listening (digital) | `1 <pin> 0 1` |
| Stop listening (analog) | `1 <pin> 3 0` |
| Stop listening (digital) | `1 <pin> 0 0` |

The legacy `MODIFY_REPORTING` (`4`) command is intentionally not used: its declared command length of one byte does not match the three bytes the reference client sends, which leaves bytes in the serial buffer and desynchronizes the command stream.

##### Device to host

| Feature | Id | Arguments | Ardulink mapping |
|---------|----|-----------|------------------|
| Digital report | `2` | `pin, value` | `digitalPin(pin)`, `value != 0` |
| Analog report | `3` | `pin, high, low` | `analogPin(pin)`, `(high << 8) \| low` |
| Firmware report | `5` | `major, minor` | `FromDeviceMessageInfo` |
| I am here | `6` | `arduinoId` | `FromDeviceMessageInfo` |
| Loop back | `0` | `byte` | ignored |
| Debug print | `99` | `id, high, low` | ignored |
