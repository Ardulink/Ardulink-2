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

import static java.lang.Long.MAX_VALUE;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.util.Regex.regex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Tone;
import org.ardulink.testsupport.junit5.ArduinoStubExt;
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
@Timeout(value = 15, unit = SECONDS)
class QosLinkTest {

	@RegisterExtension
	ArduinoStubExt arduinoStub = new ArduinoStubExt();

	@Test
	void canDoGuranteedDelivery() throws Exception {
		arduinoStub.onReceive(regex(lf("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))).respondWith(lf("alp://rply/ok?id={0}"));
		try (QosLink qosLink = newQosLink(MAX_VALUE, DAYS)) {
			AnalogPin pin = analogPin(3);
			assertThat(qosLink.sendNoTone(pin)).isEqualTo(1);
			assertThat(qosLink.sendNoTone(pin)).isEqualTo(2);
			assertThat(qosLink.sendNoTone(pin)).isEqualTo(3);
		}
	}

	@Test
	void doesThrowExceptionIfNotResponseReceivedWithinHalfAsecond() throws Exception {
		arduinoStub.onReceive(regex(lf("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))).doNotRespond();
		try (QosLink qosLink = newQosLink(500, MILLISECONDS)) {
			assertThatIllegalStateException().isThrownBy(() -> qosLink.sendNoTone(analogPin(3)))
					.withMessageContainingAll("response", "500 MILLISECONDS");
		}
	}

	@Test
	void doesThrowExceptionIfKoResponse() throws Exception {
		arduinoStub.onReceive(regex(lf("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))).respondWith(lf("alp://rply/ko?id={0}"));
		try (QosLink qosLink = newQosLink(MAX_VALUE, DAYS)) {
			assertThatIllegalStateException().isThrownBy(() -> qosLink.sendNoTone(analogPin(3)))
					.withMessageContainingAll("status", "not ok");
		}
	}

	@Test
	void secondCallPassesIfFirstOneKeepsUnresponded() throws Exception {
		arduinoStub.onReceive(regex(lf("alp:\\/\\/tone\\/1\\/2\\/3\\?id\\=(\\d)"))).doNotRespond();
		arduinoStub.onReceive(regex(lf("alp:\\/\\/tone\\/4\\/5\\/6\\?id\\=(\\d)")))
				.respondWith(lf("alp://rply/ok?id={0}"));
		try (QosLink qosLink = newQosLink(500, MILLISECONDS)) {
			assertThatIllegalStateException().isThrownBy(
					() -> qosLink.sendTone(Tone.forPin(analogPin(1)).withHertz(2).withDuration(3, MILLISECONDS)))
					.withMessageContaining("No response");
			qosLink.sendTone(Tone.forPin(analogPin(4)).withHertz(5).withDuration(6, MILLISECONDS));
		}
	}

	private QosLink newQosLink(long timeout, TimeUnit timeUnit) throws IOException {
		return new QosLink(arduinoStub.link(), timeout, timeUnit);
	}

	private static String lf(String string) {
		return string + "\n";
	}

}
