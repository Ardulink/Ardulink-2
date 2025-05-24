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

package org.ardulink.camel;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.ardulink.camel.ArdulinkProducer.Handled.HANDLED_SUCCESSFULLY;
import static org.ardulink.camel.ArdulinkProducer.Handled.NOT_HANDLED;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessors.parse;
import static org.ardulink.util.Iterables.getFirst;

import java.io.IOException;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.support.DefaultProducer;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceChangeListeningState;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.proto.ardulink.ArdulinkProtocol2.ALPByteStreamProcessor;

/**
 * [ardulinktitle] [ardulinkversion]
 *
 * project Ardulink http://www.ardulink.org/
 *
 * [adsense]
 *
 */
public class ArdulinkProducer extends DefaultProducer {

	static enum Handled {
		HANDLED_SUCCESSFULLY, NOT_HANDLED;
	}

	private final Link link;

	/**
	 * This is NOT the protocol of the link but the expected payloads of camel's
	 * {@link Message}s since they are ALP.
	 */
	private final ALPByteStreamProcessor camelPayloadProcessor = new ALPByteStreamProcessor();

	public ArdulinkProducer(Endpoint endpoint, Link link) {
		super(endpoint);
		this.link = link;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		String payload = exchange.getIn().getBody(String.class);
		FromDeviceMessage fromDevice = payload == null //
				? null //
				: getFirst(parse(camelPayloadProcessor, camelPayloadProcessor.toBytes(payload))).orElse(null);
		Handled handledSuccessfully = NOT_HANDLED;
		if (fromDevice instanceof FromDeviceMessagePinStateChanged) {
			handledSuccessfully = handlePinStateChange((FromDeviceMessagePinStateChanged) fromDevice);
		} else if (fromDevice instanceof FromDeviceChangeListeningState) {
			handledSuccessfully = handleListeningStateChange((FromDeviceChangeListeningState) fromDevice);
		}
		String rc = handledSuccessfully == HANDLED_SUCCESSFULLY ? "OK" : "NOK";
		exchange.getMessage().setBody(format("%s=%s", payload, rc));
	}

	private Handled handlePinStateChange(FromDeviceMessagePinStateChanged event) throws IOException {
		Pin pin = event.getPin();
		String value = String.valueOf(event.getValue());
		if (pin.is(ANALOG)) {
			link.switchAnalogPin(analogPin(pin.pinNum()), parseInt(value));
			return HANDLED_SUCCESSFULLY;
		}
		if (pin.is(DIGITAL)) {
			link.switchDigitalPin(digitalPin(pin.pinNum()), parseBoolean(value));
			return HANDLED_SUCCESSFULLY;
		}
		return NOT_HANDLED;
	}

	private Handled handleListeningStateChange(FromDeviceChangeListeningState event) throws IOException {
		switch (event.getMode()) {
		case START:
			link.startListening(event.getPin());
			return HANDLED_SUCCESSFULLY;
		case STOP:
			link.stopListening(event.getPin());
			return HANDLED_SUCCESSFULLY;
		}
		return NOT_HANDLED;
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		this.link.close();
	}

}
