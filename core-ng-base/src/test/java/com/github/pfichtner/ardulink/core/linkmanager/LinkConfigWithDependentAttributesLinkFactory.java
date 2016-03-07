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

package com.github.pfichtner.ardulink.core.linkmanager;

import java.io.IOException;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol2;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class LinkConfigWithDependentAttributesLinkFactory implements
		LinkFactory<LinkConfigWithDependentAttributes> {

	public static class H implements Connection {

		public H(LinkConfigWithDependentAttributes config) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void write(byte[] bytes) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void addListener(Listener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public void removeListener(Listener listener) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public String getName() {
		return "dependendAttributes";
	}

	@Override
	public ConnectionBasedLink newLink(LinkConfigWithDependentAttributes config) {
		return new ConnectionBasedLink(new H(config),
				ArdulinkProtocol2.instance());
	}

	@Override
	public LinkConfigWithDependentAttributes newLinkConfig() {
		return new LinkConfigWithDependentAttributes();
	}

}
