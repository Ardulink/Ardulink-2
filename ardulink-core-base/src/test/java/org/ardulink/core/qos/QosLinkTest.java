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

import static org.ardulink.core.Pin.analogPin;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import org.ardulink.core.Connection;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.Tone;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

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
	public ExpectedException exceptions = ExpectedException.none();

	@Rule
	public Arduino arduino = Arduino.newArduino();

	private ConnectionBasedQosLink qosLink;

	@After
	public void tearDown() throws IOException {
		qosLink.close();
	}

	@Test
	public void canDoGuranteedDelivery() throws Exception {
		arduino.whenReceive(regex("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))
				.thenRespond("alp://rply/ok?id=%s");
		qosLink = new ConnectionBasedQosLink(connectionTo(arduino),
				ArdulinkProtocol2.instance(), 15, MINUTES);
		qosLink.sendNoTone(analogPin(3));
	}

	@Test
	public void doesThrowExceptionIfNotResponseReceivedWithinHalfAsecond()
			throws Exception {
		arduino.whenReceive(regex("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))
				.thenDoNotRespond();
		qosLink = new ConnectionBasedQosLink(connectionTo(arduino),
				ArdulinkProtocol2.instance(), 500, MILLISECONDS);
		exceptions.expect(IllegalStateException.class);
		exceptions.expectMessage(allOf(containsString("response"),
				containsString("500 MILLISECONDS")));
		qosLink.sendNoTone(analogPin(3));
	}

	@Test
	public void doesThrowExceptionIfKoResponse() throws Exception {
		arduino.whenReceive(regex("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))
				.thenRespond("alp://rply/ko?id=%s");
		Connection connection = connectionTo(arduino);
		qosLink = new ConnectionBasedQosLink(connection,
				ArdulinkProtocol2.instance(), 750, MILLISECONDS);
		exceptions.expect(IllegalStateException.class);
		exceptions.expectMessage(allOf(containsString("status"),
				containsString("not ok")));
		qosLink.sendNoTone(analogPin(3));
	}

	@Test
	public void secondCallPassesIfFirstOnKeepsUnresponded() throws Exception {
		arduino.whenReceive(regex("alp:\\/\\/tone\\/1/2/3\\?id\\=(\\d)"))
				.thenDoNotRespond();
		arduino.whenReceive(regex("alp:\\/\\/tone\\/4/5/6\\?id\\=(\\d)"))
				.thenRespond("alp://rply/ok?id=%s");
		qosLink = new ConnectionBasedQosLink(connectionTo(arduino),
				ArdulinkProtocol2.instance(), 500, MILLISECONDS);
		try {
			qosLink.sendTone(Tone.forPin(analogPin(1)).withHertz(2)
					.withDuration(3, MILLISECONDS));
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(), containsString("No response"));
		}
		qosLink.sendTone(Tone.forPin(analogPin(4)).withHertz(5)
				.withDuration(6, MILLISECONDS));
	}

	private StreamConnection connectionTo(Arduino arduino) {
		return new StreamConnection(arduino.getInputStream(),
				arduino.getOutputStream(), ArdulinkProtocol2.instance());
	}

	private Pattern regex(String regex) {
		return Pattern.compile(regex);
	}

}
