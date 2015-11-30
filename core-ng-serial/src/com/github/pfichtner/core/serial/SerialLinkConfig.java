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
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol;

public class SerialLinkConfig implements LinkConfig {

	private static final String PORT = "port";

	private static final String SPEED = "speed";

	private static final String PROTO = "proto";

	private static final int DEFAULT_SPEED = 115200;

	private String port;
	private int speed = DEFAULT_SPEED;
	private Protocol proto = ArdulinkProtocol.instance();

	public String getPort() {
		return port;
	}

	@Named(PORT)
	public void setPort(String port) {
		this.port = port;
	}

	@Named(PROTO)
	public void setProto(String proto) {
		this.proto = Protocols.getByName(proto);
	}

	@Named(SPEED)
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getSpeed() {
		return speed;
	}

	public Protocol getProto() {
		return proto;
	}

	@PossibleValueFor(PORT)
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
