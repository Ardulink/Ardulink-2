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
package org.ardulink.mqtt.util;

import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.util.Throwables.propagate;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.Type;
import org.ardulink.util.Lists;
import org.ardulink.util.Throwables;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class AnotherMqttClient implements Closeable {

	public static class Builder {

		private String host = "localhost";
		private int port = 1883;
		private String topic;
		public String clientId = "anotherMqttClient";
		public boolean appendValueSet;

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder topic(String topic) {
			this.topic = topic;
			return this;
		}

		public Builder clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		public Builder appendValueSet(boolean appendValueSet) {
			this.appendValueSet = appendValueSet;
			return this;
		}

		public AnotherMqttClient connect() {
			try {
				return new AnotherMqttClient(this).connect();
			} catch (Exception e) {
				throw propagate(e);
			}
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	private final ProducerTemplate producerTemplate;

	private final List<Message> messages = new CopyOnWriteArrayList<Message>();
	private final String topic;
	private final String controlTopic;

	private static final Map<Type, String> typeMap = unmodifiableMap(typeMap());

	private CamelContext context;

	private boolean appendValueSet;

	private static Map<Type, String> typeMap() {
		Map<Type, String> typeMap = new HashMap<Type, String>();
		typeMap.put(ANALOG, "A");
		typeMap.put(DIGITAL, "D");
		return typeMap;
	}

	private AnotherMqttClient(Builder builder) {
		this.topic = builder.topic.endsWith("/") ? builder.topic
				: builder.topic + "/";
		this.controlTopic = this.topic + "system/listening/";
		this.context = camelRoute(builder.host, builder.port);
		this.producerTemplate = context.createProducerTemplate();
		this.appendValueSet = builder.appendValueSet;
	}

	private CamelContext camelRoute(final String host, final int port) {
		try {
			CamelContext context = new DefaultCamelContext();
			context.addRoutes(new RouteBuilder() {
				@Override
				public void configure() {
					String mqtt = "mqtt://" + host + port + "?host=tcp://"
							+ host + ":" + port;
					from("direct:start").to(mqtt);
					from(mqtt + "&subscribeTopicNames=#").process(
							addTo(messages));
				}
			});
			return context;
		} catch (Exception e) {
			throw propagate(e);
		}

	}

	public AnotherMqttClient connect() throws IOException {
		try {
			context.start();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		return this;
	}

	private Processor addTo(final List<Message> addTo) {
		return new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				org.apache.camel.Message in = exchange.getIn();
				addTo.add(new Message(String.valueOf(in
						.getHeader("CamelMQTTSubscribeTopic")), in
						.getBody(String.class)));
			}
		};
	}

	public List<Message> getMessages() {
		try {
			MILLISECONDS.sleep(25);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return Lists.newArrayList(this.messages);
	}

	public List<Message> pollMessages() {
		List<Message> messages = getMessages();
		this.messages.clear();
		return messages;
	}

	public void switchPin(Pin pin, Object value) throws IOException {
		sendMessage(new Message(append(this.topic + typeMap.get(pin.getType())
				+ pin.pinNum()), String.valueOf(value)));
	}

	private String append(String msgTopic) {
		return appendValueSet ? msgTopic + "/value/set" : msgTopic;
	}

	public void startListenig(Pin pin) throws IOException {
		startStopListening(pin, true);
	}

	public void stopListenig(Pin pin) throws IOException {
		startStopListening(pin, false);
	}

	private void startStopListening(Pin pin, boolean state) throws IOException {
		sendMessage(new Message(append(this.controlTopic
				+ typeMap.get(pin.getType()) + pin.pinNum()),
				String.valueOf(state)));
	}

	private void sendMessage(Message message) throws IOException {
		producerTemplate.sendBodyAndHeader("direct:start",
				message.getMessage(), "CamelMQTTPublishTopic",
				message.getTopic());
	}

	@Override
	public void close() throws IOException {
		try {
			this.context.stop();
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	public List<Message> hasReceived() {
		return new ArrayList<Message>(messages);
	}

}
