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

package org.ardulink.core;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.ConnectionBasedLinkNG.Mode.READY_MESSAGE_ONLY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.util.regex.Pattern;

import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.core.qos.Arduino;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class WaitForArduinoToBootTest {

	private static final ByteStreamProcessor byteStreamProcessor = new ArdulinkProtocol2().newByteStreamProcessor();

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public Arduino arduino = Arduino.newArduino();

	private final ConnectionBasedLinkNG link = new ConnectionBasedLinkNG(
			new StreamConnection(arduino.getInputStream(), arduino.getOutputStream(), byteStreamProcessor),
			byteStreamProcessor);

	@After
	public void tearDown() throws IOException {
		this.link.close();
	}

	@Test
	public void ifNoResponseReceivedWithin1SecondWaitWillReturnFalse() throws IOException {
		arduino.whenReceive(regex(lf("alp:\\/\\/notn\\/0\\?id\\=(\\d)"))).thenDoNotRespond();
		assertThat(link.waitForArduinoToBoot(1, SECONDS), is(false));
	}

	@Test
	public void noNeedToWaitIfArduinoResponds() throws IOException {
		arduino.whenReceive(regex(lf("alp:\\/\\/notn\\/0\\?id\\=(\\d)"))).thenRespond(lf("alp://rply/ok?id=%s"));
		assertThat(link.waitForArduinoToBoot(3, DAYS), is(true));
	}

	@Test
	public void canDetectReadyPaket() throws IOException {
		arduino.after(1, SECONDS).send(lf("alp://ready/"));
		assertThat(link.waitForArduinoToBoot(3, DAYS, READY_MESSAGE_ONLY), is(true));
	}

	@Test
	public void ignoresMisformedReadyPaket() throws IOException {
		arduino.after(1, SECONDS).send(lf("alp://XXXXXreadyXXXXX/"));
		assertThat(link.waitForArduinoToBoot(3, SECONDS, READY_MESSAGE_ONLY), is(false));
	}

	@Test
	public void detectAlreadySentReadyPaket() throws IOException {
		arduino.send(lf("alp://ready/"));
		assertThat(link.waitForArduinoToBoot(3, DAYS, READY_MESSAGE_ONLY), is(true));
	}

	private Pattern regex(String regex) {
		return Pattern.compile(regex);
	}

	private static String lf(String string) {
		return string + "\n";
	}

}
