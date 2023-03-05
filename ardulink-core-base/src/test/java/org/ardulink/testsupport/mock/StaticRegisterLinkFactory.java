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

	public static String register(Link link) {
		return register(randomUUID().toString(), link);
	}

	public static String register(String identifier, Link link) {
		checkState(links.putIfAbsent(identifier, link) == null, "Identifier %s already taken", identifier);
		return identifier;
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

	public static String ardulinkUri(String identifier) {
		return String.format("ardulink://%s?%s=%s", StaticRegisterLinkFactory.NAME,
				StaticRegisterLinkConfig.ATTRIBUTE_IDENTIFIER, identifier);
	}

}
