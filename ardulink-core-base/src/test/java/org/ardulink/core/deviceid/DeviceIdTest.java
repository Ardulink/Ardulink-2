package org.ardulink.core.deviceid;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.ardulink.core.Connection;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.core.qos.Arduino;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

public class DeviceIdTest {

	private static class ConnectionReader {

		private final StringBuilder sb = new StringBuilder();

		public ConnectionReader(Connection connection) {
			connection.addListener(new Connection.ListenerAdapter() {
				@Override
				public void received(byte[] bytes) throws IOException {
					sb.append(new String(bytes));
				}
			});

		}

		public void assertReceived(String expected) throws InterruptedException {
			while (sb.length() < expected.length()) {
				TimeUnit.MILLISECONDS.sleep(50);
			}
			assertThat(sb.toString(), is(expected));
		}

	}

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public ExpectedException exceptions = ExpectedException.none();

	@Rule
	public Arduino arduino = Arduino.newArduino();

	@Test
	public void testName() throws Exception {
		arduino.whenReceive(regex("alp:\\/\\/deviceId")).thenRespond(
				"alp://foobar");
		Connection connection = connectionTo(arduino);
		try {
			connection.write("alp://deviceId\n".getBytes());
			readOn(connection).assertReceived("alp://foobar");
		} finally {
			connection.close();
		}

	}

	private ConnectionReader readOn(Connection connection) {
		return new ConnectionReader(connection);
	}

	private StreamConnection connectionTo(Arduino arduino) {
		return new StreamConnection(arduino.getInputStream(),
				arduino.getOutputStream(), ArdulinkProtocol2.instance());
	}

	private Pattern regex(String regex) {
		return Pattern.compile(regex);
	}

}
