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

package org.ardulink.core.serial.jssc;

import static org.ardulink.core.proto.api.Protocols.tryByName;
import static org.ardulink.util.Iterables.getFirst;
import static org.ardulink.util.Optionals.or;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkConfig.I18n;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.Protocols;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

import jssc.SerialPortList;

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

	private static final String NAMED_PROTO = "proto";

	private static final String NAMED_PORT = "port";

	@Named(NAMED_PORT)
	private String port;

	@Named("baudrate")
	@Positive
	private int baudrate = 115200;

	@Named(NAMED_PROTO)
	private Protocol protoName = useProtoOrFallback(ArdulinkProtocol2.NAME);

	@Named("qos")
	private boolean qos;

	@PositiveOrZero
	@Max(59)
	@Named("waitsecs")
	private int waitsecs = 10;

	@Named("pingprobe")
	private boolean pingprobe = true;

	public int getBaudrate() {
		return baudrate;
	}

	private Protocol useProtoOrFallback(String prefered) {
		return or(tryByName(prefered), () -> getFirst(Protocols.list())).orElse(null);
	}

	public String getPort() {
		return port;
	}

	@ChoiceFor(NAMED_PORT)
	public String[] listPorts() {
		return SerialPortList.getPortNames();
	}

	@ChoiceFor(NAMED_PROTO)
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
