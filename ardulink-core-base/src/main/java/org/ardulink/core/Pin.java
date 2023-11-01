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

import java.util.Objects;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class Pin {

	public enum Type {
		ANALOG, DIGITAL
	}

	private final int num;
	private final Type type;

	protected Pin(int num, Type type) {
		this.num = num;
		this.type = type;
	}

	public int pinNum() {
		return num;
	}

	public Type getType() {
		return type;
	}

	@Override
	public final int hashCode() {
		return Objects.hash(type, num);
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Pin))
			return false;
		Pin other = (Pin) obj;
		return type == other.type && num == other.num;
	}

	public static class AnalogPin extends Pin {

		private AnalogPin(int num) {
			super(num, Type.ANALOG);
		}

	}

	public static class DigitalPin extends Pin {

		private DigitalPin(int num) {
			super(num, Type.DIGITAL);
		}

	}

	public static AnalogPin analogPin(int num) {
		return new AnalogPin(num);
	}

	public static DigitalPin digitalPin(int num) {
		return new DigitalPin(num);
	}

	public boolean is(Type type) {
		return getType() == type;
	}

	@Override
	public String toString() {
		return getType() + " " + pinNum();
	}

}
