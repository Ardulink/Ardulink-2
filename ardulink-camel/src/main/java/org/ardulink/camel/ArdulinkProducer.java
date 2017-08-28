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

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.messages.api.FromDeviceChangeListeningState.Mode.START;
import static org.ardulink.core.messages.api.FromDeviceChangeListeningState.Mode.STOP;

import java.io.IOException;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.messages.api.FromDeviceChangeListeningState;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkProducer extends DefaultProducer {

	private final Link link;

	/**
	 * This is NOT the protocol of the link but the expected payload of camel's
	 * {@link Message}.
	 */
	private final Protocol protocol = ArdulinkProtocol2.instance();

	public ArdulinkProducer(Endpoint endpoint, Link link) {
		super(endpoint);
		this.link = link;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		String body = exchange.getIn().getBody(String.class);
		FromDeviceMessage fromDevice = protocol.fromDevice(body.getBytes());
		if (fromDevice instanceof FromDeviceMessagePinStateChanged) {
			handlePinStateChange((FromDeviceMessagePinStateChanged) fromDevice);
			setResponse(exchange, body, "OK");
		} else if (fromDevice instanceof FromDeviceChangeListeningState) {
			handleListeningStateChange((FromDeviceChangeListeningState) fromDevice);
			setResponse(exchange, body, "OK");
		}
	}

	private void setResponse(Exchange exchange, String bodyIn, String rc) {
		exchange.getIn().setBody(bodyIn + "=" + rc);
	}

	private void handlePinStateChange(FromDeviceMessagePinStateChanged event)
			throws IOException {
		Pin pin = event.getPin();
		if (pin.is(ANALOG)) {
			link.switchAnalogPin(analogPin(pin.pinNum()),
					Integer.parseInt(String.valueOf(event.getValue())));
		} else if (pin.is(DIGITAL)) {
			link.switchDigitalPin(digitalPin(pin.pinNum()),
					Boolean.parseBoolean(String.valueOf(event.getValue())));
		}
	}

	private void handleListeningStateChange(FromDeviceChangeListeningState event)
			throws IOException {
		Pin pin = event.getPin();
		if (event.getMode() == START) {
			link.startListening(pin);
		} else if (event.getMode() == STOP) {
			link.stopListening(pin);
		}
	}

	@Override
	public void stop() throws Exception {
		this.link.close();
		super.stop();
	}

}
