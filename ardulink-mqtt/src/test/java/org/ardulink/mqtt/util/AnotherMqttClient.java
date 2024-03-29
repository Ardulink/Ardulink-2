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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.mqtt.MqttCamelRouteBuilder.PUBLISH_HEADER;
import static org.ardulink.mqtt.MqttCamelRouteBuilder.SUBSCRIBE_HEADER;
import static org.ardulink.util.Throwables.propagate;
import static org.awaitility.Awaitility.await;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.Type;
import org.ardulink.mqtt.MqttCamelRouteBuilder;
import org.ardulink.util.Throwables;
import org.assertj.core.api.ThrowingConsumer;

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
		private int port = MqttCamelRouteBuilder.DEFAULT_PORT;
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

	private final List<Message> messages = new CopyOnWriteArrayList<>();
	private final String topic;
	private final String controlTopic;

	private static final Map<Type, String> typeMap = unmodifiableMap(typeMap());

	private CamelContext context;

	private boolean appendValueSet;

	private static Map<Type, String> typeMap() {
		Map<Type, String> typeMap = new HashMap<>();
		typeMap.put(ANALOG, "A");
		typeMap.put(DIGITAL, "D");
		return typeMap;
	}

	private AnotherMqttClient(Builder builder) {
		this.topic = builder.topic.endsWith("/") ? builder.topic : builder.topic + "/";
		this.controlTopic = this.topic + "system/listening/";
		this.context = camelRoute(builder.host, builder.port);
		this.producerTemplate = context.createProducerTemplate();
		this.appendValueSet = builder.appendValueSet;
	}

	private CamelContext camelRoute(String host, int port) {
		String mqtt = "paho://#?brokerUrl=tcp://" + host + ":" + port + "&qos=0";
		try {
			CamelContext context = new DefaultCamelContext();
			context.addRoutes(new RouteBuilder() {
				@Override
				public void configure() {
					from("direct:start").to(mqtt);
					from(mqtt).process(addTo(messages));
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

	private Processor addTo(List<Message> addTo) {
		return exchange -> {
			org.apache.camel.Message inMessage = exchange.getIn();
			addTo.add(new Message(String.valueOf(inMessage.getHeader(SUBSCRIBE_HEADER)),
					inMessage.getBody(String.class)));
		};
	}

	public void awaitMessages(ThrowingConsumer<List<Message>> throwingConsumer) {
		awaitMessages(throwingConsumer, 10, SECONDS);
	}

	public void awaitMessages(ThrowingConsumer<List<Message>> throwingConsumer, long timeout, TimeUnit timeUnit) {
		await().timeout(timeout, timeUnit).pollInterval(Duration.ofMillis(100))
				.untilAsserted(() -> throwingConsumer.accept(messages));
	}

	public void clear() {
		this.messages.clear();
	}

	public void switchPin(Pin pin, Object value) throws IOException {
		sendMessage(new Message(append(this.topic + typeMap.get(pin.getType()) + pin.pinNum()), String.valueOf(value)));
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
		sendMessage(new Message(append(this.controlTopic + typeMap.get(pin.getType()) + pin.pinNum()),
				String.valueOf(state)));
	}

	private void sendMessage(Message message) throws IOException {
		producerTemplate.sendBodyAndHeader("direct:start", message.getMessage(), PUBLISH_HEADER, message.getTopic());
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
		return new ArrayList<>(messages);
	}

}
