package com.github.pfichtner.core.mqtt;

import java.io.IOException;

import org.dna.mqtt.moquette.server.Server;
import org.junit.rules.ExternalResource;

public class Broker extends ExternalResource {

	private Server mqttServer;

	@Override
	protected void before() throws IOException, InterruptedException {
		this.mqttServer = startMqttServer();
	}

	private static Server startMqttServer() throws IOException {
		Server broker = new Server();
		broker.startServer();
		return broker;
	}

	@Override
	protected void after() {
		this.mqttServer.stopServer();
	}

}
