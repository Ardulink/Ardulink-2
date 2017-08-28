package org.ardulink.mail.camel;

import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Strings.nullOrEmpty;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.ardulink.util.ListMultiMap;

/**
 * Translates the scenario to Ardulink protocol (Ardulink Camel endpoint
 * protocol).
 */
public class ScenarioProcessor implements Processor {

	private ListMultiMap<String, String> scenarios = new ListMultiMap<String, String>();

	public static ScenarioProcessor processScenario() {
		return new ScenarioProcessor();
	}

	private ScenarioProcessor() {
		super();
	}

	public ScenarioProcessor withCommand(String commandName,
			List<String> commands) {
		for (String command : commands) {
			this.scenarios.put(commandName, command);
		}
		return this;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		exchange.getIn().setBody(commandsFor(bodyOf(exchange)));
	}

	private static String bodyOf(Exchange exchange) {
		String body = exchange.getIn().getBody(String.class);
		checkState(!nullOrEmpty(body), "body is empty");
		return body;
	}

	private List<String> commandsFor(String body) {
		List<String> commands = scenarios.asMap().get(body);
		checkState(commands != null, "scenario %s not known", body);
		return commands;
	}

}