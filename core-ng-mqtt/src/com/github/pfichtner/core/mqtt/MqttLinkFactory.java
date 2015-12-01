package com.github.pfichtner.core.mqtt;

import java.io.IOException;
import java.net.UnknownHostException;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;

public class MqttLinkFactory implements LinkFactory<MqttLinkConfig> {

	@Override
	public String getName() {
		return "mqtt";
	}

	@Override
	public Link newLink(MqttLinkConfig config) throws UnknownHostException,
			IOException, MqttException {
		return new MqttLink(config);
	}

	public MqttLinkConfig newLinkConfig() {
		return new MqttLinkConfig();
	}

}
