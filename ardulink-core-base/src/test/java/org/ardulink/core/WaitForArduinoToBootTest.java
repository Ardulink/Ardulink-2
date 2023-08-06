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

import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.ConnectionBasedLink.Mode.READY_MESSAGE_ONLY;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.regex.Pattern;

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
@Timeout(value = 5, unit = SECONDS)
class WaitForArduinoToBootTest {

	@RegisterExtension
	ArduinoStubExt arduinoStub = new ArduinoStubExt();

	@Test
	void ifNoResponseReceivedWithin1SecondWaitWillReturnFalse() throws IOException {
		arduinoStub.onReceive(regex(lf("alp:\\/\\/notn\\/0\\?id\\=(\\d)"))).doNotRespond();
		assertThat(arduinoStub.link().waitForArduinoToBoot(1, SECONDS)).isFalse();
	}

	@Test
	void noNeedToWaitIfArduinoDoesRespond() throws IOException {
		arduinoStub.onReceive(regex(lf("alp:\\/\\/notn\\/0\\?id\\=(\\d)"))).respondWith(lf("alp://rply/ok?id=%s"));
		assertThat(arduinoStub.link().waitForArduinoToBoot(MAX_VALUE, DAYS)).isTrue();
	}

	@Test
	void canDetectReadyPaket() throws IOException {
		arduinoStub.after(1, SECONDS).send(lf("alp://ready/"));
		assertThat(arduinoStub.link().waitForArduinoToBoot(MAX_VALUE, DAYS, READY_MESSAGE_ONLY)).isTrue();
	}

	@Test
	void ignoresMisformedReadyPaket() throws IOException {
		arduinoStub.after(1, SECONDS).send(lf("alp://XXXXXreadyXXXXX/"));
		assertThat(arduinoStub.link().waitForArduinoToBoot(3, SECONDS, READY_MESSAGE_ONLY)).isFalse();
	}

	@Test
	void detectAlreadySentReadyPaket() throws IOException {
		arduinoStub.send(lf("alp://ready/"));
		assertThat(arduinoStub.link().waitForArduinoToBoot(MAX_VALUE, DAYS, READY_MESSAGE_ONLY)).isTrue();
	}

	private Pattern regex(String regex) {
		return Pattern.compile(regex);
	}

	private static String lf(String string) {
		return string + "\n";
	}

}
