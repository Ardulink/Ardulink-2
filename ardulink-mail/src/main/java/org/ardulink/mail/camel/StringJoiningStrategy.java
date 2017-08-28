package org.ardulink.mail.camel;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public final class StringJoiningStrategy implements
		AggregationStrategy {

	private final String joiner;

	public static AggregationStrategy joinUsing(String joiner) {
		return new StringJoiningStrategy(joiner);
	}

	private StringJoiningStrategy(String joiner) {
		this.joiner = joiner;
	}

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		if (oldExchange == null) {
			return newExchange;
		}
		oldExchange.getIn().setBody(
				bodyOf(oldExchange) + joiner + bodyOf(newExchange));
		return oldExchange;
	}

	private String bodyOf(Exchange exchange) {
		return exchange.getIn().getBody(String.class);
	}

}