# PRD: Complete Firmata Protocol Parity with Ardulink-2

## 1. Overview

The `ardulink-core-firmata-proto` module provides a Firmata protocol implementation (`FirmataProtocol`) that maps Firmata messages onto Ardulink's protocol-agnostic `ByteStreamProcessor` API. Several `ByteStreamProcessor.toDevice(...)` methods currently throw `UnsupportedOperationException("not yet implemented")`, and several incoming Firmata message types are not parsed. This PRD specifies the work required so that every feature available through the Ardulink-2 protocol is also available through the Firmata protocol.

## 2. Current State

### 2.1 Fully working in Firmata

| Direction | Message | Status |
|-----------|---------|--------|
| Outgoing | `ToDeviceMessagePinStateChange` (digital) | OK |
| Outgoing | `ToDeviceMessagePinStateChange` (analog/PWM) | OK (standard + extended sysex for values > 1023) |
| Outgoing | `ToDeviceMessageStartListening` | OK |
| Outgoing | `ToDeviceMessageStopListening` | OK |
| Outgoing | `ToDeviceMessageTone` | OK (Firmata tone proposal, not standard) |
| Outgoing | `ToDeviceMessageNoTone` | OK (Firmata tone proposal, not standard) |
| Incoming | Firmware message (`FromDeviceMessageInfo`) | OK |
| Incoming | Pin capabilities | OK |
| Incoming | Analog pin state change | OK |
| Incoming | Digital pin state change | OK |

### 2.2 Not implemented (throw `UnsupportedOperationException`)

| Direction | Message | Location |
|-----------|---------|----------|
| Outgoing | `ToDeviceMessagePing` | `FirmataProtocol.java:259` |
| Outgoing | `ToDeviceMessageKeyPress` | `FirmataProtocol.java:343` |
| Outgoing | `ToDeviceMessageCustom` | `FirmataProtocol.java:372` |

### 2.3 Incoming messages not parsed

| From-Device Message | Used by |
|---------------------|---------|
| `FromDeviceMessageReply` (ok/ko with message ID) | `QosLink`, `ConnectionBasedLink` reply handling |
| `FromDeviceMessageCustom` | `ConnectionBasedLink` custom event dispatch |
| `FromDeviceChangeListeningState` | `ConnectionBasedLink` (not currently dispatched but defined in Ardulink-2 parser) |

### 2.4 Architectural gaps

1. **No outbound capability from ByteStreamProcessor.** The BSP is purely a parser for incoming bytes. There is no mechanism for it to trigger outbound messages (e.g., sending a capabilities query after receiving the firmware message). This is documented in the `@ExpectedToFail` test `doesRequestsCapabilitiesOnlyOnFirstFirmwareStartupResponse` (`FirmataProtocolTest.java:87`).
2. **Preview feature gate.** Firmata is gated behind system property `protocol.firmata.enabled` (`PreviewFeature.java:35`). For GA parity this gate should be removed or the feature promoted.
3. **No message-ID tracking.** Ardulink-2 attaches message IDs to outgoing messages and parses them from replies. Firmata has no equivalent concept; `MessageIdHolder` is never used.

## 3. Requirements

### FR-1: Implement Ping

**What:** `toDevice(ToDeviceMessagePing)` must produce valid Firmata bytes.

**How:** Firmata has no standard ping command. Use a no-op that elicits a response from the device. Recommended approach: send a `REPORT_VERSION` message (`0xF9`) which causes StandardFirmata to reply with its version. Alternatively, send a zero-length sysex or a string message. The implementation must produce bytes that StandardFirmata (or a compatible sketch) will respond to, so `ConnectionBasedLink.waitForArduinoToBoot()` and `QosLink` can use ping for liveness detection.

**Acceptance:**
- `toDevice(ToDeviceMessagePing)` returns a non-empty `byte[]` without throwing.
- Unit test verifies the returned bytes match the chosen Firmata command.
- Integration: `ConnectionBasedLink.waitForArduinoToBoot()` succeeds with a Firmata-connected device.

### FR-2: Implement Key Press

**What:** `toDevice(ToDeviceMessageKeyPress)` must translate key press events into Firmata messages.

