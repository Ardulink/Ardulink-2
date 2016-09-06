package org.ardulink.camel;

import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.ardulink.core.messages.api.LinkMessageAdapter;
import org.ardulink.core.messages.api.ToDeviceMessage;

public class ArdulinkProducer extends DefaultProducer {

	private LinkMessageAdapter linkMessageAdapter;
	
	public ArdulinkProducer(ArdulinkEndpoint endpoint) {
		super(endpoint);
		
		linkMessageAdapter = endpoint.getLinkMessageAdapter();
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Message message = exchange.getIn();
		Object body = message.getBody();
		
		checkNotNull(body, "Camel body message is null");
		checkState(body instanceof ToDeviceMessage, "Expected an OutMessage as body obtained: %s", body.getClass().getCanonicalName());

		linkMessageAdapter.sendMessage((ToDeviceMessage)body);
	}
}
