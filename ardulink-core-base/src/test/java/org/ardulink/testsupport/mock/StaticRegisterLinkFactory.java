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

package org.ardulink.testsupport.mock;

import static java.util.UUID.randomUUID;
import static org.ardulink.util.Preconditions.checkState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.testsupport.mock.StaticRegisterLinkFactory.StaticRegisterLinkConfig;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class StaticRegisterLinkFactory implements LinkFactory<StaticRegisterLinkConfig> {

	public static final String NAME = "staticregistry";

	public static class StaticRegisterLinkConfig implements LinkConfig {
		public static final String ATTRIBUTE_IDENTIFIER = "identifier";
		@Named(ATTRIBUTE_IDENTIFIER)
		public String identifier;
	}

	private static final ConcurrentMap<String, Link> links = new ConcurrentHashMap<>();

	public static final class Registration implements AutoCloseable {

		private final String identifier;

		public Registration() {
			this(randomUUID().toString());
		}

		public Registration(String identifier) {
			this.identifier = identifier;
		}

		public String ardulinkUri() {
			return String.format("ardulink://%s?%s=%s", StaticRegisterLinkFactory.NAME,
					StaticRegisterLinkConfig.ATTRIBUTE_IDENTIFIER, identifier);
		}

		@Override
		public void close() throws Exception {
			deregister(identifier);
		}

	}

	public static Registration register(Link link) {
		Registration registration = new Registration();
		String identifier = registration.identifier;
		checkState(links.putIfAbsent(identifier, link) == null, "Identifier %s already taken", identifier);
		return registration;
	}

	public static void deregister(String identifier) {
		checkState(links.remove(identifier) != null, "Identifier %s was not taken", identifier);
	}

	@Override
	public String getName() {
		return StaticRegisterLinkFactory.NAME;
	}

	@Override
	public Link newLink(StaticRegisterLinkConfig config) {
		return links.get(config.identifier);
	}

	@Override
	public StaticRegisterLinkConfig newLinkConfig() {
		return new StaticRegisterLinkConfig();
	}

}
