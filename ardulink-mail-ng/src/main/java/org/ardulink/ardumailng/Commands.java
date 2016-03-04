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

package org.ardulink.ardumailng;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;

import java.net.URISyntaxException;

import com.github.pfichtner.ardulink.core.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class Commands {

	private static class SwitchDigitalPinCommand implements Command {

		private final int pin;
		private final boolean value;

		public SwitchDigitalPinCommand(int pin, boolean value) {
			this.pin = pin;
			this.value = value;
		}

		@Override
		public void execute(Link link) throws URISyntaxException, Exception {
			link.switchDigitalPin(digitalPin(pin), value);
		}

		@Override
		public String toString() {
			return "SwitchDigitalPinCommand [pin=" + pin + ", value=" + value
					+ "]";
		}

	}

	private static class SwitchAnalogPinCommand implements Command {

		private final int pin;
		private final int value;

		public SwitchAnalogPinCommand(int pin, int value) {
			this.pin = pin;
			this.value = value;
		}

		@Override
		public void execute(Link link) throws URISyntaxException, Exception {
			link.switchAnalogPin(analogPin(pin), value);
		}

		@Override
		public String toString() {
			return "SwitchAnalogPinCommand [pin=" + pin + ", value=" + value
					+ "]";
		}

	}

	public static Command switchDigitalPin(int pin, boolean value) {
		return new SwitchDigitalPinCommand(pin, value);
	}

	public static Command switchAnalogPin(int pin, int value) {
		return new SwitchAnalogPinCommand(pin, value);
	}

}
