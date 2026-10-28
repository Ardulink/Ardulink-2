package org.ardulink.camel;

import static org.ardulink.core.proto.ardulink.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;

import java.io.IOException;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;
import org.apache.camel.support.DefaultMessage;
import org.ardulink.core.Link;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;

public class ArdulinkConsumer extends DefaultConsumer {

	private final EventListener eventListener = eventListener();
	private final Link link;

	public ArdulinkConsumer(Endpoint endpoint, Processor processor, Link link) {
		super(endpoint, processor);
		this.link = link;
	}

	@Override
	public void start() {
		try {
			link.addListener(eventListener);
			super.start();
		} catch (IOException e) {
			fail(e);
		}
	}

	@Override
	public void stop() {
		try {
			link.removeListener(eventListener);
			super.stop();
		} catch (IOException e) {
			fail(e);
		}
	}

	private EventListener eventListener() {
		return new EventListener() {

			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				String body = alpProtocolMessage(DIGITAL_PIN_READ).forPin(event.getPin().pinNum())
						.withState(event.getValue().booleanValue());
				process(exchangeWithBody(body));
			}

			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				String body = alpProtocolMessage(ANALOG_PIN_READ).forPin(event.getPin().pinNum())
						.withValue(event.getValue());
				process(exchangeWithBody(body));
			}

			private Exchange exchangeWithBody(String body) {
				Exchange exchange = getEndpoint().createExchange();
				Message message = new DefaultMessage(exchange.getContext());
				message.setBody(body);
				exchange.setMessage(message);
				return exchange;
			}

			private void process(Exchange exchange) {
				try {
					getAsyncProcessor().process(exchange);
				} catch (Exception e) {
					getExceptionHandler().handleException("Failed to process notification", e);
				}
			}

		};

	}

}