**How:** Firmata has no standard keyboard/HID command. Two options:
- **Option A (recommended):** Encode the key press fields (`keychar`, `keycode`, `keylocation`, `keymodifiers`, `keymodifiersex`) into a Firmata sysex message using a custom command byte (e.g., in the user-defined sysex range `0x00`-`0x0F` or a dedicated proposal byte). The Arduino sketch must be extended to decode this sysex and emit HID keystrokes.
- **Option B:** Use a `STRING_MESSAGE` (sysex `0x71`) encoding the key fields as a delimited string.

Document the chosen wire format in the module README. The Ardulink sketch for Firmata must be updated to handle the chosen format.

**Acceptance:**
- `toDevice(ToDeviceMessageKeyPress)` returns a valid `byte[]` without throwing.
- Round-trip unit test: encode then verify byte structure.
- README documents the wire format and required Arduino sketch support.

### FR-3: Implement Custom Messages

**What:** `toDevice(ToDeviceMessageCustom)` must send arbitrary string payloads to the device.

**How:** Use Firmata's `STRING_MESSAGE` (sysex command `0x71`). Encode each string from `getMessages()` as a Firmata string sysex message (null-terminated, 7-bit safe). If multiple strings are provided, send them as separate `STRING_MESSAGE` sysex frames or concatenate with a delimiter.

**Acceptance:**
- `toDevice(ToDeviceMessageCustom)` returns valid Firmata `STRING_MESSAGE` sysex bytes.
- Unit test verifies correct encoding including 7-bit framing and null termination.
- Corresponding incoming `STRING_MESSAGE` parsing produces `FromDeviceMessageCustom` (see FR-6).

### FR-4: Parse Incoming Reply Messages

**What:** The Firmata BSP must recognize reply/acknowledgement messages from the device and emit `FromDeviceMessageReply`.

**How:** Firmata has no built-in reply mechanism. Define a convention:
- After processing a command, the Arduino sketch sends back a `STRING_MESSAGE` with a structured payload, e.g., `rply|ok|<id>` or `rply|ko|<id>`.
- Alternatively, define a dedicated sysex command for replies.

The BSP must parse these incoming messages and fire `DefaultFromDeviceMessageReply` events with the correct `isOk()` and `getId()` values.

**Acceptance:**
- BSP recognizes reply messages and fires `FromDeviceMessageReply`.
- `QosLink` works end-to-end with Firmata (waitForResponse returns).
- Unit tests cover ok and ko replies with and without extra parameters.

### FR-5: Parse Incoming Custom Messages

**What:** The Firmata BSP must parse incoming `STRING_MESSAGE` sysex frames (that are not replies) and emit `FromDeviceMessageCustom`.

**How:** When a `STRING_MESSAGE` is received and it does not match the reply format (FR-4), emit a `DefaultFromDeviceMessageCustom` with the string content.

**Acceptance:**
- Incoming `STRING_MESSAGE` sysex produces `FromDeviceMessageCustom`.
- Unit test verifies correct parsing.
- `ConnectionBasedLink` dispatches custom events to `CustomListener`s.

### FR-6: Parse Incoming Change Listening State (optional/low priority)

**What:** If the device confirms a start/stop listening command with an explicit message, parse it as `FromDeviceChangeListeningState`.

**How:** Define a convention (e.g., a specific `STRING_MESSAGE` format or a dedicated sysex byte) and parse it in the BSP.

**Acceptance:**
- BSP emits `FromDeviceChangeListeningState` when the device sends the corresponding message.
- Unit test verifies parsing.

### FR-7: Enable Outbound Messages from ByteStreamProcessor

**What:** The BSP must be able to trigger outbound messages (e.g., send a capabilities query after receiving the firmware message).

**How:** Introduce an outbound callback interface on `ByteStreamProcessor` (or a sub-interface like `OutboundCapable`). When the firmware message is received, the BSP invokes the callback to send the capabilities query (`0xF0 0x6B 0xF7`). `ConnectionBasedLink` or `StreamConnection` must wire the BSP's outbound callback to the connection's `write()` method.

**Acceptance:**
- On receiving the firmware message, a capabilities query is automatically sent to the device.
- The `@ExpectedToFail` test `doesRequestsCapabilitiesOnlyOnFirstFirmwareStartupResponse` passes (remove `@ExpectedToFail`).
- Capabilities query is sent only once (not on every firmware message).

