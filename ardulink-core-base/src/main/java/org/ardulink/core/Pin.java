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

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public abstract class Pin {

	public enum Type {
		ANALOG, DIGITAL
	}

	private final int num;

	protected Pin(int num) {
		this.num = num;
	}

	public int pinNum() {
		return num;
	}

	public abstract Type getType();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + num;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		Pin other = (Pin) obj;
		return num == other.num;
	}

	public static class AnalogPin extends Pin {
		private AnalogPin(int num) {
			super(num);
		}

		@Override
		public Type getType() {
			return Type.ANALOG;
		}
	}

	public static class DigitalPin extends Pin {
		private DigitalPin(int num) {
			super(num);
		}

		@Override
		public Type getType() {
			return Type.DIGITAL;
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
