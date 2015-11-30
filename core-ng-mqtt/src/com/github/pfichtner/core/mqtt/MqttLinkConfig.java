package com.github.pfichtner.core.mqtt;

import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;

public class MqttLinkConfig implements LinkConfig {

	private static final String HOST = "host";

	private static final String PORT = "port";

	private static final String TOPIC = "topic";

	private String host;
	private int port;
	private String topic;

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
		this.topic = topic;
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

}
