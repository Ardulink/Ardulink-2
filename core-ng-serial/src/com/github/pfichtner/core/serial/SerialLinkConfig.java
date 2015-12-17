package com.github.pfichtner.core.serial;

import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import static org.zu.ardulink.util.Iterables.forEnumeration;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocols;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocolN;

public class SerialLinkConfig implements LinkConfig {

	@Named("port")
	private String port;

	@Named("baudrate")
	private int baudrate = 115200;

	@Named("proto")
	private Protocol proto = ArdulinkProtocolN.instance();

	@Named("qos")
	private boolean qos;

	@Named("waitsecs")
	private int waitsecs = 10;

	@Named("pingprobe")
	private boolean pingprobe = true;

	public int getBaudrate() {
		return baudrate;
	}

	public String getPort() {
		return port;
	}

	@ChoiceFor("port")
	public String[] listPorts() {
		List<String> ports = new ArrayList<String>();
		for (CommPortIdentifier portIdentifier : portIdentifiers()) {
			if (portIdentifier.getPortType() == PORT_SERIAL) {
				ports.add(portIdentifier.getName());
			}
		}
		return ports.toArray(new String[ports.size()]);
	}

	public Protocol getProto() {
		return proto;
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

	public void setProto(String proto) {
		this.proto = Protocols.getByName(proto);
	}

	public void setQos(boolean qos) {
		this.qos = qos;
	}

	public void setWaitsecs(int waitsecs) {
		this.waitsecs = waitsecs;
	}

}
