package org.ardulink.mail.camel;

import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Strings.nullOrEmpty;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.ardulink.util.Lists;

public final class FromValidator implements Processor {

	private final Iterable<String> validFroms;

	public static FromValidator validateFromHeader(Iterable<String> validFroms) {
		return new FromValidator(validFroms);
	}

	private FromValidator(Iterable<String> validFroms) {
		this.validFroms = Lists.newArrayList(validFroms);
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		String header = "From";
		String from = exchange.getIn().getHeader(header, String.class);
		checkState(!nullOrEmpty(from), "No from header in message");
		checkState(matches(from), "From user %s not a valid from address", from);
	}

	private boolean matches(String from) {
		for (String validFrom : validFroms) {
			if (validFrom.equalsIgnoreCase(from)) {
				return true;
			}
		}
		return false;
	}

}