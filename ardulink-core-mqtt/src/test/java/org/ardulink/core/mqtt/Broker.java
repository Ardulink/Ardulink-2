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

package org.ardulink.core.mqtt;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.FilesystemConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ardulink.core.mqtt.duplicated.Message;
import org.ardulink.util.Lists;
import org.junit.rules.ExternalResource;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class Broker extends ExternalResource {

	private Server mqttServer;
	private final List<InterceptHandler> listeners = Lists.newArrayList();
	private final List<Message> messages = new CopyOnWriteArrayList<Message>();

	private Broker() {
		super();
	}

	public static Broker newBroker() {
		return new Broker();
	}

	public static Broker newBroker(
			Collection<? extends InterceptHandler> listeners) {
		Broker newBroker = newBroker();
		newBroker.listeners.addAll(listeners);
		return newBroker;
	}

	@Override
	protected void before() throws IOException, InterruptedException {
		this.mqttServer = new Server();
		start();
	}

	@Override
	protected void after() {
		stop();
	}

	public void start() throws IOException {
		if (listeners.isEmpty()) {
			this.mqttServer.startServer();
		} else {
			this.mqttServer.startServer(new FilesystemConfig(), listeners);
		}
	}

	public void stop() {
		this.mqttServer.stopServer();
	}

	public Broker recordMessages() {
		listeners.add(new AbstractInterceptHandler() {
			public void onPublish(InterceptPublishMessage message) {
				messages.add(new Message(message.getTopicName(), new String(
						message.getPayload().array())));
			};
		});
		return this;
	}

	public List<Message> getMessages() {
		return Lists.newArrayList(messages);
	}

}
