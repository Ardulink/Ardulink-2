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

import static java.util.Collections.emptyList;
import static org.ardulink.core.proto.api.Protocols.protoByName;
import static org.ardulink.core.proto.api.Protocols.protocolNames;

import java.io.IOException;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;

import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkConfig.I18n;
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
@I18n("message")
public class ProxyLinkConfig implements LinkConfig {

	private static final String NAMED_TCPHOST = "tcphost";

	private static final String NAMED_TCPPORT = "tcpport";

	private static final String NAMED_PROTO = "proto";

	private static final String NAMED_PORT = "port";

	private static final int DEFAULT_LISTENING_PORT = 4478;

	private static final int DEFAULT_SPEED = 115200;

	@Named(NAMED_TCPHOST)
	public String tcphost = "localhost";

	@Named(NAMED_TCPPORT)
	@Positive
	@Max(2 << 16 - 1)
	public int tcpport = DEFAULT_LISTENING_PORT;

	@Named(NAMED_PORT)
	public String port;

	@Named("speed")
	@Positive
	public int speed = DEFAULT_SPEED;

	@Named(NAMED_PROTO)
	private Protocol proto = protoByName(ArdulinkProtocol2.NAME);

	private ProxyConnectionToRemote remote;

	public void setProto(String proto) {
		this.proto = protoByName(proto);
	}

	public String getProto() {
		return proto == null ? null : proto.getName();
	}

	@ChoiceFor(NAMED_PROTO)
	public List<String> getProtos() {
		return protocolNames();
	}

	@ChoiceFor(value = NAMED_PORT, dependsOn = { NAMED_TCPHOST, NAMED_TCPPORT })
	public List<String> getAvailablePorts() throws IOException {
		return tcphost == null ? emptyList() : getRemoteInternal().getPortList();
	}

	public synchronized ProxyConnectionToRemote getRemote() throws IOException {
		ProxyConnectionToRemote result = getRemoteInternal();
		this.remote = null;
		return result;
	}

	private ProxyConnectionToRemote getRemoteInternal() throws IOException {
		if (this.remote == null) {
			this.remote = new ProxyConnectionToRemote(tcphost, tcpport);
		}
		return this.remote;
	}

}
