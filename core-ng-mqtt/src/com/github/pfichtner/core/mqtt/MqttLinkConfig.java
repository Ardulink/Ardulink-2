package com.github.pfichtner.core.mqtt;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;

public class MqttLinkConfig implements LinkConfig {

	@Named("host")
	private String host = "localhost";

	@Named("port")
	private int port = 1883;

	@Named("topic")
	private String topic = normalize("home/devices/ardulink/");

	@Named("clientid")
	private String clientId = "ardulink-mqtt-link";

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	private static String normalize(String topic) {
		return topic.endsWith("/") ? topic : topic + "/";
	}

}
