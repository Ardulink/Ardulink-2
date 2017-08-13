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

package org.ardulink.mqtt;

import java.io.ByteArrayOutputStream;

import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DummyLinkFactory implements LinkFactory<LinkConfig> {

	@Override
	public String getName() {
		return "dummy";
	}

	@Override
	public Link newLink(LinkConfig config) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Protocol protocol = ArdulinkProtocol2.instance();
		Connection connection = new StreamConnection(null, outputStream,
				protocol);
		return new ConnectionBasedLink(connection, protocol);
	}

	@Override
	public LinkConfig newLinkConfig() {
		return LinkConfig.NO_ATTRIBUTES;
	}

}
