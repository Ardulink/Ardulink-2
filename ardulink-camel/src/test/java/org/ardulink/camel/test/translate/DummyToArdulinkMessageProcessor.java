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
package org.ardulink.camel.test.translate;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageCustom;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DummyToArdulinkMessageProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		Message out = getMessageTarget(exchange);
		String body = in.getBody(String.class);
		if ("send Custom Message".equals(body)) {
			out.setBody(new DefaultToDeviceMessageCustom("dummy"));
		} else {
			out.setFault(true);
		}

	}

	private Message getMessageTarget(Exchange exchange) {
		Message in = exchange.getIn();
		if (exchange.getPattern().isOutCapable()) {
			Message out = exchange.getOut();
			out.setHeaders(in.getHeaders());
			out.setAttachments(in.getAttachments());
			return out;
		}
		return in;
	}

}
