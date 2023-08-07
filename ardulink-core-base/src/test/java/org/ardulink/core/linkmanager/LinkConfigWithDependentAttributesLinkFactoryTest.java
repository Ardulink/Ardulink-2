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

package org.ardulink.core.linkmanager;

import static java.lang.Integer.valueOf;
import static java.net.URI.create;
import static java.util.Optional.ofNullable;
import static org.ardulink.core.linkmanager.providers.DynamicLinkFactoriesProvider.withRegistered;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.ardulink.core.Link;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class LinkConfigWithDependentAttributesLinkFactoryTest {

	public static class LinkConfigWithDependentAttributes implements LinkConfig {

		@Named("host")
		private String host;
		@Named("port")
		private Integer port;
		@Named("devicePort")
		private String devicePort;

		@ChoiceFor("devicePort")
		public String[] availableDevicePort() {
			checkNotNull(host, "host must not be null");
			checkNotNull(port, "port must not be null");
			return new String[] { "foo", "bar" };
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return ofNullable(port).orElse(valueOf(1)).intValue();
		}

		public void setPort(int port) {
			this.port = valueOf(port);
		}

		public String getDevicePort() {
			return devicePort;
		}

		public void setDevicePort(String devicePort) {
			this.devicePort = devicePort;
		}

	}

	public static class LinkConfigWithDependentAttributesLinkFactory
			implements LinkFactory<LinkConfigWithDependentAttributes> {

		@Override
		public String getName() {
			return "dependendAttributes";
		}

		@Override
		public Link newLink(LinkConfigWithDependentAttributes config) {
			return mock(Link.class);
		}

		@Override
		public LinkConfigWithDependentAttributes newLinkConfig() {
			return new LinkConfigWithDependentAttributes();
		}

	}

	@Test
	void canInstantiateLinkWithDependentAttributes() throws Throwable {
		withRegistered(new LinkConfigWithDependentAttributesLinkFactory()).execute(() -> {
			try (Link link = LinkManager.getInstance()
					.getConfigurer(create("ardulink://dependendAttributes?devicePort=foo&host=h&port=1")).newLink()) {
				assertThat(link).isNotNull();
			}
		});
	}

}
