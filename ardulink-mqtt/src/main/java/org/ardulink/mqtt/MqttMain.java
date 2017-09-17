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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.mqtt.MqttCamelRouteBuilder.CompactStrategy.AVERAGE;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Strings.nullOrEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.mqtt.MqttBroker.Builder;
import org.ardulink.mqtt.MqttCamelRouteBuilder.CompactStrategy;
import org.ardulink.mqtt.MqttCamelRouteBuilder.MqttConnectionProperties;
import org.ardulink.util.Joiner;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttMain {

	@Option(name = "-brokerTopic", usage = "Topic to register. "
			+ "To switch pins a message of the form $brokerTopic/[A|D]$pinNumber must be sent. "
			+ "When separateTopics is enabled the topis has to be $brokerTopic/[A|D]$pinNumber/value/set. "
			+ "A for analog pins, D for digital pins")
	private String brokerTopic = Topics.DEFAULT_BASE_TOPIC;

	@Option(name = "-separateTopics", usage = "use one toic for read/write or use separate topics (value/set and value/get)")
	private boolean separateTopics;

	@Option(name = "-brokerHost", usage = "Hostname of the broker to connect to")
	private String brokerHost = "localhost";

	@Option(name = "-brokerPort", usage = "Port of the broker to connect to")
	private Integer brokerPort;

	@Option(name = "-brokerssl", usage = "Communicate encrypted with the broker using SSL")
	private boolean ssl;

	@Option(name = "-clientId", usage = "This client's name")
	private String clientId = "ardulink";

	@Option(name = "-credentials", usage = "Credentials for mqtt authentication")
	private String credentials;

	// TODO PF re-add
	// @Option(name = "-publishClientInfo", usage =
	// "When set, publish messages on connect/disconnect under this topic")
	// private String publishClientInfoTopic;

	@Option(name = "-d", aliases = "--digital", usage = "Digital pins to listen to")
	private int[] digitals = new int[0];

	@Option(name = "-a", aliases = "--analog", usage = "Analog pins to listen to")
	private int[] analogs = new int[0];

	@Option(name = "-athms", aliases = "--throttle", usage = "Analog throttle, do not publish multiple events within <throttleMillis>")
	private int throttleMillis = (int) MILLISECONDS.toMillis(250);

	@Option(name = "-athstr", aliases = "--strategy", usage = "Analog throttle strategy")
	private CompactStrategy compactStrategy = AVERAGE;

	@Option(name = "-connection", usage = "Connection URI to the arduino")
	private String connection = "ardulink://serial";

	@Option(name = "-control", usage = "Enable the control of listeners via mqtt")
	private boolean control;

	@Option(name = "-standalone", usage = "Start a mqtt server on this host")
	private boolean standalone;

	private MqttBroker standaloneServer;

	private CamelContext context;

	private CamelContext createCamelContext(Topics topics) throws Exception {
		return addRoutes(topics, new DefaultCamelContext());
	}

	private CamelContext addRoutes(Topics topics, CamelContext context)
			throws Exception {
		MqttCamelRouteBuilder rb = new MqttCamelRouteBuilder(context, topics);
		if (throttleMillis > 0 && compactStrategy != null) {
			rb = rb.compact(compactStrategy, throttleMillis, MILLISECONDS);
		}
		String ardulink = appendListenTo(connection);
		MqttConnectionProperties mqtt = appendAuth(
				new MqttConnectionProperties().name("mqttMain")
						.brokerHost(brokerHost).ssl(ssl))
				.brokerPort(brokerPort);
		rb.fromSomethingToMqtt(ardulink, mqtt).andReverse();
		return context;
	}

	private String appendListenTo(String connection) {
		String listenTo = listenTo();
		return listenTo.isEmpty() ? connection : connection
				+ (connection.contains("?") ? "&" : "?") + "listenTo="
				+ listenTo;
	}

	private MqttConnectionProperties appendAuth(
			MqttConnectionProperties properties) {
		if (nullOrEmpty(credentials)) {
			return properties;
		}
		String[] auth = credentials.split(":");
		checkState(auth.length == 2, "Credentials not in format user:password");
		return properties.auth(auth[0], auth[1].getBytes());
	}

	private String listenTo() {
		return Joiner.on(",").join(
				add("D%s", digitals,
						add("A%s", analogs, new ArrayList<String>())));
	}

	private List<String> add(String format, int[] pins, List<String> to) {
		for (int pin : pins) {
			to.add(String.format(format, pin));
		}
		return to;
	}

	public static void main(String[] args) throws Exception {
		try {
			new MqttMain().doMain(args);
		} catch (Exception e) {
			// we have to System#exit because the camel context keeps MqttMain
			// running (even when calling Context#stop)
			e.printStackTrace();
			System.exit(1);
		}
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
		ensureBrokerTopicIsnormalized();
		if (standalone) {
			this.standaloneServer = createBroker().startBroker();
		}
		Topics topics = separateTopics ? Topics
				.withSeparateReadWriteTopics(this.brokerTopic) : Topics
				.basedOn(this.brokerTopic);
		this.context = createCamelContext(this.control ? topics
				.withControlChannelEnabled() : topics);
		this.context.start();
	}

	protected Builder createBroker() {
		return MqttBroker.builder().host(this.brokerHost).useSsl(this.ssl)
				.port(brokerPort);
	}

	public void ensureBrokerTopicIsnormalized() {
		setBrokerTopic(this.brokerTopic);
	}

	public boolean isConnected() {
		List<Route> routes = context.getRoutes();
		for (Route route : routes) {
			if (!context.getRouteStatus(route.getId()).isStarted()) {
				return false;
			}
		}
		return true;
	}

	public void close() throws IOException {
		CamelContext tmpContext = this.context;
		if ((tmpContext) != null) {
			try {
				tmpContext.stop();
			} catch (Exception e) {
				throw new IOException("Error stoping camel", e);
			}
		}

		MqttBroker tmpServer = this.standaloneServer;
		if (tmpServer != null) {
			tmpServer.stop();
		}
	}

	public void setBrokerPort(int brokerPort) {
		this.brokerPort = brokerPort;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
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

	public void setConnection(String connection) {
		this.connection = connection;
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
