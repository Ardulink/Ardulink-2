package com.github.pfichtner.ardulink.util;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.dna.mqtt.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.zu.ardulink.connection.Connection;
import org.zu.ardulink.connection.ConnectionContact;
import org.zu.ardulink.connection.serial.AbstractSerialConnection;

import com.github.pfichtner.ardulink.MqttMain;

public final class TestUtil {

	private TestUtil() {
		super();
	}

	public static Connection createConnection(final OutputStream outputStream,
			ConnectionContact connectionContact) {
		return new AbstractSerialConnection(connectionContact) {

			{
				setOutputStream(outputStream);
			}

			@Override
			public List<String> getPortList() {
				return singletonList("/dev/null");
			}

			@Override
			public boolean disconnect() {
				setConnected(false);
				return isConnected();
			}

			@Override
			public boolean connect(Object... params) {
				setConnected(true);
				return isConnected();
			}
		};
	}

	public static int[] toCodepoints(String message) {
		int[] codepoints = new int[message.length()];
		for (int i = 0; i < message.length(); i++) {
			codepoints[i] = message.codePointAt(i);
		}
		return codepoints;
	}

	public static Field getField(Object target, String fieldName) {
		try {
			return target.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(e);
		} catch (SecurityException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void set(Object target, Field field, Object instance) {
		field.setAccessible(true);
		try {
			field.set(target, instance);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Server startBroker() throws IOException, InterruptedException {
		Server broker = new Server();
		broker.startServer();
		return broker;
	}

	public static MqttMain startAsync(MqttMain mqttMain)
			throws InterruptedException, MqttSecurityException, MqttException {
		mqttMain.connectToMqttBroker();
		return waitUntilIsConnected(mqttMain, 250, MILLISECONDS);
	}

	private static MqttMain waitUntilIsConnected(MqttMain mqttMain, int value,
			TimeUnit timeUnit) throws InterruptedException {
		StopWatch stopWatch = new StopWatch().start();
		while (!mqttMain.isConnected()) {
			timeUnit.sleep(value);
			int secs = 5;
			if (stopWatch.getTime() > SECONDS.toMillis(secs)) {
				throw new IllegalStateException("Could not connect within "
						+ secs + " seconds");
			}
		}
		return mqttMain;
	}

}
