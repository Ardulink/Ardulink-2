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

package org.ardulink.core.qos;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.util.anno.LapsedWith.JDK8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.Tone;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.util.anno.LapsedWith;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.rules.Timeout;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class QosLinkTest {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public Arduino arduino = Arduino.newArduino();

	private final ByteStreamProcessor byteStreamProcessor = new ArdulinkProtocol2()
			.newByteStreamProcessor(Protocol.NULL_OUTPUTSTREAM);
	private QosLink qosLink;

	@After
	public void tearDown() throws IOException {
		qosLink.close();
	}

	@Test
	public void canDoGuranteedDelivery() throws Exception {
		arduino.whenReceive(regex(lf("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))).thenRespond(lf("alp://rply/ok?id=%s"));
		qosLink = newQosLink(connectionTo(arduino), 15, MINUTES);
		qosLink.sendNoTone(analogPin(3));
	}

	@Test
	public void doesThrowExceptionIfNotResponseReceivedWithinHalfAsecond() throws Exception {
		arduino.whenReceive(regex(lf("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))).thenDoNotRespond();
		qosLink = newQosLink(connectionTo(arduino), 500, MILLISECONDS);
		@LapsedWith(module = JDK8, value = "Lambda")
		IllegalStateException exception = assertThrows(IllegalStateException.class, new ThrowingRunnable() {
			@Override
			public void run() throws IOException {
				qosLink.sendNoTone(analogPin(3));
			}
		});
		assertThat(exception.getMessage(), is(allOf(containsString("response"), containsString("500 MILLISECONDS"))));
	}

	@Test
	public void doesThrowExceptionIfKoResponse() throws Exception {
		arduino.whenReceive(regex(lf("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))).thenRespond(lf("alp://rply/ko?id=%s"));
		Connection connection = connectionTo(arduino);
		qosLink = newQosLink(connection, 500 + someMillisMore(), MILLISECONDS);
		@LapsedWith(module = JDK8, value = "Lambda")
		IllegalStateException exception = assertThrows(IllegalStateException.class, new ThrowingRunnable() {
			@Override
			public void run() throws IOException {
				qosLink.sendNoTone(analogPin(3));
			}
		});
		assertThat(exception.getMessage(), is(allOf(containsString("status"), containsString("not ok"))));
	}

	@Test
	public void secondCallPassesIfFirstOneKeepsUnresponded() throws Exception {
		arduino.whenReceive(regex(lf("alp:\\/\\/tone\\/1\\/2\\/3\\?id\\=(\\d)"))).thenDoNotRespond();
		arduino.whenReceive(regex(lf("alp:\\/\\/tone\\/4\\/5\\/6\\?id\\=(\\d)"))).thenRespond(lf("alp://rply/ok?id=%s"));
		qosLink = newQosLink(connectionTo(arduino), 500, MILLISECONDS);
		try {
			qosLink.sendTone(Tone.forPin(analogPin(1)).withHertz(2).withDuration(3, MILLISECONDS));
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(), containsString("No response"));
		}
		qosLink.sendTone(Tone.forPin(analogPin(4)).withHertz(5).withDuration(6, MILLISECONDS));
	}

	private QosLink newQosLink(Connection connection, int timeout, TimeUnit timeUnit) throws IOException {
		return new QosLink(new ConnectionBasedLink(connection, byteStreamProcessor), timeout, timeUnit);
	}

	private StreamConnection connectionTo(Arduino arduino) {
		return new StreamConnection(arduino.getInputStream(), arduino.getOutputStream(), byteStreamProcessor);
	}

	private Pattern regex(String regex) {
		return Pattern.compile(regex);
	}

	private static int someMillisMore() {
		return 250;
	}

	private static String lf(String string) {
		return string + "\n";
	}

}
