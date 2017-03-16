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
package org.ardulink.util;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Preconditions {

	private Preconditions() {
		super();
	}

	public static <T> T checkNotNull(T t, String message, Object... args) {
		if (t == null) {
			throw new IllegalStateException(String.format(message, args));
		}
		return t;
	}

	public static void checkArgument(boolean state, String message, Object... args) {
		if (!state) {
			throw new IllegalArgumentException(String.format(message, args));
		}
	}

	public static void checkState(boolean state, String message, Object... args) {
		if (!state) {
			throw new IllegalStateException(String.format(message, args));
		}
	}

}
