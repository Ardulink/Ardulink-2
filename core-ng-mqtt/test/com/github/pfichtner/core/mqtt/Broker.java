package com.github.pfichtner.core.mqtt;

import java.io.IOException;

import org.dna.mqtt.moquette.server.Server;
import org.junit.rules.ExternalResource;

public class Broker extends ExternalResource {

	private Server mqttServer;
	
	private Broker() {
		super();
	}
	
	public static Broker newBroker() {
		return new Broker();
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
		this.mqttServer.startServer();
	}

	public void stop() {
		this.mqttServer.stopServer();
	}

}
