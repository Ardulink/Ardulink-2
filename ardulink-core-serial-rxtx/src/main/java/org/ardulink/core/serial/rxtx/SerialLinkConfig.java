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

package org.ardulink.core.serial.rxtx;

import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import static org.ardulink.util.Iterables.forEnumeration;
import static org.ardulink.util.Iterables.getFirst;
import gnu.io.CommPortIdentifier;

import java.util.Enumeration;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkConfig.I18n;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.Protocols;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.util.Lists;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@I18n("message")
public class SerialLinkConfig implements LinkConfig {

	@Named("port")
	private String port;

	@Named("baudrate")
	@Min(1)
	private int baudrate = 115200;

	@Named("proto")
	private Protocol protoName = useProtoOrFallback(ArdulinkProtocol2
			.instance());

	@Named("qos")
	private boolean qos;

	@Min(0)
	@Max(59)
	@Named("waitsecs")
	private int waitsecs = 10;

	@Named("pingprobe")
	private boolean pingprobe = true;

	@Named("searchport")
	private boolean searchport;

	public int getBaudrate() {
		return baudrate;
	}

	private Protocol useProtoOrFallback(Protocol prefered) {
		return isAvailable(prefered) ? prefered : getFirst(Protocols.list())
				.orNull();
	}

	private boolean isAvailable(Protocol prefered) {
		return availableProtos().contains(prefered.getName());
	}

	public String getPort() {
		return port;
	}

	@ChoiceFor("port")
	public List<String> listPorts() {
		List<String> ports = Lists.newArrayList();
		for (CommPortIdentifier portIdentifier : portIdentifiers()) {
			if (portIdentifier.getPortType() == PORT_SERIAL) {
				ports.add(portIdentifier.getName());
			}
		}
		return ports;
	}

	@ChoiceFor("proto")
	public List<String> availableProtos() {
		return Protocols.names();
	}

	public String getProtoName() {
		return protoName == null ? null : protoName.getName();
	}

	public Protocol getProto() {
		return Protocols.getByName(getProtoName());
	}

	public int getWaitsecs() {
		return waitsecs;
	}

	public boolean isPingprobe() {
		return pingprobe;
	}

	public boolean isQos() {
		return this.qos;
	}

	@SuppressWarnings("unchecked")
	private Iterable<CommPortIdentifier> portIdentifiers() {
		return forEnumeration((Enumeration<CommPortIdentifier>) CommPortIdentifier
				.getPortIdentifiers());
	}

	public void setBaudrate(int baudrate) {
		this.baudrate = baudrate;
	}

	public void setPingprobe(boolean pingprobe) {
		this.pingprobe = pingprobe;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setProtoName(String protoName) {
		this.protoName = Protocols.getByName(protoName);
	}

	public void setQos(boolean qos) {
		this.qos = qos;
	}

	public void setWaitsecs(int waitsecs) {
		this.waitsecs = waitsecs;
	}

}
