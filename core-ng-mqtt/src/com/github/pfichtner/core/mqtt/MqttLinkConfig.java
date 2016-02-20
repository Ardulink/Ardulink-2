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
		this.topic = normalize(topic);
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
