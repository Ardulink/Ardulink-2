package com.github.pfichtner.ardulink.core;

import static com.github.pfichtner.ardulink.core.AbstractConnectionBasedLink.Mode.READY_MESSAGE_ONLY;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol2;
import com.github.pfichtner.ardulink.core.qos.Arduino;

public class WaitForArduinoToBootTest {

	private static final Protocol proto = ArdulinkProtocol2.instance();

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public Arduino arduino = Arduino.newArduino();

	private final ConnectionBasedLink link = new ConnectionBasedLink(
			new StreamConnection(arduino.getInputStream(),
					arduino.getOutputStream(), proto), proto);

	@After
	public void tearDown() throws IOException {
		this.link.close();
	}

	@Test
	public void ifNoResponseReceivedWithin1SecondWaitWillReturnFalse()
			throws IOException {
		arduino.whenReceive(regex("alp:\\/\\/notn\\/0\\?id\\=(\\d)"))
				.thenDoNotRespond();
		assertThat(link.waitForArduinoToBoot(1, SECONDS), is(false));
	}

	@Test
	public void noNeedToWaitIfArduinoResponds() throws IOException {
		arduino.whenReceive(regex("alp:\\/\\/notn\\/0\\?id\\=(\\d)"))
				.thenRespond("alp://rply/ok?id=%s");
		assertThat(link.waitForArduinoToBoot(3, DAYS), is(true));
	}

	@Test
	public void canDetectReadyPaket() throws IOException {
		arduino.after(1, SECONDS).send("alp://ready/");
		assertThat(link.waitForArduinoToBoot(3, DAYS, READY_MESSAGE_ONLY),
				is(true));
	}

	@Test
	public void ignoresMisformedReadyPaket() throws IOException {
		arduino.after(1, SECONDS).send("alp://XXXXXreadyXXXXX/");
		assertThat(link.waitForArduinoToBoot(3, SECONDS, READY_MESSAGE_ONLY),
				is(false));
	}

	@Test
	public void detectAlreadySentReadyPaket() throws IOException {
		arduino.send("alp://ready/");
		assertThat(link.waitForArduinoToBoot(3, DAYS, READY_MESSAGE_ONLY),
				is(true));
	}

	private Pattern regex(String regex) {
		return Pattern.compile(regex);
	}
}
