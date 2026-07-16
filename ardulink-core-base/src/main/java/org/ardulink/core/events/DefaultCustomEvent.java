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
package org.ardulink.core.events;

import static org.ardulink.util.anno.LapsedWith.JDK14;

import java.util.Objects;

import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@LapsedWith(value = JDK14, module = "records")
public final class DefaultCustomEvent implements CustomEvent {

	private final String message;

	public DefaultCustomEvent(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public int hashCode() {
		return Objects.hash(message);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultCustomEvent other = (DefaultCustomEvent) obj;
		return Objects.equals(message, other.message);
	}

	@Override
	public String toString() {
		return "DefaultCustomEvent [message=" + message + "]";
	}

}