### FR-8: Message ID Support

**What:** Outgoing Firmata messages should carry message IDs when `MessageIdHolder` is set, and incoming replies must carry the matching ID.

**How:** Since Firmata has no native message ID concept, embed the message ID in the outbound message payload (e.g., as a prefix in `STRING_MESSAGE` or as extra sysex data bytes). The reply parser (FR-4) extracts this ID.

**Acceptance:**
- `addMessageId(...)` works with Firmata messages without error.
- Round-trip: send a message with ID, receive reply with same ID.
- `QosLink` correctly correlates replies.

### FR-9: Remove Preview Feature Gate

**What:** Promote Firmata from preview to GA.

**How:**
- Remove the `isFirmataProtocolFeatureEnabled()` check in `FirmataProtocol.isActive()` (or change the default to `true`).
- Update `PreviewFeature` javadoc.
- Update tests that rely on `@SetSystemProperty` / `@ClearSystemProperty` for the Firmata gate.

**Acceptance:**
- `FirmataProtocol.isActive()` returns `true` without any system property.
- `Protocols.tryProtoByName("Firmata")` returns the protocol by default.
- All existing tests pass.

## 4. Non-Functional Requirements

- **NFR-1:** All new code must have unit tests with at least the same coverage as the existing `FirmataProtocolTest`.
- **NFR-2:** Wire formats for FR-2, FR-3, FR-4, FR-6, FR-8 must be documented in `ardulink-core-firmata-proto/README.md`.
- **NFR-3:** No changes to the `ByteStreamProcessor` API that break existing Ardulink-2 protocol implementation.
- **NFR-4:** The Firmata tone/noTone implementation uses a non-standard proposal. Document this clearly and note that it requires a Firmata firmware variant with tone support.

## 5. Out of Scope

- Changes to the Ardulink-2 protocol itself.
- Changes to `Link`, `ConnectionBasedLink`, or `QosLink` (unless required to wire FR-7).
- I2C, OneWire, Stepper, Encoder, or other extended Firmata modes (these are Firmata-specific and have no Ardulink-2 equivalent).
- Arduino sketch development (the PRD covers the Java side; sketch changes are a separate deliverable but must be documented).

## 6. Priority Order

1. **FR-1 (Ping)** - unblocks `waitForArduinoToBoot()` and basic liveness.
2. **FR-7 (Outbound from BSP)** - unblocks automatic capabilities query.
3. **FR-3 (Custom outgoing) + FR-5 (Custom incoming)** - enables arbitrary device communication.
4. **FR-4 (Reply parsing) + FR-8 (Message IDs)** - enables `QosLink`.
5. **FR-2 (Key press)** - requires sketch-side support; lower urgency.
6. **FR-6 (Change listening state)** - low priority, optional.
7. **FR-9 (Remove preview gate)** - do last, after all features are stable.

## 7. Key Source References

| File | Relevance |
|------|-----------|
| `ardulink-core-firmata-proto/src/main/java/org/ardulink/core/proto/firmata/FirmataProtocol.java` | Main implementation; `notYetImplemented()` at lines 259, 343, 372 |
| `ardulink-core-firmata-proto/src/test/java/org/ardulink/core/proto/firmata/FirmataProtocolTest.java` | Existing tests; `@ExpectedToFail` at line 87 |
| `ardulink-core-base/src/main/java/org/ardulink/core/proto/ardulink/ArdulinkProtocol2.java` | Reference implementation for all message types |
| `ardulink-core-base/src/main/java/org/ardulink/core/proto/api/bytestreamproccesors/ByteStreamProcessor.java` | Interface contract |
| `ardulink-core-base/src/main/java/org/ardulink/core/ConnectionBasedLink.java` | Shows how `FromDeviceMessage` types are dispatched |
| `ardulink-core-base/src/main/java/org/ardulink/core/qos/QosLink.java` | Requires `FromDeviceMessageReply` support |
| `ardulink-core-base/src/main/java/org/ardulink/core/featureflags/PreviewFeature.java` | Feature gate to remove (FR-9) |
