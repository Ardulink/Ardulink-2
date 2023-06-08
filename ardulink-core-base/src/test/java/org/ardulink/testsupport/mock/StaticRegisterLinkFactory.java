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
import static org.ardulink.util.Preconditions.checkNotNull;
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

	public static final class Registration implements AutoCloseable {

		private static final ConcurrentMap<String, Registration> instances = new ConcurrentHashMap<>();

		private final Link link;
		private final String identifier;

		public Registration(Link link) {
			this(link, randomUUID().toString());
		}

		public Registration(Link link, String identifier) {
			this.link = link;
			this.identifier = identifier;
			checkState(instances.putIfAbsent(identifier, this) == null, "Identifier %s already taken", identifier);
		}

		@Override
		public void close() throws Exception {
			checkState(instances.remove(identifier) != null, "Identifier %s was not taken", identifier);
		}

		public String ardulinkUri() {
			return String.format("ardulink://%s?%s=%s", StaticRegisterLinkFactory.NAME,
					StaticRegisterLinkConfig.ATTRIBUTE_IDENTIFIER, identifier);
		}

		@SuppressWarnings("resource")
		private static Link linkByIdentifier(String identifier) {
			return checkNotNull(instances.get(identifier), "No link with identifier %s registered", identifier).link;
		}

	}

	public static Registration register(Link link) {
		return new Registration(link);
	}

	@Override
	public String getName() {
		return StaticRegisterLinkFactory.NAME;
	}

	@Override
	public Link newLink(StaticRegisterLinkConfig config) {
		return Registration.linkByIdentifier(config.identifier);
	}

	@Override
	public StaticRegisterLinkConfig newLinkConfig() {
		return new StaticRegisterLinkConfig();
	}

}
