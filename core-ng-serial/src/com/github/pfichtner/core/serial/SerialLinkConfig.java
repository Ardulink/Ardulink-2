package com.github.pfichtner.core.serial;

import static com.github.pfichtner.ardulink.core.guava.Iterables.forEnumeration;
import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocols;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocolN;

public class SerialLinkConfig implements LinkConfig {

	private static final int DEFAULT_SPEED = 115200;

	@Named("port")
	private String port;

	@Named("speed")
	private int speed = DEFAULT_SPEED;

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

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getSpeed() {
		return speed;
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
