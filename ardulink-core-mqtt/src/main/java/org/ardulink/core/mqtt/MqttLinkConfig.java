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

import static org.ardulink.util.Objects.firstNonNull;
import static org.ardulink.util.Preconditions.checkNotNull;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkConfig.I18n;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@I18n("message")
public class MqttLinkConfig implements LinkConfig {

	public static final String DEFAULT_HOST = "localhost";
	
	public static final int DEFAULT_PORT = 1883;

	private static final Qos DEFAULT_QOS = Qos.DEFAULT;
	
	public enum Connection {
		TCP, SSL, TLS
	}

	@Named("host")
	@NotNull
	private String host = DEFAULT_HOST;

	@Named("port")
	@Positive
	@Max(2 << 16 - 1)
	public int port = DEFAULT_PORT;

	@Named("connection")
	private Connection connection = Connection.TCP;

	@Named("topic")
	@NotNull
	private String topic = normalize("home/devices/ardulink/");

	@Named("qos")
	private Qos qos = DEFAULT_QOS;

	@Named("clientId")
	@NotNull
	private String clientId = "ardulink-mqtt-link";

	@Named("user")
	public String user;

	@Named("password")
	public String password;

	@Named("separatedTopics")
	public boolean separateTopics;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = firstNonNull(host, DEFAULT_HOST);
	}

	public Qos getQos() {
		return qos;
	}

	public void setQos(Qos qos) {
		this.qos = firstNonNull(qos, DEFAULT_QOS);
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = checkNotNull(connection, "connection must not be null");
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = normalize(checkNotNull(topic, "topic must not be null"));
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = checkNotNull(clientId, "clientId must not be null");
	}

	private static String normalize(String topic) {
		return topic.endsWith("/") ? topic : topic + "/";
	}

}
