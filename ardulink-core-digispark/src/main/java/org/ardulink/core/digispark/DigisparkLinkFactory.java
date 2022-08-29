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

package org.ardulink.core.digispark;

import java.io.IOException;
import java.io.OutputStream;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.linkmanager.LinkFactory;

public class DigisparkLinkFactory implements LinkFactory<DigisparkLinkConfig> {

	@Override
	public String getName() {
		return "digispark";
	}

	@Override
	public AbstractListenerLink newLink(DigisparkLinkConfig config) throws IOException {
		final DigisparkConnection connection = new DigisparkConnection(config);
		return new ConnectionBasedLink(connection, config.getProto().newByteStreamProcessor(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				connection.write(new byte[] { (byte) b });
			}
		}));
	}

	@Override
	public DigisparkLinkConfig newLinkConfig() {
		return new DigisparkLinkConfig();
	}

}
