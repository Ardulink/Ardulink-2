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

import static org.mockito.Mockito.spy;

import org.ardulink.core.ConnectionBasedLink;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DummyLinkFactory implements LinkFactory<DummyLinkConfig> {

	static final String DUMMY_LINK_NAME = "dummyLink";

	@Override
	public String getName() {
		return DUMMY_LINK_NAME;
	}

	@Override
	public ConnectionBasedLink newLink(DummyLinkConfig config) {
		return spy(new ConnectionBasedLink(spy(new DummyConnection(config)), config.protocol.newByteStreamProcessor()));
	}

	@Override
	public DummyLinkConfig newLinkConfig() {
		return new DummyLinkConfig();
	}

}
