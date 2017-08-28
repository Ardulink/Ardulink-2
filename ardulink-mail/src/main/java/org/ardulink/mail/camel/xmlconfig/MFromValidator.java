package org.ardulink.mail.camel.xmlconfig;

import java.util.Collections;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.ardulink.mail.camel.FromValidator;

//only used by route de xml (and route xml is not working properly at the moment)
@Deprecated
public final class MFromValidator implements Processor {

	private FromValidator delegate = FromValidator
			.validateFromHeader(Collections.<String> emptyList());

	public void setValidFroms(Iterable<String> validFroms) {
		delegate = FromValidator.validateFromHeader(validFroms);
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		delegate.process(exchange);
	}

}