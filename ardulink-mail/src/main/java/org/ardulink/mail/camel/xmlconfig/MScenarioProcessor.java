package org.ardulink.mail.camel.xmlconfig;

import static org.ardulink.util.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.ardulink.mail.camel.ScenarioProcessor;

//only used by route de xml (and route xml is not working properly at the moment)
@Deprecated
public class MScenarioProcessor implements Processor {

	private final ScenarioProcessor delegate = ScenarioProcessor
			.processScenario();

	public void setNameAndCommands(String nameAndCommands) {
		List<String> split = Arrays.asList(nameAndCommands.split("\\,"));
		checkArgument(split.size() >= 2,
				"Could not split %s into at two parts or more using ','",
				nameAndCommands);
		delegate.withCommand(split.get(0), split.subList(1, split.size()));
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		delegate.process(exchange);
	}

}