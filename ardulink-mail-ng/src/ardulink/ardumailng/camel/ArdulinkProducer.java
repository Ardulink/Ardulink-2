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

package ardulink.ardumailng.camel;

import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.zu.ardulink.util.Joiner;
import org.zu.ardulink.util.ListMultiMap;
import org.zu.ardulink.util.Lists;
import org.zu.ardulink.util.Optional;

import ardulink.ardumailng.Command;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.convenience.Links;

public class ArdulinkProducer extends DefaultProducer {

	private final Link link;

	public ArdulinkProducer(Endpoint endpoint, String type, String typeParams) {
		super(endpoint);
		try {
			String str = "ardulink://"
					+ checkNotNull(type, "type must not be null");
			if (typeParams != null && !typeParams.isEmpty()) {
				str += "?" + typeParams;
			}
			this.link = Links.getLink(new URI(str));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	private final List<String> validFroms = new ArrayList<String>();
	private final ListMultiMap<String, Command> commands = new ListMultiMap<String, Command>();

	private Optional<String> process(Message message) {
		String from = message.getHeader("From", String.class);
		checkState(from != null && !from.isEmpty(), "No from set in message");
		checkState(validFroms.contains(from),
				"From user %s not a valid from address", from);

		checkState(message.getBody() instanceof String, "Body not a String");
		String commandName = (String) message.getBody();
		checkState(!commandName.isEmpty(), "Body not set");

		for (Entry<String, List<Command>> entry : commands.asMap().entrySet()) {
			String key = entry.getKey();
			if (commandName.equals(key)) {
				List<Command> values = entry.getValue();
				List<String> results = Lists.newArrayList();
				for (Command command : values) {
					try {
						command.execute(link);
						results.add(command + "=OK");
					} catch (Exception e) {
						results.add(command + "=KO");
					}
				}
				return Optional.of(Joiner.on("\n").join(results));
			}
		}
		return Optional.absent();
	}

	public void setValidFroms(List<String> validFroms) {
		this.validFroms.addAll(validFroms);
	}

	public void setCommands(String onValue, Command... commands) {
		setCommands(onValue, Arrays.asList(commands));
	}

	public void setCommands(String onValue, List<Command> commands) {
		for (Command command : commands) {
			this.commands.put(onValue, command);
		}
	}

}
