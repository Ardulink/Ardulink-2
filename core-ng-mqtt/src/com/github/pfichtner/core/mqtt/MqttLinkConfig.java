package com.github.pfichtner.core.mqtt;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;

public class MqttLinkConfig implements LinkConfig {

	private static final String HOST = "host";

	private static final String PORT = "port";

	private static final String TOPIC = "topic";

	private static final String CLIENTID = "clientid";

	private String host = "localhost";
	private int port = 1883;
	private String topic = normalize("home/devices/ardulink/");
	private String clientId = "ardulink-mqtt-link";

	@Named(HOST)
	public void setHost(String host) {
		this.host = host;
	}

	@Named(PORT)
	public void setPort(int port) {
		this.port = port;
	}

	@Named(TOPIC)
	public void setTopic(String topic) {
		this.topic = normalize(topic);
	}

	@Named(CLIENTID)
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getTopic() {
		return topic;
	}

	public String getClientId() {
		return clientId;
	}

	private static String normalize(String topic) {
		return topic.endsWith("/") ? topic : topic + "/";
	}

}
