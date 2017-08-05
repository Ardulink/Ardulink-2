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
package org.ardulink.mqtt;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.mqtt.AbstractMqttAdapter.CompactStrategy.AVERAGE;
import static org.ardulink.mqtt.compactors.Tolerance.maxTolerance;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Strings.nullOrEmpty;
import static org.ardulink.util.Throwables.propagate;
import static org.fusesource.mqtt.client.QoS.AT_LEAST_ONCE;
import static org.fusesource.mqtt.client.QoS.AT_MOST_ONCE;
import io.moquette.server.Server;

import java.io.Closeable;
import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.mqtt.AbstractMqttAdapter.CompactStrategy;
import org.ardulink.mqtt.MqttBroker.Builder;
import org.ardulink.mqtt.compactors.ThreadTimeSlicer;
import org.ardulink.mqtt.compactors.TimeSlicer;
import org.ardulink.util.URIs;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Topic;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttMain {

	private static final Logger logger = LoggerFactory
			.getLogger(MqttMain.class);

	@Option(name = "-brokerTopic", usage = "Topic to register. To switch pins a message of the form $brokerTopic/[A|D]$pinNumber/value/set must be sent. A for analog pins, D for digital pins")
	private String brokerTopic = Config.DEFAULT_TOPIC;

	@Option(name = "-brokerHost", usage = "Hostname of the broker to connect to")
	private String brokerHost = "localhost";

	@Option(name = "-brokerPort", usage = "Port of the broker to connect to")
	private int brokerPort = 1883;

	@Option(name = "-clientId", usage = "This client's name")
	private String clientId = "ardulink";

	@Option(name = "-credentials", usage = "Credentials for mqtt authentication")
	private String credentials;

	@Option(name = "-publishClientInfo", usage = "When set, publish messages on connect/disconnect under this topic")
	private String publishClientInfoTopic;

	@Option(name = "-d", aliases = "--digital", usage = "Digital pins to listen to")
	private int[] digitals = new int[0];

	@Option(name = "-a", aliases = "--analog", usage = "Analog pins to listen to")
	private int[] analogs = new int[0];

	@Option(name = "-ato", aliases = "--tolerance", usage = "Analog tolerance, publish only changes exceeding this value")
	private int tolerance = 1;

	@Option(name = "-athms", aliases = "--throttle", usage = "Analog throttle, do not publish multiple events within <throttleMillis>")
	private int throttleMillis = (int) SECONDS.toMillis(10);

	@Option(name = "-athstr", aliases = "--strategy", usage = "Analog throttle strategy")
	private CompactStrategy compactStrategy = AVERAGE;

	@Option(name = "-connection", usage = "Connection URI to the arduino")
	private String connString = "ardulink://serial";

	@Option(name = "-control", usage = "Enable the control of listeners via mqtt")
	private boolean control;

	@Option(name = "-standalone", usage = "Start a mqtt server on this host")
	private boolean standalone;

	private MqttClient mqttClient;

	private Link link;

	private Server standaloneServer;

	private class MqttClient extends AbstractMqttAdapter implements Closeable {

		private static final boolean RETAINED = true;

		private final MQTT client;
		private BlockingConnection connection;

		private boolean subscribeDone;

		private MqttClient(Link link, Config config) {
			super(link, config);
			this.client = newClient(brokerHost, brokerPort, clientId,
					credentials);
		}

		public MqttClient listenToMqttAndArduino() throws IOException {
			return listenToMqtt().listenToArduino();
		}

		private MqttClient listenToMqtt() throws IOException {
			connect();
			subscribe();
			return this;
		}

		private MqttClient listenToArduino() throws IOException {
			TimeSlicer timeSlicer = null;
			if (throttleMillis > 0) {
				timeSlicer = new ThreadTimeSlicer(throttleMillis, MILLISECONDS);
			}
			for (int analogPin : analogs) {
				AnalogReadChangeListenerConfigurer cfg = configureAnalogReadChangeListener(
						analogPin).tolerance(maxTolerance(tolerance));
				cfg = timeSlicer == null ? cfg : cfg.compact(compactStrategy,
						timeSlicer);
				cfg.add();
			}

			for (int digitalPin : digitals) {
				enableDigitalPinChangeEvents(digitalPin);
			}
			return this;
		}

		private MQTT newClient(String host, int port, String clientId,
				String credentials) {
			MQTT client = new MQTT();
			client.setCleanSession(true);
			client.setClientId(clientId);
			client.setHost(URIs.newURI("tcp://" + host + ":" + port));
			if (credentials != null) {
				String[] auth = credentials.split(":");
				checkState(auth.length == 2,
						"Credentials not in format user:password");
				client.setUserName(auth[0]);
				client.setPassword(auth[1]);
			}
			return client;
		}

		@Override
		public void fromArduino(String topic, String message) {
			logger.info("Publishing arduino state change {} {}", topic, message);
			try {
				publish(topic, message);
			} catch (IOException e) {
				throw propagate(e);
			}
		}

		private void connect() throws IOException {
			mqttConnectOptions();
			connection = client.blockingConnection();
			try {
				connection.connect();
			} catch (Exception e) {
				throw new IOException(e);
			}
			new Thread() {
				@Override
				public void run() {
					while (true) {
						try {
							org.fusesource.mqtt.client.Message message = connection
									.receive();
							String payload = new String(message.getPayload());
							String topic = message.getTopic();
							logger.debug(
									"Received mqtt message, sending to arduino {} {}",
									topic, payload);
							MqttClient.this.toArduino(topic, payload);
							message.ack();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}.start();

			logger.info("Connected to mqtt broker");
			publishClientStatus(TRUE);
		}

		public void subscribe() throws IOException {
			try {
				connection.subscribe(new Topic[] { new Topic(brokerTopic + "#",
						AT_LEAST_ONCE) });
			} catch (Exception e) {
				throw new IOException(e);
			}
			this.subscribeDone = true;
		}

		public void close() throws IOException {
			if (this.connection.isConnected()) {
				try {
					connection.unsubscribe(new String[] { new String(
							brokerTopic + "#") });
					publishClientStatus(FALSE);
					this.connection.disconnect();
					this.subscribeDone = false;
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}

		private void mqttConnectOptions() {
			String clientInfoTopic = publishClientInfoTopic;
			if (!nullOrEmpty(clientInfoTopic)) {
				client.setWillTopic(clientInfoTopic);
				client.setWillMessage(FALSE.toString());
				client.setWillRetain(RETAINED);
			}
		}

		private void publish(String topic, String message) throws IOException {
			if (connection.isConnected()) {
				try {
					connection.publish(topic, message.getBytes(),
							AT_LEAST_ONCE, false);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}

		private void publishClientStatus(Boolean state) throws IOException {
			if (!nullOrEmpty(publishClientInfoTopic)) {
				try {
					connection.publish(publishClientInfoTopic, state.toString()
							.getBytes(), AT_MOST_ONCE, RETAINED);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}

		public boolean isConnected() {
			return connection.isConnected() && this.subscribeDone;
		}

	}

	public static void main(String[] args) throws Exception {
		new MqttMain().doMain(args);
	}

	public void doMain(String... args) throws Exception {
		CmdLineParser cmdLineParser = new CmdLineParser(this);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage(System.err);
			return;
		}

		connectToMqttBroker();
		try {
			wait4ever();
		} finally {
			close();
		}

	}

	public void connectToMqttBroker() throws Exception {
		this.link = createLink();
		ensureBrokerTopicIsnormalized();
		if (standalone) {
			this.standaloneServer = createBroker().startBroker();
		}
		Config config = Config.withTopic(this.brokerTopic);
		this.mqttClient = new MqttClient(link,
				this.control ? config.withControlChannelEnabled() : config)
				.listenToMqttAndArduino();
	}

	protected Builder createBroker() {
		return MqttBroker.builder().host(this.brokerHost).port(this.brokerPort);
	}

	public void ensureBrokerTopicIsnormalized() {
		setBrokerTopic(this.brokerTopic);
	}

	protected Link createLink() throws Exception {
		Configurer configurer = LinkManager.getInstance().getConfigurer(
				URIs.newURI(connString));
		return configurer.newLink();
	}

	public boolean isConnected() {
		return this.mqttClient != null && this.mqttClient.isConnected();
	}

	public void close() throws IOException {
		Closeable closeable;
		if ((closeable = this.link) != null) {
			closeable.close();
		}
		if ((closeable = mqttClient) != null) {
			closeable.close();
		}
		Server tmpServer = this.standaloneServer;
		if (tmpServer != null) {
			tmpServer.stopServer();
		}
	}

	public void setBrokerTopic(String brokerTopic) {
		this.brokerTopic = brokerTopic.endsWith("/") ? brokerTopic
				: brokerTopic + '/';
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	public void setAnalogs(int... analogs) {
		this.analogs = analogs == null ? new int[0] : analogs.clone();
	}

	public void setDigitals(int... digitals) {
		this.digitals = digitals == null ? new int[0] : digitals.clone();
	}

	public void setThrottleMillis(int throttleMillis) {
		this.throttleMillis = throttleMillis;
	}

	public void setStandalone(boolean standalone) {
		this.standalone = standalone;
	}

	private static void wait4ever() throws InterruptedException {
		Object blocker = new Object();
		synchronized (blocker) {
			while (true) {
				blocker.wait();
			}
		}
	}

}
