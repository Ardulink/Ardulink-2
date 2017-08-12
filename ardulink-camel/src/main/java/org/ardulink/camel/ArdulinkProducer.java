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

import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Throwables.propagate;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.ardulink.camel.command.Command;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.util.Optional;
import org.ardulink.util.Strings;
import org.ardulink.util.URIs;

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

	public ArdulinkProducer(Endpoint endpoint, String type, String typeParams) {
		super(endpoint);
		try {
			String base = "ardulink://"
					+ checkNotNull(type, "type must not be null");
			this.link = Links.getLink(URIs
					.newURI(appendParams(base, typeParams)));
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private static String appendParams(String base, String typeParams) {
		return Strings.nullOrEmpty(typeParams) ? base : base + "?" + typeParams;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Optional<String> out = process(exchange.getIn());
		if (out.isPresent()) {
			getMessageTarget(exchange).setBody(out.get(), String.class);
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

	@Override
	public void stop() throws Exception {
		this.link.close();
		super.stop();
	}

	// --------------------------------------------------------------------------------------------------------

	private Optional<String> process(Message message) throws Exception {
		Command command = message.getBody(Command.class);
		// TODO PF checkState or if-branch?
		if (command != null) {
			command.execute(link);
		}
		return Optional.absent();
	}

}
