package com.github.pfichtner.ardulink;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zu.ardulink.Link;

import com.github.pfichtner.ardulink.util.AnotherMqttClient;
import com.github.pfichtner.ardulink.util.MainStarter;
import com.github.pfichtner.ardulink.util.TestUtil;

public class MqttClientIntegrationReceive {

	private static final long TIMEOUT = 10 * 1000;;

	private static final String TOPIC = "foo/bar";

	private final Link mock = mock(Link.class);
	{
		when(mock.getPortList()).thenReturn(singletonList("/dev/null"));
		when(mock.connect("/dev/null", 115200)).thenReturn(true);
	}

	private MqttMain client = new MqttMain() {
		{
			setBrokerTopic(TOPIC);
		}

		@Override
		protected Link createLink() {
			return mock;
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
	public void processesBrokerEventPowerOnDigitalPin()
			throws InterruptedException, MqttSecurityException, MqttException,
			IOException {

		int pin = 1;
		int value = 1;

		doNotListenForAnything(client);
		starter.startAsync();
		amc.switchDigitalPin(pin, true);

		MILLISECONDS.sleep(100);

		assertThat(starter.getExceptions().isEmpty(), is(true));
		verify(mock).getPortList();
		verify(mock).connect("/dev/null", 115200);
		verify(mock).sendPowerPinSwitch(pin, value);
		verifyNoMoreInteractions(mock);
	}

	@Test(timeout = TIMEOUT)
	public void processesBrokerEventPowerOnAnalogPin()
			throws InterruptedException, MqttSecurityException, MqttException,
			IOException {

		int pin = 1;
		int value = 123;

		doNotListenForAnything(client);
		starter.startAsync();
		amc.switchAnalogPin(pin, value);

		MILLISECONDS.sleep(100);

		assertThat(starter.getExceptions().isEmpty(), is(true));
		verify(mock).getPortList();
		verify(mock).connect("/dev/null", 115200);
		verify(mock).sendPowerPinIntensity(pin, value);
		verifyNoMoreInteractions(mock);
	}

	private static void doNotListenForAnything(MqttMain client) {
		client.setAnalogs();
		client.setDigitals();
	}

}
