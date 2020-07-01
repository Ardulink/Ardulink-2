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
package org.ardulink.mqtt.util;

import static org.ardulink.util.anno.LapsedWith.JDK8;

import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@LapsedWith(module = JDK8, value = "Optional")
public final class Optionals {

	private Optionals() {
		super();
	}

	public static <T> org.ardulink.util.Optional<T> j2a(java.util.Optional<T> in) {
		return in.isPresent() //
				? org.ardulink.util.Optional.of(in.get()) //
				: org.ardulink.util.Optional.absent();
	}

	public static <T> java.util.Optional<T> a2j(org.ardulink.util.Optional<T> in) {
		return in.isPresent() //
				? java.util.Optional.of(in.get()) //
				: java.util.Optional.empty();
	}

}
