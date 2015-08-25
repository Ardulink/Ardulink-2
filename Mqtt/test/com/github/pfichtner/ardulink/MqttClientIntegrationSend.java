package com.github.pfichtner.ardulink;

import static com.github.pfichtner.ardulink.util.ProtoBuilder.alpProtocolMessage;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.ANALOG_PIN_READ;
import static com.github.pfichtner.ardulink.util.ProtoBuilder.ALPProtocolKeys.DIGITAL_PIN_READ;
import static com.github.pfichtner.ardulink.util.TestUtil.createConnection;
import static com.github.pfichtner.ardulink.util.TestUtil.getField;
import static com.github.pfichtner.ardulink.util.TestUtil.set;
import static com.github.pfichtner.ardulink.util.TestUtil.toCodepoints;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zu.ardulink.ConnectionContactImpl;
import org.zu.ardulink.Link;
import org.zu.ardulink.connection.Connection;
import org.zu.ardulink.connection.ConnectionContact;

import com.github.pfichtner.ardulink.util.AnotherMqttClient;
import com.github.pfichtner.ardulink.util.MainStarter;
import com.github.pfichtner.ardulink.util.MqttMessageBuilder;
import com.github.pfichtner.ardulink.util.TestUtil;

public class MqttClientIntegrationSend {

	private static final long TIMEOUT = 10 * 1000;;

	private static final String TOPIC = "foo/bar";

	private static final String LINKNAME = "testlink";

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	private final ConnectionContact connectionContact = new ConnectionContactImpl(
			null);

	private final Connection connection = createConnection(outputStream,
			connectionContact);

	private final Link link = Link.createInstance(LINKNAME, connection);

	{
		// there is an extremely high coupling of ConnectionContactImpl and Link
		// which can not be solved other than injecting the variables through
		// reflection
		set(connectionContact, getField(connectionContact, "link"), link);
		set(link, getField(link, "connectionContact"), connectionContact);

	}

	private MqttMain client = new MqttMain() {
		{
			setBrokerTopic(TOPIC);
		}

		@Override
		protected Link createLink() {
			return link;
		}
	};

	private Server broker;
	private AnotherMqttClient amc;
	private MainStarter starter;

	@Before
	public void setup() throws IOException, InterruptedException,
			MqttSecurityException, MqttException {
		broker = TestUtil.startBroker();
		amc = new AnotherMqttClient(TOPIC).connect();
		starter = new MainStarter(client);
	}

	@After
	public void tearDown() throws InterruptedException, MqttException {
		client.close();
		amc.disconnect();
		broker.stopServer();
	}

	@Test(timeout = TIMEOUT)
	public void generatesBrokerEventOnDigitalPinChange()
			throws InterruptedException, MqttSecurityException, MqttException,
			IOException {

		int pin = 1;
		client.setAnalogs();
		client.setDigitals(pin);

		starter.startAsync();
		simulateArduinoToMqtt(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin)
				.withValue(1));

		tearDown();

		assertThat(starter.getExceptions().isEmpty(), is(true));
		assertThat(
				amc.hasReceived(),
				is(Collections.singletonList(MqttMessageBuilder
						.mqttMessageWithBasicTopic(TOPIC).forDigitalPin(pin)
						.withValue(1).createGetMessage())));
	}

	@Test(timeout = TIMEOUT)
	public void generatesBrokerEventOnAnalogPinChange()
			throws InterruptedException, MqttSecurityException, MqttException,
			IOException {

		int pin = 1;
		int value = 45;
		client.setAnalogs(pin);
		client.setDigitals();

		starter.startAsync();
		simulateArduinoToMqtt(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin)
				.withValue(value));

		tearDown();

		assertThat(starter.getExceptions().isEmpty(), is(true));
		assertThat(
				amc.hasReceived(),
				is(Collections.singletonList(MqttMessageBuilder
						.mqttMessageWithBasicTopic(TOPIC).forAnalogPin(pin)
						.withValue(value).createGetMessage())));
	}

	private void simulateArduinoToMqtt(String message) {
		int[] codepoints = toCodepoints(message);
		connectionContact.parseInput("someId", codepoints.length, codepoints);
	}

}
