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
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static org.ardulink.util.Iterables.getFirst;
import static org.ardulink.util.Lists.mapList;
import static org.ardulink.util.Predicates.attribute;
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
	 * List all registered protocols.
	 * 
	 * @return list of all registered protocols
	 * @see #protocolNames()
	 */
	public static List<Protocol> protocols() {
		return services(Protocol.class).filter(Protocol::isActive).collect(toList());
	}

	/**
	 * List all registered protocols names.
	 * 
	 * @return list of all registered protocols names
	 * @see #protocols()
	 */
	public static List<String> protocolNames() {
		return extractNames(protocols());
	}

	/**
	 * Returns the protocol with the given name. If no protocol with the given name
	 * is registered a {@link RuntimeException} is thrown.
	 * 
	 * @param name the name of the protocol
	 * @return protocol with the given name
	 * @see #tryProtoByName(String)
	 */
	public static Protocol protoByName(String name) {
		List<Protocol> availables = protocols();
		return withName(availables, name).orElseThrow(() -> new IllegalStateException(
				format("No protocol with name %s registered. Available names are %s", name, extractNames(availables))));
	}

	/**
	 * Returns an {@link Optional} of the protocol with the given name. If no
	 * protocol with the given name is registered the {@link Optional} is empty.
	 * 
	 * @param name the name of the protocol
	 * @return Optional holding the protocol with the given name or empty if not
	 *         found
	 */
	public static Optional<Protocol> tryProtoByName(String name) {
		return withName(protocols(), name);
	}

	/**
	 * Tries to load the passed protocol, if this fails, the first available
	 * protocol is returned. Since there could be no available protocols an
	 * {@link Optional} is returned.
	 * 
	 * @param name the name of the protocol
	 * @return Optional holding the protocol with the given name or empty if not
	 *         found
	 */
	public static Optional<Protocol> tryProtoByNameWithFallback(String name) {
		return tryProtoByName(name).or(() -> getFirst(protocols()));
	}

	private static Optional<Protocol> withName(List<Protocol> protocols, String name) {
		return protocols.stream().filter(attribute(Protocol::getName, isEqual(name))).findFirst();
	}

	private static List<String> extractNames(List<Protocol> protocols) {
		return mapList(protocols, Protocol::getName);
	}

}
