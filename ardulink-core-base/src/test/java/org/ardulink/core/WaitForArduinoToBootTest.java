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
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.ConnectionBasedLink.Mode.READY_MESSAGE_ONLY;
import static org.ardulink.util.Regex.regex;
import static org.ardulink.util.Throwables.propagate;
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

	Pattern noToneRegex = regex(lf("alp:\\/\\/notn\\/0\\?id\\=(\\d)"));

	@RegisterExtension
	ArduinoStubExt arduinoStub = new ArduinoStubExt();

	@Test
	void ifNoResponseReceivedWithin3SecondsWaitWillReturnFalse() throws IOException {
		arduinoStub.onReceive(noToneRegex).doNotRespond();
		assertThat(arduinoStub.link().waitForArduinoToBoot(3, SECONDS)).isFalse();
	}

	@Test
	void noNeedToWaitIfArduinoDoesRespond() throws IOException {
		arduinoStub.onReceive(noToneRegex).respondWith(lf("alp://rply/ok?id={0}"));
		assertThat(arduinoStub.link().waitForArduinoToBoot(MAX_VALUE, DAYS)).isTrue();
	}

	@Test
	void canDetectReadyPaket() throws IOException {
		simulateArduinoSendsInOneSecond(lf("alp://ready/"));
		assertThat(arduinoStub.link().waitForArduinoToBoot(MAX_VALUE, DAYS, READY_MESSAGE_ONLY)).isTrue();
	}

	@Test
	void ignoresMisformedReadyPaket() throws IOException {
		simulateArduinoSendsInOneSecond(lf("alp://readyX/"));
		assertThat(arduinoStub.link().waitForArduinoToBoot(3, SECONDS, READY_MESSAGE_ONLY)).isFalse();
	}

	private void simulateArduinoSendsInOneSecond(String message) {
		newSingleThreadExecutor().execute(() -> {
			try {
				SECONDS.sleep(1);
				arduinoStub.simulateArduinoSends(message);
			} catch (InterruptedException | IOException e) {
				throw propagate(e);
			}
		});
	}

	private static String lf(String string) {
		return string + "\n";
	}

}
