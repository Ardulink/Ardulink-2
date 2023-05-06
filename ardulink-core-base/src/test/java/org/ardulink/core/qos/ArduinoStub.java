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

import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.ardulink.core.qos.ArduinoDouble.Adder;
import org.ardulink.core.qos.ArduinoDouble.RegexAdder;
import org.ardulink.core.qos.ArduinoDouble.ExecRunnableThenDoBuilder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArduinoStub implements AfterEachCallback {

	private final ArduinoDouble arduinoDouble;

	public static ArduinoStub newArduinoStub() {
		return new ArduinoStub();
	}

	public ArduinoStub() {
		this.arduinoDouble = createArduinoDouble();
	}

	private ArduinoDouble createArduinoDouble() {
		try {
			return new ArduinoDouble();
		} catch (IOException e) {
			throw propagate(e);
		}
	}
	
	@Override
	public void afterEach(ExtensionContext context) {
		try {
			this.arduinoDouble.close();
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	public Adder whenReceive(String string) {
		return arduinoDouble.whenReceive(string);
	}

	public RegexAdder onReceive(Pattern pattern) {
		return arduinoDouble.whenReceive(pattern);
	}

	public ExecRunnableThenDoBuilder after(int i, TimeUnit timeUnit) {
		return arduinoDouble.after(i, timeUnit);
	}

	public void send(String message) throws IOException {
		arduinoDouble.send(message);
	}

	public InputStream getInputStream() {
		return arduinoDouble.getInputStream();
	}

	public OutputStream getOutputStream() {
		return arduinoDouble.getOutputStream();
	}

}