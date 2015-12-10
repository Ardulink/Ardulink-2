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

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setProto(String proto) {
		this.proto = Protocols.getByName(proto);
	}

	public void setBaudrate(int baudrate) {
		this.baudrate = baudrate;
	}

	public int getBaudrate() {
		return baudrate;
	}

	public Protocol getProto() {
		return proto;
	}

	@ChoiceFor("port")
	public String[] getPortList() {
		List<String> ports = new ArrayList<String>();
		for (CommPortIdentifier portIdentifier : portIdentifiers()) {
			if (portIdentifier.getPortType() == PORT_SERIAL) {
				ports.add(portIdentifier.getName());
			}
		}
		return ports.toArray(new String[ports.size()]);
	}

	@SuppressWarnings("unchecked")
	private Iterable<CommPortIdentifier> portIdentifiers() {
		return forEnumeration((Enumeration<CommPortIdentifier>) CommPortIdentifier
				.getPortIdentifiers());
	}

}
