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

import static org.ardulink.util.Preconditions.checkNotNull;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class LinkConfigWithDependentAttributes implements LinkConfig {

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
		return port == null ? -1 : port.intValue();
	}

	public void setPort(int port) {
		this.port = Integer.valueOf(port);
	}

	public String getDevicePort() {
		return devicePort;
	}

	public void setDevicePort(String devicePort) {
		this.devicePort = devicePort;
	}

}
