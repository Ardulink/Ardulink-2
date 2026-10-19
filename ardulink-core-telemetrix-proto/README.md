### ardulink-core-telemetrix-proto

Contains the Telemetrix protocol (named "Telemetrix"). Use this if you have modules that use the legacy [arduino-telemetrix.ino](https://github.com/MrYsLab/telemetrix/tree/master/arduino_sketches/arduino-telemetrix) sketch as their communication protocol.

#### Wire Format Documentation

This module implements the legacy Telemetrix protocol. It is a plain byte protocol: every message starts with a command/report id followed by a fixed number of argument bytes. There is no support for key presses, tones or custom messages.

##### Host to device

| Feature | Id | Format |
|---------|----|--------|
| Loop back | `0` | `0 <byte>` |
| Set pin mode | `1` | `1 <pin> <mode> <reporting> <unused>` |
| Digital write | `2` | `2 <pin> <value>` |
| Analog (PWM) write | `3` | `3 <pin> <value>` |
| Get firmware version | `5` | `5` (used for Ping) |
| Are you there | `6` | `6` |

Pin modes are the AVR constants plus Telemetrix extensions: `AT_INPUT=0`, `AT_OUTPUT=1`, `AT_INPUT_PULLUP=2`, `AT_ANALOG=3`.

Listening is enabled/disabled through the reporting byte of `SET_PIN_MODE`:

| Feature | Format |
|---------|--------|
| Start listening (analog) | `1 <pin> 3 1 0` |
| Start listening (digital) | `1 <pin> 0 1 0` |
| Stop listening (analog) | `1 <pin> 3 0 0` |
| Stop listening (digital) | `1 <pin> 0 0 0` |

The legacy `MODIFY_REPORTING` (`4`) command is intentionally not used: its declared command length of one byte does not match the three bytes the reference client sends, which leaves bytes in the serial buffer and desynchronizes the command stream.

`SET_PIN_MODE` declares four data bytes in the sketch, but only `<pin>`, `<mode>` and `<reporting>` are read; the fourth is unused and sent as `0`. It must not be omitted: the sketch calls `set_pin_mode` after each of the four data bytes and, on the fourth iteration, reads whatever byte is buffered next. Sending only three data bytes makes that read consume the following command's id byte and desynchronizes the stream.


##### Device to host

| Feature | Id | Arguments | Ardulink mapping |
|---------|----|-----------|------------------|
| Digital report | `2` | `pin, value` | `digitalPin(pin)`, `value != 0` |
| Analog report | `3` | `pin, high, low` | `analogPin(pin)`, `(high << 8) \| low` |
| Firmware report | `5` | `major, minor` | `FromDeviceMessageInfo` |
| I am here | `6` | `arduinoId` | `FromDeviceMessageInfo` (parameter `arduinoId`) |
| Loop back | `0` | `byte` | ignored |
| Debug print | `99` | `id, high, low` | ignored |
