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
package org.ardulink.core.virtual.console;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.linkmanager.LinkFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class VirtualConnectionLinkFactory implements LinkFactory<VirtualConnectionConfig> {

	public static final String VIRTUAL_CONSOLE_NAME = "virtual-console";

	@Override
	public String getName() {
		return VIRTUAL_CONSOLE_NAME;
	}

	@Override
	public Link newLink(VirtualConnectionConfig config) throws Exception {
		System.out.println("Created a link that writes it's output to and gets it's input from here");
		return new ConnectionBasedLink(
				new StreamConnection(System.in, System.out, config.protocol().newByteStreamProcessor()));
	}

	@Override
	public VirtualConnectionConfig newLinkConfig() {
		return new VirtualConnectionConfig();
	}

}
