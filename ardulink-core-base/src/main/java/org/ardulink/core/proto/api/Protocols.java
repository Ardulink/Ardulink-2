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

package org.ardulink.core.proto.api;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.ardulink.util.ServiceLoaders.services;

import java.util.List;
import java.util.Optional;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class Protocols {

	private Protocols() {
		super();
	}

	/**
	 * Returns the protocol with the given name. If no protocol with the given name
	 * is registered a {@link RuntimeException} is thrown.
	 * 
	 * @param name the name of the protocol
	 * @return protocol with the given name
	 * @see #tryByName(String)
	 */
	public static Protocol getByName(String name) {
		return tryByName(name).orElseThrow(() -> new IllegalStateException(
				format("No protocol with name %s registered. Available names are %s", name, names())));
	}

	/**
	 * Returns an {@link Optional} of the protocol with the given name. If no
	 * protocol with the given name is registered the {@link Optional} is empty.
	 * 
	 * @param name the name of the protocol
	 * @return Optional holding the protocol with the given name or empty if not
	 *         found
	 */
	public static Optional<Protocol> tryByName(String name) {
		return list().stream().filter(p -> p.getName().equals(name)).findFirst();
	}

	/**
	 * List all registered protocols
	 * 
	 * @return list of all registered protocols
	 * @see #names()
	 */
	public static List<Protocol> list() {
		return services(Protocol.class);
	}

	/**
	 * List all registered protocols names
	 * 
	 * @return list of all registered protocols names
	 * @see #list()
	 */
	public static List<String> names() {
		return list().stream().map(Protocol::getName).collect(toList());
	}

}
