package org.ardulink.camel;

import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultMessage;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.proto.impl.ALProtoBuilder;

public class ArdulinkConsumer extends DefaultConsumer {

	private final EventListener listener = listener();

	public ArdulinkConsumer(ArdulinkEndpoint endpoint, Processor processor)
			throws IOException {
		super(endpoint, processor);
	}

	@Override
	public void start() throws Exception {
		getEndpoint().getLink().addListener(listener);
		super.start();
	}

	@Override
	public void stop() throws Exception {
		getEndpoint().getLink().removeListener(listener);
		super.stop();
	}

	public ArdulinkEndpoint getEndpoint() {
		return (ArdulinkEndpoint) super.getEndpoint();
	}

	private EventListener listener() {
		return new EventListener() {

			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				String body = ALProtoBuilder
						.alpProtocolMessage(DIGITAL_PIN_READ)
						.forPin(event.getPin().pinNum())
						.withState(event.getValue().booleanValue());
				process(exchangeWithBody(body));
			}

			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				String body = ALProtoBuilder
						.alpProtocolMessage(ANALOG_PIN_READ)
						.forPin(event.getPin().pinNum())
						.withValue(event.getValue().intValue());
				process(exchangeWithBody(body));
			}

			private Exchange exchangeWithBody(String body) {
				Exchange exchange = getEndpoint().createExchange();
				Message message = new DefaultMessage();
				message.setBody(body);
				exchange.setIn(message);
				return exchange;
			}

			private void process(Exchange exchange) {
				try {
					getAsyncProcessor().process(exchange);
				} catch (Exception ex) {
					ex.printStackTrace();
					getExceptionHandler().handleException(
							"Failed to process notification", ex);
				}
			}

		};

	}

}
