# Migration plan: one-shot pin queries in ardulink-rest

## Context

Firmware PR [Ardulink/Firmware#27](https://github.com/Ardulink/Firmware/pull/27) adds
one-shot pin query commands to the Ardulink protocol:

| Command | Reply |
| --- | --- |
| `alp://qrya/<pin>?id=<id>` | `alp://rply/ok?id=<id>&value=<analog value>` |
| `alp://qryd/<pin>?id=<id>` | `alp://rply/ok?id=<id>&value=<0\|1>` |

The commands read the pin once, without starting a subscription. They configure the pin
as `INPUT` while reading. The `id` is required: without it the firmware sends no reply.

This addresses [Ardulink-2#1185](https://github.com/Ardulink/Ardulink-2/issues/1185):
REST clients should be able to read a pin without subscribing to it.

## Current behaviour

A REST `GET /pin/analog/{pin}` or `GET /pin/digital/{pin}` currently does **not** start
or stop listening. It waits for the next pin-state event, which only arrives when the pin
is already being listened to. In practice clients use:

1. `PATCH /pin/{type}/{pin}` with body `listen=true`
2. `GET /pin/{type}/{pin}`
3. optionally `PATCH ...` with body `listen=false`

This document calls that the **legacy listen/read** strategy. The new strategy is called
**one-shot query**.

## Target behaviour

- `legacy` keeps the current behaviour unchanged. `PATCH` continues to control listening.
- `query` makes `GET /pin/analog/{pin}` send `qrya` and `GET /pin/digital/{pin}` send
  `qryd`. The response body is the value from the `rply` message.
- `query` does not call `startListening` or `stopListening`.
- `PATCH` remains available in both modes for clients that need continuous updates; it is
  not removed by this migration.

## Manual feature flag

Start with a manual switch. Automatic detection is a later phase.

- Add a flag to `org.ardulink.core.featureflags.PreviewFeature`:

  ```text
  public static final String PIN_READ_QUERY_ENABLED_PROPERTY_FEATURE = "pin.read.query.enabled";
  public static boolean isPinReadQueryEnabled() { return isPropertySet(PIN_READ_QUERY_ENABLED_PROPERTY_FEATURE); }
  ```

- When the property is **not set**, the behaviour is `legacy`. Existing deployments and
  existing tests keep their current behaviour without any configuration change.
- When the property is set to any non-empty value, the behaviour is `query`.
- `query` requires firmware containing Firmware PR #27. With older firmware the command is
  unknown, no reply is sent, and the REST read fails with a timeout/HTTP 500.
- The flag applies to the Ardulink/ALP protocol. Firmata and Telemetrix links keep using
  the legacy path; forcing `query` for them must produce an explicit error rather than an
  undefined failure.

### Example

```bash
java -Dpin.read.query.enabled=true -jar ardulink-rest-*.jar \
  -connection "ardulink://serial?port=/dev/ttyUSB0"
```

## Implementation outline

The document only describes the migration. The following is the recommended
implementation breakdown for the follow-up change.

### Core

1. Add `QUERY_ANALOG_PIN` (`qrya`) and `QUERY_DIGITAL_PIN` (`qryd`) to
   `ALProtoBuilder.ALPProtocolKey`.
2. Add a to-device ``ToDeviceMessageQueryPin`` message carrying a `Pin`.
3. Extend `ByteStreamProcessor` with a query-pin `toDevice` method. Give it a default
   `UnsupportedOperationException` implementation so protocols without query support do
   not silently misbehave.
4. Implement ALP encoding in `ArdulinkProtocol2`:

   ```text
   alp://qrya/<pin>?id=<id>
   alp://qryd/<pin>?id=<id>
   ```

5. Add `Object queryPin(Pin pin) throws IOException` to `Link`, with a default
   unsupported implementation.
6. Implement `queryPin` in `ConnectionBasedLink`:
   - assign a message id,
   - send the query,
   - wait for the matching `FromDeviceMessageReply`/`RplyEvent`,
   - fail on `ko`, timeout, or a missing `value`,
   - return an `Integer` for analog pins and a `Boolean` for digital pins.
7. Delegate the new method in `LinkDelegate` and, where applicable, `QosLink`.
8. Make the test mock link stubbable for `queryPin`.

### Camel

`ArdulinkProducer` should recognise `alp://qrya/<pin>` and `alp://qryd/<pin>` payloads,
call `link.queryPin(pin)`, and return the value to the route. Keep the existing handling
for all current commands unchanged.

### REST

1. Read the feature flag from `PreviewFeature` in `RestRouteBuilder` (or where reads are
   handled). Do not add a CLI argument.
2. Keep the existing `readAnalog`/`readDigital` + `readQueue` path for `legacy`.
3. Add a `query` path that sends the ALP query through the Camel target and returns the
   resulting value.
4. Add a one-second timeout consistent with the current REST read timeout.
5. Document the new property and its firmware requirement in `README.md`.

## Test plan

Legacy is the default, so all existing REST read tests must remain unchanged and continue
to verify the old behaviour.

### Core

- `ArdulinkProtocol2Test`: outbound `qrya`/`qryd` encoding, with and without a message id.
- `ArdulinkProtocol2Test`: parse `alp://rply/ok?id=42&value=987`, including the `value`
  parameter.
- `ConnectionBasedLinkTest`:
  - analog query returns the integer value,
  - digital query returns `true`/`false`,
  - `ko` reply fails,
  - missing reply times out,
  - no `srla`/`srld`/`spla`/`spld` is sent during a query.

### Camel

- `qrya`/`qryd` invoke `link.queryPin` with the correct pin.
- The returned value is the route body.
- Existing command handling and `NOK` behaviour are unchanged.

### REST

- Existing tests run with the default `legacy` flag and cover the old behaviour.
- With `pin.read.query.enabled` set:
  - analog and digital GETs return the mocked query value,
  - the correct pin is queried,
  - no `startListening`/`stopListening` calls are made,
  - timeout/error produces HTTP 500.
- No CLI parsing tests for a `pinRead` flag are required (the flag is a system property).

### System tests

- The existing `deploy-dist/tests/systemtest-rest.sh` downloads firmware release `v1.2.0`
  and runs with the default `legacy` flag. It remains the end-to-end regression test for
  old firmware.
- Add a query-mode system test after a firmware release containing PR #27 is available,
  or against a PR-built firmware fixture. Until then, query mode is covered by unit and
  integration tests using the Arduino stub.

## Future automatic detection

Automatic detection is explicitly out of scope for the first change.

The current firmware PR still reports `alp://info/fw=1.3-snapshot`, so the firmware version
string alone cannot distinguish firmware with query support from firmware without it.
Before version-based detection can work, the firmware must either:

- expose a capability flag in the `info` message, or
- bump to a unique minimum version that guarantees query support.

Recommended later approach: detect the capability at startup and choose `query` when
supported, otherwise fall back to `legacy`.

Do **not** detect support by sending `qrya`/`qryd` to an arbitrary pin during startup:
the command changes the pin to `INPUT`, which can disturb existing output wiring. Also,
old firmware replies `ko` to unknown commands when an id is supplied, so a generic
unknown-command probe cannot distinguish old from new firmware.

## Rollout and removal

1. **Phase 1 (this migration):** add the manual flag, default `legacy`, and preserve all
   existing behaviour.
2. **Phase 2:** users with new firmware opt into `query`; document the requirement and the
   failure mode with old firmware.
3. **Phase 3:** add automatic capability/version detection; prefer query when supported.
4. **Phase 4:** after establishing a minimum supported firmware version, switch the
   default to query and eventually remove the legacy GET path. Keep `PATCH` for streaming
   use cases.

## Risks

- Using `query` with old firmware causes read timeouts and HTTP 500 responses.
- A query changes the pin to `INPUT`; reading a pin that was previously driven as an
  output can change the observed state.
- Concurrent reads must be isolated by request/message id so one request cannot consume
  another request's reply.
- Firmata and Telemetrix links do not support the ALP query commands and must remain on the
  legacy path.
