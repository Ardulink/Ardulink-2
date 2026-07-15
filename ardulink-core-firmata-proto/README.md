### ardulink-core-firmata-proto

Contains the Firmata protocol (named "Firmata"). Use this if you have modules that use Firmata as their communication protocol.

#### Wire Format Documentation

This module implements the Firmata protocol with extensions for Ardulink-specific features.

##### Standard Firmata Commands Used

| Feature | Command | Description |
|---------|---------|-------------|
| Digital read/write | `0x90`-`0x9F` | `DIGITAL_MESSAGE` with port number |
| Analog read/write | `0xE0`-`0xEF` | `ANALOG_MESSAGE` with pin number |
| Report analog | `0xC0`-`0xCF` | `REPORT_ANALOG` to enable/disable analog reporting |
| Report digital | `0xD0`-`0xDF` | `REPORT_DIGITAL` to enable/disable digital reporting |
| Set pin mode | `0xF4` | `SET_PIN_MODE` |
| Set digital pin | `0xF5` | `SET_DIGITAL_PIN_VALUE` |
| Extended analog | `0x6F` | `EXTENDED_ANALOG` for values > 1023 |
| Report version | `0xF9` | Used for Ping (FR-1) |

##### Custom Extensions (sysex)

All custom extensions use Firmata sysex messages (`0xF0` ... `0xF7`).

| Feature | Sysex Command | Format |
|---------|---------------|--------|
| Custom message (FR-3) | `STRING_DATA` (`0x71`) | `0xF0 0x71 <char_lsb> <char_msb>... 0xF7` |
| Key press (FR-2) | `STRING_DATA` (`0x71`) | `0xF0 0x71 <encoded "chr<keychar>cod<keycode>loc<keylocation>mod<keymodifiers>mex<keymodifiersex>"> 0xF7` |
| Reply (FR-4) | `STRING_DATA` (`0x71`) | `0xF0 0x71 <encoded "rply|ok|<id>|<params>"> 0xF7` |
| Listening state (FR-6) | `STRING_DATA` (`0x71`) | `0xF0 0x71 <encoded "listen|start|analog|<pin>"> 0xF7` |
| Tone | `0x5F` (proposal) | `0xF0 0x5F 0x00 <pin> <freq_lsb> <freq_msb> <dur_lsb> <dur_msb> 0xF7` |
| NoTone | `0x5F` (proposal) | `0xF0 0x5F 0x01 <pin> 0xF7` |
| Capabilities query | `0x6B` | `0xF0 0x6B 0xF7` (auto-sent after firmware message) |

##### Firmata String Encoding

Firmata uses 7-bit encoding for strings in sysex messages. Each character is split into:
- LSB: `char & 0x7F` (lower 7 bits)
- MSB: `(char >> 7) & 0x7F` (upper bits, padded to 7)

##### Reply Format (FR-4)

Reply messages use the format: `rply|ok|<id>|<param1>=<value1>|<param2>=<value2>`

Or for failures: `rply|ko|<id>|<error_param>=<error_value>`

The `<id>` corresponds to the message ID assigned by the sender.

##### Message ID Support (FR-8)

When a message ID is attached to an outgoing message (via `MessageIdHolder`), the ID is appended to the string payload with a pipe separator: `<payload>|<messageId>`.

##### Capabilities Auto-Query (FR-7)

After receiving the first firmware message (`REPORT_FIRMWARE`), the BSP automatically sends a capabilities query (`0xF0 0x6B 0xF7`) to the device. This query is sent only once per session.

##### Tone/NoTone

The tone and noTone commands use a non-standard proposal from the Firmata protocol. This requires a Firmata firmware variant with tone support. See [Firmata Tone Proposal](https://github.com/firmata/protocol/blob/master/proposals/tone-proposal.md) for details.
