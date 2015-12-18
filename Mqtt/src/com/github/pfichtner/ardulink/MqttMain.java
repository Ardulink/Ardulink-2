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
package com.github.pfichtner.ardulink;

import static com.github.pfichtner.ardulink.AbstractMqttAdapter.CompactStrategy.AVERAGE;
import static com.github.pfichtner.ardulink.compactors.Tolerance.maxTolerance;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.zu.ardulink.util.Strings.nullOrEmpty;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pfichtner.ardulink.AbstractMqttAdapter.CompactStrategy;
import com.github.pfichtner.ardulink.compactors.ThreadTimeSlicer;
import com.github.pfichtner.ardulink.compactors.TimeSlicer;
import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 * [adsense]
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

	private class MqttClient extends AbstractMqttAdapter {

		private static final boolean RETAINED = true;

		private org.eclipse.paho.client.mqttv3.MqttClient client;

		private MqttClient(Link link, Config config)
				throws MqttSecurityException, MqttException {
			super(link, config);
			this.client = newClient(brokerHost, brokerPort, clientId);
			this.client.setCallback(createCallback());
		}

		public MqttClient listenToMqttAndArduino()
				throws MqttSecurityException, MqttException, IOException {
			return listenToMqtt().listenToArduino();
		}

		private MqttClient listenToMqtt() throws MqttSecurityException,
				MqttException {
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

		private MqttCallback createCallback() {
			return new MqttCallback() {
				public void connectionLost(Throwable cause) {
					logger.warn("Connection to mqtt broker lost");
					do {
						try {
							SECONDS.sleep(1);
						} catch (InterruptedException e1) {
							Thread.currentThread().interrupt();
						}
						try {
							logger.info("Trying to reconnect");
							listenToMqtt();
						} catch (Exception e) {
							logger.warn("Reconnect failed", e);
						}
					} while (!MqttClient.this.client.isConnected());
					logger.info("Successfully reconnected");
				}

				public void messageArrived(String topic, MqttMessage message)
						throws IOException {
					String payload = new String(message.getPayload());
					logger.debug(
							"Received mqtt message, sending to arduino {} {}",
							topic, payload);
					MqttClient.this.toArduino(topic, payload);
				}

				public void deliveryComplete(IMqttDeliveryToken token) {
					// nothing to do
				}
			};
		}

		private org.eclipse.paho.client.mqttv3.MqttClient newClient(
				String host, int port, String clientId) throws MqttException,
				MqttSecurityException {
			return new org.eclipse.paho.client.mqttv3.MqttClient("tcp://"
					+ host + ":" + port, clientId);
		}

		@Override
		public void fromArduino(String topic, String message) {
			try {
				logger.info("Publishing arduino state change {} {}", topic,
						message);
				publish(topic, message);
			} catch (MqttPersistenceException e) {
				throw new RuntimeException(e);
			} catch (MqttException e) {
				throw new RuntimeException(e);
			}
		}

		private void connect() throws MqttSecurityException, MqttException {
			this.client.connect(mqttConnectOptions());
			logger.info("Connected to mqtt broker");
			publishClientStatus(TRUE);
		}

		public void subscribe() throws MqttException {
			this.client.subscribe(brokerTopic + '#');
		}

		private void unsubscribe() throws MqttException {
			this.client.unsubscribe(brokerTopic + '#');
		}

		public void close() throws MqttException {
			if (this.client.isConnected()) {
				unsubscribe();
				// "kill" the callback since it retries to reconnect
				this.client.setCallback(null);
				publishClientStatus(FALSE);
				this.client.disconnect();
			}
			this.client.close();
		}

		private MqttConnectOptions mqttConnectOptions() {
			MqttConnectOptions options = new MqttConnectOptions();
			String clientInfoTopic = publishClientInfoTopic;
			if (!nullOrEmpty(clientInfoTopic)) {
				options.setWill(clientInfoTopic, FALSE.toString().getBytes(),
						0, RETAINED);
			}
			return options;
		}

		private void publish(String topic, String message)
				throws MqttException, MqttPersistenceException {
			client.publish(topic, new MqttMessage(message.getBytes()));
		}

		private void publishClientStatus(Boolean state) throws MqttException,
				MqttPersistenceException {
			if (!nullOrEmpty(publishClientInfoTopic)) {
				client.publish(publishClientInfoTopic, state.toString()
						.getBytes(), 0, RETAINED);
			}
		}

		public boolean isConnected() {
			return client.isConnected();
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
			this.standaloneServer = MqttBroker.builder().host(this.brokerHost)
					.port(this.brokerPort).startBroker();
		}
		Config config = Config.withTopic(this.brokerTopic);
		this.mqttClient = new MqttClient(link,
				this.control ? config.withControlChannelEnabled() : config)
				.listenToMqttAndArduino();
	}

	public void ensureBrokerTopicIsnormalized() {
		setBrokerTopic(this.brokerTopic);
	}

	protected Link createLink() throws Exception, URISyntaxException {
		Configurer configurer = LinkManager.getInstance().getConfigurer(
				new URI(connString));

		// are there choice values?
		for (String key : configurer.getAttributes()) {
			ConfigAttribute attribute = configurer.getAttribute(key);
			if (attribute.hasChoiceValues()) {
				Object[] choiceValues = attribute.getChoiceValues();
				// we use the first one for each
				if (choiceValues.length > 0) {
					attribute.setValue(choiceValues[0]);
				}
			}
		}

		return configurer.newLink();
	}

	public boolean isConnected() {
		return this.mqttClient != null && this.mqttClient.isConnected();
	}

	public void close() throws MqttException, IOException {
		this.link.close();
		this.mqttClient.close();
		Server tmp = this.standaloneServer;
		if (tmp != null) {
			tmp.stopServer();
		}
	}

	public void setBrokerTopic(String brokerTopic) {
		this.brokerTopic = brokerTopic.endsWith("/") ? brokerTopic
				: brokerTopic + '/';
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
			blocker.wait();
		}
	}

}
