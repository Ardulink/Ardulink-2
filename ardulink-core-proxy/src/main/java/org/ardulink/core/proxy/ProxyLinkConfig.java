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

package org.ardulink.core.proxy;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.Protocols;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ProxyLinkConfig implements LinkConfig {

	private static final int DEFAULT_LISTENING_PORT = 4478;

	private static final int DEFAULT_SPEED = 115200;

	@Named("tcphost")
	private String tcphost;

	@Named("tcpport")
	@Min(1)
	@Max(2 << 16 - 1)
	private int tcpport = DEFAULT_LISTENING_PORT;

	@Named("port")
	private String port;

	@Named("speed")
	@Min(1)
	private int speed = DEFAULT_SPEED;

	@Named("proto")
	private Protocol proto = ArdulinkProtocol2.instance();

	private ProxyConnectionToRemote remote;

	public String getPort() {
		return port;
	}

	public int getSpeed() {
		return speed;
	}

	public String getTcphost() {
		return tcphost;
	}

	public int getTcpport() {
		return tcpport;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setProto(String proto) {
		this.proto = Protocols.getByName(proto);
	}

	public String getProto() {
		return proto == null ? null : proto.getName();
	}

	@ChoiceFor("proto")
	public List<String> getProtos() {
		return Protocols.names();
	}

	public void setTcphost(String tcphost) {
		this.tcphost = tcphost;
	}

	public void setTcpport(int tcpport) {
		this.tcpport = tcpport;
	}

	@ChoiceFor(value = "port", dependsOn = { "tcphost", "tcpport" })
	public List<String> getAvailablePorts() throws IOException {
		return tcphost == null ? Collections.<String> emptyList()
				: getRemoteInternal().getPortList();
	}

	public synchronized ProxyConnectionToRemote getRemote()
			throws UnknownHostException, IOException {
		ProxyConnectionToRemote result = getRemoteInternal();
		this.remote = null;
		return result;
	}

	private ProxyConnectionToRemote getRemoteInternal()
			throws UnknownHostException, IOException {
		if (this.remote == null) {
			this.remote = new ProxyConnectionToRemote(tcphost, tcpport);
		}
		return this.remote;
	}

}
