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

package org.ardulink.camel.test;

import static org.mockito.Mockito.mock;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MockLinkFactory implements LinkFactory<LinkConfig> {

	@Override
	public String getName() {
		return "mock";
	}

	@Override
	public Link newLink(LinkConfig config) {
		return mock(Link.class);
	}

	@Override
	public LinkConfig newLinkConfig() {
		return LinkConfig.NO_ATTRIBUTES;
	}

}
