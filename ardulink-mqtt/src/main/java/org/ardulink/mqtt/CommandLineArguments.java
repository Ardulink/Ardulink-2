package org.ardulink.mqtt;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.mqtt.MqttCamelRouteBuilder.CompactStrategy.AVERAGE;

import org.ardulink.mqtt.MqttCamelRouteBuilder.CompactStrategy;
import org.kohsuke.args4j.Option;

public class CommandLineArguments {

	private static final int[] NO_PINS = new int[0];

	@Option(name = "-brokerTopic", usage = "Topic to register. "
			+ "To switch pins a message of the form $brokerTopic/[A|D]$pinNumber must be sent. "
			+ "When separateTopics is enabled the topis has to be $brokerTopic/[A|D]$pinNumber/value/set. "
			+ "A for analog pins, D for digital pins")
	public String brokerTopic = Topics.DEFAULT_BASE_TOPIC;

	@Option(name = "-separateTopics", usage = "use one toic for read/write or use separate topics (value/set and value/get)")
	public boolean separateTopics;

	@Option(name = "-brokerHost", usage = "Hostname of the broker to connect to")
	public String brokerHost = "localhost";

	@Option(name = "-brokerPort", usage = "Port of the broker to connect to")
	public Integer brokerPort;

	@Option(name = "-brokerssl", usage = "Communicate encrypted with the broker using SSL")
	public boolean ssl;

	@Option(name = "-clientId", usage = "This client's name")
	public String clientId = "ardulink";

	@Option(name = "-credentials", usage = "Credentials for mqtt authentication")
	public String credentials;

	// TODO PF re-add
	// @Option(name = "-publishClientInfo", usage =
	// "When set, publish messages on connect/disconnect under this topic")
	// public String publishClientInfoTopic;

	@Option(name = "-d", aliases = "--digital", usage = "Digital pins to listen to")
	public int[] digitals = NO_PINS;

	@Option(name = "-a", aliases = "--analog", usage = "Analog pins to listen to")
	public int[] analogs = NO_PINS;

	@Option(name = "-athms", aliases = "--throttle", usage = "Analog throttle, do not publish multiple events within <throttleMillis>")
	public int throttleMillis = (int) MILLISECONDS.toMillis(250);

	@Option(name = "-athstr", aliases = "--strategy", usage = "Analog throttle strategy")
	public CompactStrategy compactStrategy = AVERAGE;

	@Option(name = "-connection", usage = "Connection URI to the arduino")
	public String connection = "ardulink://serial";

	@Option(name = "-control", usage = "Enable the control of listeners via mqtt")
	public boolean control;

	@Option(name = "-standalone", usage = "Start a mqtt server on this host")
	public boolean standalone;

	CommandLineArguments normalize() {
		if (brokerTopic != null && !brokerTopic.endsWith("/")) {
			brokerTopic = brokerTopic + '/';
		}
		return this;
	}

}