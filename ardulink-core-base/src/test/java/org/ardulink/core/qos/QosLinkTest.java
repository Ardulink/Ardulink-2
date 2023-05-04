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

import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.Tone;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Timeout(15)
class QosLinkTest {

	@RegisterExtension
	ArduinoStub arduinoStub = ArduinoStub.newArduinoStub();

	ByteStreamProcessor byteStreamProcessor = new ArdulinkProtocol2().newByteStreamProcessor();
	QosLink qosLink;

	@AfterEach
	void tearDown() throws IOException {
		if (qosLink != null) {
			qosLink.close();
		}
	}

	@Test
	void canDoGuranteedDelivery() throws Exception {
		arduinoStub.onReceive(regex(lf("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))).respondWith(lf("alp://rply/ok?id=%s"));
		qosLink = newQosLink(MAX_VALUE, DAYS);
		AnalogPin pin = analogPin(3);
		assertThat(qosLink.sendNoTone(pin)).isEqualTo(1);
		assertThat(qosLink.sendNoTone(pin)).isEqualTo(2);
		assertThat(qosLink.sendNoTone(pin)).isEqualTo(3);
	}

	@Test
	void doesThrowExceptionIfNotResponseReceivedWithinHalfAsecond() throws Exception {
		arduinoStub.onReceive(regex(lf("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))).doNotRespond();
		qosLink = newQosLink(500, MILLISECONDS);
		assertThat(assertThrows(IllegalStateException.class, () -> qosLink.sendNoTone(analogPin(3))))
				.hasMessageContaining("response").hasMessageContaining("500 MILLISECONDS");
	}

	@Test
	void doesThrowExceptionIfKoResponse() throws Exception {
		arduinoStub.onReceive(regex(lf("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))).respondWith(lf("alp://rply/ko?id=%s"));
		qosLink = newQosLink(500 + someMillisMore(), MILLISECONDS);
		IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> qosLink.sendNoTone(analogPin(3)));
		assertThat(exception).hasMessageContaining("status").hasMessageContaining("not ok");
	}

	@RepeatedTest(100)
	void secondCallPassesIfFirstOneKeepsUnresponded() throws Exception {
		arduinoStub.onReceive(regex(lf("alp:\\/\\/tone\\/1\\/2\\/3\\?id\\=(\\d)"))).doNotRespond();
		arduinoStub.onReceive(regex(lf("alp:\\/\\/tone\\/4\\/5\\/6\\?id\\=(\\d)")))
				.respondWith(lf("alp://rply/ok?id=%s"));
		qosLink = newQosLink(500, MILLISECONDS);
		assertThat(assertThrows(IllegalStateException.class,
				() -> qosLink.sendTone(Tone.forPin(analogPin(1)).withHertz(2).withDuration(3, MILLISECONDS))))
				.hasMessageContaining("No response");
		qosLink.sendTone(Tone.forPin(analogPin(4)).withHertz(5).withDuration(6, MILLISECONDS));
	}

	private QosLink newQosLink(long timeout, TimeUnit timeUnit) throws IOException {
		return new QosLink(new ConnectionBasedLink(connectionTo(arduinoStub), byteStreamProcessor), timeout, timeUnit);
	}

	private StreamConnection connectionTo(ArduinoStub arduino) {
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
